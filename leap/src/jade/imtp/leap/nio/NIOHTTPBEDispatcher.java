package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPProtocol;
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
        JICPPacket response = super.handleJICPPacket(p, addr, port);
        if ((p.getInfo() & JICPProtocol.TERMINATED_INFO) != 0) {
        	// PEER TERMINATION NOTIFICATION --> Close the connection. Note that in this case the response is certainly null (we have nothing to send back)
            try {
            	c.close();
            }
            catch (Exception e) {}
        }
        return response; 
    }

    public void handleConnectionError(Connection c, Exception e) {
        super.handleConnectionError();
    }

    public Properties getProperties() {
        return new Properties();
    }

    @Override
    public JICPPacket handleJICPPacket(JICPPacket pkt, InetAddress addr, int port) throws ICPException {
        throw new ICPException("Unexpected call");
    }

}
