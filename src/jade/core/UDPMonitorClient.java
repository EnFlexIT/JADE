package jade.core;

//#MIDP_EXCLUDE_FILE

import jade.util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * The <code>UDPMonitorClient</code> sends UDP ping messages 
 * in a specified interval to the main container.
 * 
 * It can be configured with the following profile parameters:
 * <p>
 * <table border="1" cellspacing="0">
 *  <tr>
 *    <td>-udpmonitoring_ping_delay:</td>
 *    <td>time between two outgoing pings</td>
 *  </tr>
 *  <tr>
 *    <td>-udpmonitoring_port:</td>
 *    <td>number of the port on which the server is waiting for ping messages at the main container.</td>
 *  </tr>
 * </table>
 * @author Roland Mungenast - Profactor
 */
public class UDPMonitorClient {
 
  /**
   * Default time between to outgoing pings
   */
  public static int DEFAULT_PING_DELAY = 1000;
  
  
  private boolean running = false;
  private boolean terminating = false;
  
  private DatagramChannel channel;
  private String serverHost;
  private int serverPort;
  private ByteBuffer ping;
  private int pingDelay;
  private String nodeName;
  private Logger logger;
  
  /**
   * Private class which is sending ping messages regularly
   * 
   * @author Roland Mungenast - Profactor
   */
  private class Sender implements Runnable {

    public void run() {
      while (running) {
        updatePing();
        try {
          channel.send(ping, new InetSocketAddress(serverHost, serverPort));
          Thread.sleep(pingDelay - 5);
        } catch (IOException e) {
          if(logger.isLoggable(Logger.SEVERE))
            logger.log(Logger.SEVERE,"Error sengind UDP ping message for node " + nodeName+". "+e);
        } catch (InterruptedException e) { 
          e.printStackTrace(); 
        }
      }
      try {
        channel.close();
      } catch (IOException e) {
        if(logger.isLoggable(Logger.FINER))
          logger.log(Logger.FINER,"Error closing UDP channel. "+ e);
      }
    }
    
    private void updatePing() {
      ping.position(ping.limit()-1);
      if (terminating) {
        ping.put((byte)1);
        running = false;
      } else {
        ping.put((byte)0);
      }
      ping.position(0);
    }
  }
  
  /**
   * Constructor
   * @param nodeName node for which to send ping messages
   * @param serverHost hostname of the server
   * @param serverPort port on which the server is listening for ping messages
   */
  public UDPMonitorClient(String nodeName, String serverHost, int serverPort, int pingDelay) {
    logger = Logger.getMyLogger(this.getClass().getName());
    System.out.println(nodeName + " - " + serverHost + " - " + serverPort + " - " + pingDelay);
    this.nodeName = nodeName;
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    this.pingDelay = pingDelay;
  }
  
  /**
   * Starts sending UDP ping messages to the node failure server
   * @throws IOException if the 
   */
  public void start() throws IOException {
    running = true;
    channel = DatagramChannel.open();
    ping = ByteBuffer.allocate(4 + nodeName.length() + 1);
    ping.putInt(nodeName.length());
    ping.put(nodeName.getBytes());
    ping.put((byte)0);
    new Thread(new Sender()).start();
  }
  
  /**
   * Stops sending UDP ping messages
   */
  public void stop() {
    terminating = true;
  }
}


