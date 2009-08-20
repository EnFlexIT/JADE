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
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * helper class that holds the ByteBuffers, SSLEngine and other Objects that deal with the non static part of the
 * ssl/nio handshaking/input/output. The contained SSLEngine is hidden to apps, because this helper takes on the responsibility
 * of dealing with concurrency issues
 * 
 * @author eduard
 */
public final class SSLEngineHelper {
    private SSLEngine ssle = null;
    private ByteBuffer socketData;
    private ByteBuffer wrapData;
    private ByteBuffer unwrapData;
    private NIOJICPConnection connection = null;
    private static Logger log = Logger.getLogger(SSLEngineHelper.class.getName());

    /**
     * Creates and initializes ByteBuffers and SSLEngine necessary for ssl/nio.
     * @see NIOSSLHelper
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
        socketData = ByteBuffer.allocateDirect(session.getPacketBufferSize());
        // TODO prevent buffer overflow, why *2?
        unwrapData = ByteBuffer.allocateDirect(session.getApplicationBufferSize() * 2);
        wrapData = ByteBuffer.allocateDirect(session.getPacketBufferSize());
        this.connection = connection;
    }

    /**
     *
     * @return the buffer to contain the raw socket data
     */
    public ByteBuffer getSocketData() {
        return socketData;
    }

    /**
     * executes a wrap on the SSLEngine, prevents threads from calling this method concurrently
     * @param source
     * @return
     * @throws SSLException
     */
    public synchronized SSLEngineResult encode(ByteBuffer source) throws SSLException {
        return ssle.wrap(source, wrapData);
    }

    /**
     * executes an unwrap on the SSLEngine, prevents threads from calling this method concurrently
     * @param source
     * @return
     * @throws SSLException
     */
    public synchronized SSLEngineResult decode(ByteBuffer source) throws SSLException {
        return ssle.unwrap(source, unwrapData);
    }

    /**
     * runs all background handshaking tasks, blocks untill these are finished
     * @return the handshak status after finishing the background tasks
     */
    public synchronized SSLEngineResult.HandshakeStatus runHandshakTasks() {
                Runnable task = null;
                while ((task = ssle.getDelegatedTask()) != null) {
                    task.run();
                }
                // re evaluate handshake status
                return ssle.getHandshakeStatus();
    }

    /**
     *
     * @return the buffer to contain the decrypted data after reading
     */
    public ByteBuffer getUnwrapData() {
        return unwrapData;
    }

    /**
     *
     * @return the buffer to contain the encrypted data to send
     */
    public ByteBuffer getWrapData() {
        return wrapData;
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
                NIOSSLHelper.wrapAndSend(this);
            }
        } catch (IOException e) {
            log.log(Level.FINE, "unable to send ssl close packet", e);
        }
    }

    /**
     * clears all handshaking buffers
     */
    public void clear() {
        socketData.clear();
        unwrapData.clear();
        wrapData.clear();
    }

    public String getRemoteHost() {
        return connection.getRemoteHost();
    }

    public NIOJICPConnection getConnection() {
        return connection;
    }
    
}
