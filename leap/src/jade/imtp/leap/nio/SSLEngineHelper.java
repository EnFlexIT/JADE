/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.imtp.leap.ICPException;
import jade.imtp.leap.SSLHelper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * helper class that holds the ByteBuffers, SSLEngine and other Objects that deal with the non static part of the
 * ssl/nio handshaking/input/output. The contained SSLEngine is hidden to apps, because this helper takes on the responsibility
 * of dealing with concurrency issues
 * 
 * @author eduard
 */
public final class SSLEngineHelper implements BufferTransformer {

    public static final ByteBuffer EMPTY_BUFFER = NIOHelper.EMPTY_BUFFER;
    
    private SSLEngine ssle = null;
    private ByteBuffer wrapData;
    private ByteBuffer unwrapData;
    private ByteBuffer sendData;
    private NIOJICPConnection connection = null;
    private boolean useUnwrapped = false;
    private static Logger log = Logger.getLogger(SSLEngineHelper.class.getName());
    private boolean needToRead = false;

    /**
     * Creates and initializes ByteBuffers and SSLEngine necessary for ssl/nio.
     * @see NIOHelper
     * @see SSLHelper
     * @param host provides a hint for optimization
     * @param port provides a hint for optimization
     * @param connection the connection that will use this helper
     * @throws ICPException
     */
    public SSLEngineHelper(String host, int port, NIOJICPConnection connection) throws ICPException {
        SSLContext context = SSLHelper.createContext();
        // get a SSLEngine, use host and port for optimization
        ssle = context.createSSLEngine(host, port);
        ssle.setUseClientMode(false);
        ssle.setNeedClientAuth(SSLHelper.needAuth());
        if (!SSLHelper.needAuth()) {
            // if we don't do authentication we restrict our ssl connection to use specific cipher suites
            ssle.setEnabledCipherSuites(SSLHelper.getSupportedKeys());
        }

        SSLSession session = ssle.getSession();
        // TODO prevent buffer overflow, why *2?
        unwrapData = ByteBuffer.allocateDirect(session.getApplicationBufferSize() + 1500);
        wrapData = ByteBuffer.allocateDirect(session.getPacketBufferSize());
        sendData = ByteBuffer.allocateDirect(wrapData.capacity());;
        this.connection = connection;
    }
    /**
     * executes a wrap on the SSLEngine, prevents threads from calling this method concurrently
     * @param source
     * @return
     * @throws SSLException
     */
    private synchronized SSLEngineResult encode(ByteBuffer source) throws SSLException {
        try {
            return ssle.wrap(source, wrapData);
        } catch (SSLException sSLException) {
            throw sSLException;
        }
    }

    /**
     * executes an unwrap on the SSLEngine, prevents threads from calling this method concurrently
     * @return
     * @throws SSLException
     */
    private synchronized SSLEngineResult decode(ByteBuffer socketData) throws SSLException {
        return ssle.unwrap(socketData, unwrapData);
    }

    /**
     * runs all background handshaking tasks, blocks untill these are finished
     * @return the handshak status after finishing the background tasks
     */
    private synchronized SSLEngineResult.HandshakeStatus runHandshakTasks() {
        Runnable task = null;
        while ((task = ssle.getDelegatedTask()) != null) {
            task.run();
        }
        // re evaluate handshake status
        return ssle.getHandshakeStatus();
    }

    /**
     * closes the SSLEngine, tries to send a ssl close message
     * @throws IOException
     */
    public void close() throws IOException {

        /*
         * try to nicely terminate ssl connection
         * 1 closeoutbound
         * 2 send ssl close message
         * 3 wait for client to also send close message
         * 4 close inbound
         * 5 don't let all this frustrate the channel closing
         */

        ssle.closeOutbound();

        sendSSLClose();

        ssle.closeInbound();

    }

    private void sendSSLClose() {
        try {
            // try to send close message
            while (!ssle.isOutboundDone()) {
                wrapAndSend();
            }
        } catch (IOException e) {
            log.log(Level.FINE, "unable to send ssl close packet", e);
        }
    }

