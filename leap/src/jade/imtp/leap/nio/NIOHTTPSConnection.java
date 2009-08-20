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
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduard
 */
public class NIOHTTPSConnection extends NIOHTTPConnection {
    private SSLEngineHelper helper = null;
    private static Logger log = Logger.getLogger(NIOHTTPSConnection.class.getName());

    protected ByteBuffer preprocessBufferToWrite(ByteBuffer dataToSend) throws IOException {
        /* TODO
         * compare to NIOJICPSConnection
         * - first let super preprocess
         * - then finish encoding
         * - finally the NIOJICPConection superclass will do the sending
         *
         *
         */
        throw new UnsupportedOperationException("not yet implemented");
    }

    protected synchronized int readHeader(ByteBuffer headerBuf) throws IOException {
        /* TODO
         * compare to NIOJICPSConnection
         * - first finish handshaking/decoding
         * - then let super handle data
         * - finally the NIOJICPConection superclass will do the JICPPacket handling
         *
         * the super should probably be refactored because now it reads data from the socket where
         * this task is performed by this class now
         *
         */
        throw new UnsupportedOperationException("not yet implemented");
    }

    protected synchronized int readPayload(ByteBuffer payloadBuf) throws IOException {
        /*
         * probably there is no need to override this method....readHeader already prepared the data
         * sufficiently
         *
         */
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void close() throws IOException {
        try {
            helper.close();
        } catch (IOException ex) {
        }
        super.close();
    }

    void init(SelectionKey key) throws ICPException {
        super.init(key);
        if (log.isLoggable(Level.FINE)) {
            log.fine("initialize ssl tooling");
        }
        Socket s = ((SocketChannel) key.channel()).socket();
        helper = new SSLEngineHelper(s.getInetAddress().getHostName(), s.getPort(),this);
    }

}
