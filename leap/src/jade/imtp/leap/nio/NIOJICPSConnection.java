/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.ICPException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;

/**
 * This class provides a nio based server connection for which ssl can be configured. A {@link SSLEngine} is used
 * for dealing with handshaking and encrypting/decrypting application data. The superclass does the actual
 * {@link #readHeader(java.nio.ByteBuffer) reading} (also {@link #readPayload(java.nio.ByteBuffer)}),
 * and {@link #writeToChannel(java.nio.ByteBuffer) writing} from the SocketChannel and handles the application data.
 *
 * @see SSLContext
 * @author eduard
 */
public class NIOJICPSConnection extends NIOJICPConnection {

    private SSLEngineHelper helper = null;
    private static Logger log = Logger.getLogger(NIOJICPSConnection.class.getName());

    public NIOJICPSConnection() {
    }

    /**
     * Initializes this connection by setting key and channel and the {@link SSLEngineHelper }.
     *
     * @see SSLEngineHelper
     * @param key the Selection key provided by the {@link Selector}.
     * @throws ICPException
     */
    void init(SelectionKey key) throws ICPException {
        super.init(key);
        if (log.isLoggable(Level.FINE)) {
            log.fine("initialize ssl tooling");
        }
        Socket s = ((SocketChannel) key.channel()).socket();
        helper = new SSLEngineHelper(s.getInetAddress().getHostName(), s.getPort(),this);
    }

    /**
     * first try to send ssl close packet, then close channel
     * @throws IOException
     */
    public void close() throws IOException {

        try {
            helper.close();
        } catch (IOException ex) {
        }
        super.close();
    }


    /**
     * First the super is called to read data from the socket. Note that, in contrast to the super,
     * all data will be read from the socket in one call. The reason for this is that the SSLEngine unwraps application data by decrypting
     * the socket data. We remember the 'extra' data read and provide that when the super calls {@link #readPayload(java.nio.ByteBuffer)}.
     * @param headerBuf
     * @return the number of application bytes generated, -1 means end of stream, 0 means nothing read yet
     * @throws IOException
     */
    protected int readHeader(ByteBuffer headerBuf) throws IOException {
        /*
         * - first read from channel in socketData buffer
         * - do handshaking
         * - put application data in headerBuffer argument
         * - possibly remember payload data read
         * - return number of application bytes read
         *
         */

        // possibly we get here with data left in the unwrapBuffer, just copy and return
        if (helper.getUnwrapData().position() > 0 && helper.getUnwrapData().hasRemaining()) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("using " + helper.getUnwrapData().remaining() + " bytes left unhandled for new headerBuffer");
                return NIOSSLHelper.copyAsMuchAsFits(helper.getUnwrapData(), headerBuf);
            }
        }

        helper.clear();
        // read data into the socketbuffer

        int n = super.readHeader(helper.getSocketData());
        if (log.isLoggable(Level.FINE)) {
            log.fine(n + " bytes read " + getRemoteHost());
        }

        switch (n) {
            case 0:
                // nothing read....yet
                if (log.isLoggable(Level.FINE)) {
                    log.fine("nothing read yet from socket " + getRemoteHost());
                }
                return 0;
            case -1:
                log.info("end of stream reached " + getRemoteHost());
                return n;
            default:
                // prepare buffer
                helper.getSocketData().flip();
                // handshake
                return NIOSSLHelper.doHandshake(helper, headerBuf);
        }
    }


    /**
     * When the super calls this we copy data left in the unwrapbuffer to the payload
     * @param payloadBuf
     * @return
     * @throws IOException
     */
    protected final int readPayload(ByteBuffer payloadBuf) throws IOException {
        // copy data from forPayload into headerBuf, return number of data copied
        int pos = payloadBuf.position();
        if (helper.getUnwrapData().position() > 0 && helper.getUnwrapData().hasRemaining()) {
            if (helper.getUnwrapData().remaining() > payloadBuf.remaining()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("trying to put " + payloadBuf.remaining() + " bytes in payloadBuffer, leaving " +
                            (helper.getUnwrapData().remaining() - payloadBuf.remaining()) + " unhandled " + getRemoteHost());
                }
                return NIOSSLHelper.copyAsMuchAsFits(helper.getUnwrapData(), payloadBuf);
            }
            if (log.isLoggable(Level.FINE)) {
                log.fine("trying to put " + helper.getUnwrapData().remaining() + " bytes in payloadBuffer " + getRemoteHost());
            }
            payloadBuf.put(helper.getUnwrapData());
        } else {
            if (payloadBuf.hasRemaining()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("payload still needs " + payloadBuf.remaining() + " try to read from socket");
                }
                helper.clear();
                int n = super.readPayload(helper.getSocketData());
                if (log.isLoggable(Level.FINE)) {
                    log.fine(n + " bytes read " + getRemoteHost());
                }
                switch (n) {
                    case 0:
                        // nothing read....yet
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("nothing read yet from socket " + getRemoteHost());
                        }
                        return 0;
                    case -1:
                        log.info("end of stream reached " + getRemoteHost());
                        return n;
                    default:
                        // prepare buffer
                        helper.getSocketData().flip();
                        // handshake
                        return NIOSSLHelper.doHandshake(helper, payloadBuf);
                }
            }
        }
        return payloadBuf.position() - pos;
    }

    /**
     * encrypts end sends data, possibly recursively
     * @param bb the application data to encrypt
     * @return an empty buffer, we've done sending ourselves
     * @throws IOException
     */
    protected ByteBuffer preprocessBufferToWrite(ByteBuffer bb) throws IOException {
        if (bb.hasRemaining()) {
            SSLEngineResult result = NIOSSLHelper.wrapAppData(helper, bb);
            if (log.isLoggable(Level.FINE)) {
                log.fine("after wrap " + result);
            }
            if (result.bytesProduced() > 0) {
                int m = writeToChannel(helper.getWrapData());
                if (result.bytesProduced() != m) {
                    throw new IOException("writePacket: bytes written " + m + ", needed to write: " + result.bytesProduced());
                }
            } else {
                throw new IOException("wrap did not produce any data");
            }
            if (bb.hasRemaining()) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("still data to send, recursing....");
                }
                return preprocessBufferToWrite(bb);
            }
        }
        return NIOSSLHelper.EMPTY_BUFFER;
    }
}
