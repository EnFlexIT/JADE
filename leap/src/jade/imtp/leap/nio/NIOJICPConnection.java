package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.JICP.*;

import java.io.IOException;
import java.io.EOFException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.*;
import java.nio.channels.*;
import java.net.Socket;
import java.net.InetAddress;

/**
   @author Giovanni Caire - TILAB
 */
public class NIOJICPConnection extends Connection {
	// type+info+session+recipient-length+recipient(255)+payload-length(4)
	private static final int MAX_HEADER_SIZE = 263;
	
	private SelectionKey myKey;
	private SocketChannel myChannel;
	private ByteBuffer headerBuf = ByteBuffer.allocateDirect(MAX_HEADER_SIZE);
	private ByteBuffer payloadBuf;
	private byte type;
	private byte info;	
	private byte sessionID;
	private String recipientID;
	private byte[] payload;
	private boolean idle = true;
	
	public NIOJICPConnection(SelectionKey k) {
		myKey = k;
		myChannel = (SocketChannel) k.channel();
	}
	
	/**
	   Read a JICPPacket from the connection.
	   The method is synchronized since we reuse the same Buffer object 
	   for reading the packet header.
	   It should be noted that the packet data may not be completely
	   available when the embedded channel is ready for a READ operation.
	   In that case a PacketIncompleteException is thrown to indicate 
	   that successive calls to this method must occur in order to 
	   fully read the packet.
	 */
	public synchronized JICPPacket readPacket() throws IOException {
		if (idle) {
			headerBuf.clear();
			int n = myChannel.read(headerBuf);
			if (n > 0) {
				idle = false;
				headerBuf.flip();
				type = headerBuf.get();
				info = headerBuf.get();
				sessionID = -1;
				if ((info & JICPProtocol.SESSION_ID_PRESENT_INFO) != 0) {
					sessionID = headerBuf.get();
				}
				if ((info & JICPProtocol.RECIPIENT_ID_PRESENT_INFO) != 0) {
					byte recipientIDLength = headerBuf.get();
					byte[] bb = new byte[recipientIDLength];
					headerBuf.get(bb);
					recipientID = new String(bb);
				}
				if ((info & JICPProtocol.DATA_PRESENT_INFO) != 0) {
		    	int b1 = (int) headerBuf.get();
		    	int b2 = (int) headerBuf.get();
		    	int payloadLength = ((b2 << 8) & 0x0000ff00) | (b1 & 0x000000ff);
		    	int b3 = (int) headerBuf.get();
		    	int b4 = (int) headerBuf.get();
		    	payloadLength |= ((b4 << 24) & 0xff000000) | ((b3 << 16) & 0x00ff0000);
		    	// FIXME: Set a meaningful maximum packet size
		    	if (payloadLength > JICPPacket.MAX_SIZE) {
		    		throw new IOException("Packet size greater that maximum allowed size. "+payloadLength);
		    	}
		    		
		    	payload = new byte[payloadLength];
					
					int payloadRead = headerBuf.remaining();
					int payloadUnread = payloadLength - payloadRead;
					if (payloadRead > 0) {
						// Part of the payload has already been read.
						headerBuf.get(payload, 0, payloadRead);
					}
					if (payloadUnread > 0) {
						payloadBuf = ByteBuffer.wrap(payload);
						payloadBuf.position(payloadRead);
						n = myChannel.read(payloadBuf);
						if (payloadBuf.remaining() > 0) {
							if (n > 0) {
								throw new PacketIncompleteException();
							}
							else {
								idle = true;
								throw new EOFException("Channel closed");
							}
						}
					}
				}
				return buildPacket();
			}
			else { 
				throw new EOFException("Channel closed");
			}
		}
		else {
			// We are in the middle of reading the payload of a packet
			int n = myChannel.read(payloadBuf);
			if (payloadBuf.remaining() > 0) {
				if (n > 0) {
					throw new PacketIncompleteException();
				}
				else {
					idle = true;
					throw new EOFException("Channel closed");
				}
			}
			return buildPacket();
		}
	}
	
	private JICPPacket buildPacket() {
		JICPPacket pkt = new JICPPacket(type, info, recipientID, payload);
		pkt.setSessionID(sessionID);
		idle = true;
		recipientID = null;
		payload = null;
		payloadBuf = null;
		return pkt;
	}
		
	
	/**
	   Write a JICPPacket on the connection
	 */
	public int writePacket(JICPPacket pkt) throws IOException {
  	OutputStream os = new ByteArrayOutputStream() {	  	
			public void flush() throws IOException {
				ByteBuffer bb = ByteBuffer.wrap(buf, 0, count);
				myChannel.write(bb);
			}
  	};
  	int n = pkt.writeTo(os);
  	os.flush();
  	return n;
	}
	
	/**
	   Close the connection
	 */
	public void close() throws IOException {
		myChannel.close();
	}
	
  public String getRemoteHost() throws Exception {
  	Socket s = myChannel.socket();
    InetAddress address = s.getInetAddress();
    return address.getHostAddress();
  }
  
  public void configureBlocking() {
  	try { 
	  	myKey.cancel();
	  	myChannel.configureBlocking(true);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
}