    /**
     * When data is available in the buffer holding application data {@link NIOHelper#copyAsMuchAsFits(java.nio.ByteBuffer, java.nio.ByteBuffer)} is
     * called, else {@link NIOHelper#doHandshake(java.nio.ByteBuffer) } is called
     * @return the number of application bytes generated, 0 means nothing read yet
     * @throws IOException
     */
    private int decrypt(ByteBuffer socketData) throws IOException {
        int n = 0;
        if (useUnwrapped) {
            // first we need to use any data left in unwrapBuffer
            if (unwrapData.hasRemaining()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("using " + unwrapData.remaining() + " bytes left unhandled for buffer");
                }
                n = unwrapData.remaining();
            } else {
                // we used all data in unwrapBuffer
                useUnwrapped = false;
            }
        }
        clear();
        return n + doHandshake(socketData);
    }

    private final int writeToChannel(ByteBuffer b) throws IOException {
        return connection.writeToChannel(b);
    }

    /**
     * clears handshaking buffers
     */
    private final void clear() throws IOException {
        if (useUnwrapped && unwrapData.hasRemaining()) {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(unwrapData, "compacting unwrapData");
            }
            unwrapData.compact();
        } else {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(unwrapData, "clearing unwrapData");
            }
            unwrapData.clear();
        }
        if (wrapData.position() > 0 && wrapData.hasRemaining()) {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(wrapData, "wrapData");
            }
            throw new IOException("wrapData has remaining data!");
        }
        wrapData.clear();
    }

    private String getRemoteHost() {
        return connection.getRemoteHost();
    }
    /**
     * First initialize your {@link SSLEngineHelper} by clearing its buffers and filling {@link SSLEngineHelper#getSocketData() }
     * with data from a connection. After this this method possibly recursively deals with handshaking and unwrapping
     * application data. The supplied appBuffer argument will be filled remaining unwrapped data will stay in the
     * {@link SSLEngineHelper#getUnwrapData() }.
     * @return the number of bytes available in the unwrapBuffer
     * @throws IOException
     */
    private int doHandshake(ByteBuffer socketData) throws SSLException, IOException {

        SSLEngineResult result = null;

        do {
            try {
                if (log.isLoggable(Level.FINE)) {
                    NIOHelper.logBuffer(socketData,"unwrapping socketData");
                }
                result = decode(socketData);
            } catch (SSLException e) {
                // send close message to the client, rethrow
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "unwrap failure " + getRemoteHost(), e);
                }
                try {
                    close();
                } catch(IOException ex) {

                }
                throw e;
            }
        } while (result.getStatus().equals(Status.OK) &&
                result.getHandshakeStatus().equals(HandshakeStatus.NEED_UNWRAP) &&
                result.bytesProduced() == 0);

        SSLEngineResult.Status status = result.getStatus();
        SSLEngineResult.HandshakeStatus handshakeStatus = result.getHandshakeStatus();

        if (log.isLoggable(Level.FINE)) {
            log.fine(result.toString());
        }

        if (status.equals(Status.OK)) {
            if (handshakeStatus.equals(HandshakeStatus.FINISHED)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" finished handshaking " + getRemoteHost());
                }
            } else if (handshakeStatus.equals(HandshakeStatus.NEED_TASK)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" need to run handshake tasks " + getRemoteHost());
                }
                // re evaluate handshake status
                handshakeStatus = runHandshakTasks();

                if (handshakeStatus.equals(HandshakeStatus.FINISHED)) {
                    log.warning("not expected finished handshaking " + getRemoteHost());
                } else if (handshakeStatus.equals(HandshakeStatus.NEED_TASK)) {
                    log.warning("not expected need task " + getRemoteHost());
                } else if (handshakeStatus.equals(HandshakeStatus.NEED_UNWRAP)) {
                    // still data in the inputbuffer to decode
                    if (log.isLoggable(Level.FINE)) {
                        log.fine(("need unwrap, waiting for more data " + getRemoteHost()));
                    }
                    // prepare buffers for use in recursion
                    clear();
                    needToRead = true;
                    return 0;
                } else if (handshakeStatus.equals(HandshakeStatus.NEED_WRAP)) {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine(" need to wrap and send to client " + getRemoteHost());
                    }
                    wrapAndSend();
                } else if (handshakeStatus.equals(HandshakeStatus.NOT_HANDSHAKING)) {
                    log.warning("not expected not handshaking " + getRemoteHost());
                }

            } else if (handshakeStatus.equals(HandshakeStatus.NEED_UNWRAP)) {
                log.warning(" need to unwrap, should not happen here " + getRemoteHost());
            } else if (handshakeStatus.equals(HandshakeStatus.NEED_WRAP)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" need to wrap and send to client " + getRemoteHost());
                }
                wrapAndSend();
            } else if (handshakeStatus.equals(HandshakeStatus.NOT_HANDSHAKING)) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(" unwrapped application data: " + result.bytesProduced() + " from " + getRemoteHost());
                }
                /*
                 * we have read all data from the socket
                 *
                 * we copy data from the decode buffer to the app Buffer and leave the rest in the unwrap Buffer
                 *
                 * applications should take care of these data left behind to process
                 *
                 */
                 if (result.bytesProduced() > 0) {
                    // remember to not discard unwrapped data
                    useUnwrapped = true;
                    if (log.isLoggable(Level.FINE)) {
                        NIOHelper.logBuffer(unwrapData, "unwrapped application data");
                    }
                    return unwrapData.remaining();
                 } else {
                     return 0;
                 }
            }
        } else if (status.equals(Status.CLOSED)) {
            log.info(" sslengine closed " + getRemoteHost());
            // send ssl close
            close();
        } else if (status.equals(Status.BUFFER_UNDERFLOW)) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("not enough data read yet " + getRemoteHost());
            }
        } else if (status.equals(Status.BUFFER_OVERFLOW)) {
            NIOHelper.logBuffer(socketData,"socketData");
            NIOHelper.logBuffer(unwrapData,"overflow unwrapData");
            log.severe(" buffer overflow should not be possible " + getRemoteHost());
        }
        // default return 0, no application data read yet
        return 0;
    }


    /**
     * generates handshake data and calls {@link NIOJICPConnection#writeToChannel(java.nio.ByteBuffer) }, possibly
     * recursive when required by handshaking
     * @return the amount of bytes written to the connection
     * @throws SSLException
     * @throws IOException
     */
    private int wrapAndSend() throws SSLException, IOException {
        wrapData.clear();
        int n = 0;
        SSLEngineResult result = encode(EMPTY_BUFFER);
        if (log.isLoggable(Level.FINE)) {
            log.fine("wrapped " + result);
        }
        if (result.bytesProduced() > 0) {
            wrapData.flip();
            n = writeToChannel(wrapData);

            if (result.getHandshakeStatus().equals(HandshakeStatus.NEED_WRAP)) {
                n += wrapAndSend();
            }
            return n;
        } else {
            log.warning("wrap produced no data " + getRemoteHost());
        }
        return n;
    }
    /**
     * encrypt application data, does not write data to socket. After this method call the data to be
     * send can be retrieved through {@link SSLEngineHelper#getWrapData() }, the data will be ready for usage.
     * @param b the appData to wrap
     * @return the status object for the wrap or null
     * @throws SSLException
     * @throws IOException
     */
    private SSLEngineResult wrapAppData(ByteBuffer b) throws SSLException, IOException {
        wrapData.clear();
        SSLEngineResult result = encode(b);
        if (log.isLoggable(Level.FINE)) {
            log.fine("wrapped " + result);
        }
        if (result.bytesProduced() > 0) {
            wrapData.flip();
            return result;
        } else {
            throw new IOException("wrap produced no data " + getRemoteHost());
        }
    }

    public ByteBuffer preprocessBufferToWrite(ByteBuffer dataToSend) throws IOException {
        sendData.clear();
        while (dataToSend.hasRemaining()) {
            SSLEngineResult res = wrapAppData(dataToSend);
            if (wrapData.remaining() > sendData.remaining()) {
                // does not fit reset input
                ByteBuffer bigger = ByteBuffer.allocateDirect(sendData.capacity() + wrapData.remaining() - sendData.remaining());
                sendData.flip();
                bigger.put(sendData);
                sendData = bigger;
            }
            NIOHelper.copyAsMuchAsFits(sendData, wrapData);
        }
        sendData.flip();
        return sendData;
    }

    public ByteBuffer postprocessBufferRead(ByteBuffer socketData) throws PacketIncompleteException, IOException {
        needToRead = false;
        int n = decrypt(socketData);
        if (n > 0) {
            unwrapData.flip();
            return unwrapData;
        } else {
            return EMPTY_BUFFER;
        }
    }

    public boolean needSocketData() {
        return needToRead;
    }
}
