package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.http.HTTPBEDispatcher;
import jade.util.leap.Properties;
import java.net.InetAddress;

/**
 *
 * @author Eduard Drenth: Logica, 11-jul-2009
 * 
 */
public class NIOHTTPBEDispatcher extends HTTPBEDispatcher implements NIOMediator {

    public JICPPacket handleJICPPacket(Connection c, JICPPacket p, InetAddress addr, int port) throws ICPException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void handleConnectionError(Connection c, Exception e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Properties getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
