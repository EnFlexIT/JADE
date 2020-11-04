package jade.imtp.leap.JICP;

//#J2ME_EXCLUDE_FILE

import jade.core.IMTPException;
import jade.mtp.TransportAddress;

import java.net.InetSocketAddress;
import java.net.Socket;

public class NATUtils {
	
	private String serverAddr;
	private int serverPort;
	
	public NATUtils(String serverAddr, int serverPort) {
		this.serverAddr = serverAddr;
		this.serverPort = serverPort;
	}
	
	public InetSocketAddress[] getNATMapping(final String localAddress, final int localPort, int timeout) throws Exception {
		TransportAddress ta = new JICPAddress(serverAddr, String.valueOf(serverPort), null, null);
		JICPConnection con = null;
		try {
			con = new JICPConnection(ta) {
				@Override
				protected void bindSocket(Socket sc) {
					try {
						if (localPort > 0) {
							if (localAddress != null) {
								sc.bind(new InetSocketAddress(localAddress, localPort));
							}
							else {
								sc.bind(new InetSocketAddress(localPort));
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
 			};
			JICPPacket pkt = new JICPPacket(JICPProtocol.GET_ADDRESS_TYPE, JICPProtocol.DEFAULT_INFO, new byte[]{0});
			con.writePacket(pkt);
			InetSocketAddress internalEndPoint = new InetSocketAddress(con.getLocalHost(), con.getLocalPort());
			JICPPacket rsp = con.readPacket();
			if (rsp.getType() != JICPProtocol.ERROR_TYPE) {
				byte[] data = rsp.getData();
				if (data != null) {
					String str = new String(data);
					System.out.println("Received data "+str);
					String[] ss = str.split(":");
					if (ss.length == 2) {
						try {
							InetSocketAddress externalEndPoint = new InetSocketAddress(ss[0], Integer.parseInt(ss[1]));
							return new InetSocketAddress[]{internalEndPoint, externalEndPoint};
						}
						catch (NumberFormatException nfe) {
							throw new IMTPException("Wrong port format in JICP response");
						}
					}
					else {
						throw new IMTPException("Missing port information in JICP response");
					}
				}
				else {
					throw new IMTPException("Null JICP response");
				}
			}
			else {
				throw new IMTPException("JICP Protocol error");
			}
		}
		finally {
			// Close the socket
			try {con.close();} catch (Exception e) {}
		}
	}
	
	public static void main(String[] args) {
//		TransportAddress ta = new JICPAddress(args[0], args[1], null, null);
//		try {
//			JICPConnection con = new JICPConnection(ta) {
//				@Override
//				protected void bindSocket(Socket sc) {
//					try {
//						sc.bind(new InetSocketAddress(3333));
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
// 			};
//			JICPPacket pkt = new JICPPacket(JICPProtocol.GET_ADDRESS_TYPE, JICPProtocol.DEFAULT_INFO, new byte[]{0});
//			con.writePacket(pkt);
//			pkt = con.readPacket();
//			byte[] data = pkt.getData();
//			if (data != null) {
//				System.out.println(new String(data));
//			}
//			else {
//				System.out.println("NO DATA");
//			}
//			con.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		NATUtils nat = new NATUtils(args[0], Integer.parseInt(args[1]));
		
		try {
			InetSocketAddress[] aa = nat.getNATMapping(null, 8888, 10000);
			System.out.println("local =    "+aa[0]);
			System.out.println("external = "+aa[1]);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
