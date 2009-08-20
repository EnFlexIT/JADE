package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.*;

import java.io.IOException;
import java.io.EOFException;
import java.io.ByteArrayOutputStream;
import java.nio.*;
import java.nio.channels.*;
import java.net.Socket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger log = Logger.getLogger(NIOJICPConnection.class.getName());

    public NIOJICPConnection() {
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
    public final synchronized JICPPacket readPacket() throws IOException {
        if (idle) {
            headerBuf.clear();
            int n = readHeader(headerBuf);
            if (n > 0) {
                //System.out.println("Read "+n+" bytes");
                idle = false;
                headerBuf.flip();
                type = headerBuf.get();
                //System.out.println("type = "+type);
                info = headerBuf.get();
                //System.out.println("info = "+info);
                sessionID = -1;
                if ((info & JICPProtocol.SESSION_ID_PRESENT_INFO) != 0) {
                    sessionID = headerBuf.get();
                    //System.out.println("SessionID = "+sessionID);
                }
                if ((info & JICPProtocol.RECIPIENT_ID_PRESENT_INFO) != 0) {
                    byte recipientIDLength = headerBuf.get();
                    byte[] bb = new byte[recipientIDLength];
                    headerBuf.get(bb);
                    recipientID = new String(bb);
                    //System.out.println("RecipientID = "+recipientID);
                }
                if ((info & JICPProtocol.DATA_PRESENT_INFO) != 0) {
                    int b1 = (int) headerBuf.get();
                    int b2 = (int) headerBuf.get();
                    int payloadLength = ((b2 << 8) & 0x0000ff00) | (b1 & 0x000000ff);
                    int b3 = (int) headerBuf.get();
                    int b4 = (int) headerBuf.get();
                    payloadLength |= ((b4 << 24) & 0xff000000) | ((b3 << 16) & 0x00ff0000);
                    //System.out.println("PayloadLength = "+payloadLength);
                    // FIXME: Set a meaningful maximum packet size
                    if (payloadLength > JICPPacket.MAX_SIZE) {
                        throw new IOException("Packet size greater than maximum allowed size. " + payloadLength);
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
                        n = readPayload(payloadBuf);
                        if (payloadBuf.remaining() > 0) {
                            if (n >= 0) {
                                throw new PacketIncompleteException();
                            } else {
                                idle = true;
                                throw new EOFException("Channel closed");
                            }
                        }
                    }
                }
                return buildPacket();
            } else if (n == -1) {
                throw new EOFException("Channel closed");
            } else {
                throw new PacketIncompleteException();
            }
        } else {
            // We are in the middle of reading the payload of a packet
            int n = readPayload(payloadBuf);
            if (payloadBuf.remaining() > 0) {
                if (n >= 0) {
                    throw new PacketIncompleteException();
                } else {
                    idle = true;
                    throw new EOFException("Channel closed");
                }
            }
            return buildPacket();
        }
    }

    /**
     * read data from the socket into the header buffer, subclasses may overwrite to postprocess data before filling
     * the buffer argument
     * @param headerBuf
     * @return
     * @throws IOException
     */
    protected synchronized int readHeader(ByteBuffer headerBuf) throws IOException {
        return myChannel.read(headerBuf);
    }

    /**
     * read data from the socket into the payload buffer, subclasses may overwrite to postprocess data before filling
     * the buffer argument
     * @param payloadBuf
     * @return
     * @throws IOException
     */
    protected synchronized int readPayload(ByteBuffer payloadBuf) throws IOException {
        return myChannel.read(payloadBuf);
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
    Write a JICPPacket on the connection, first calls {@link #preprocessBufferToWrite(java.nio.ByteBuffer) }
     * @return number of application bytes written to the socket
     */
    public final int writePacket(JICPPacket pkt) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int n = pkt.writeTo(os);
        if (log.isLoggable(Level.FINE)) {
            log.fine("writePacket: number of bytes before preprocessing: " + n);
        }
        ByteBuffer bb = preprocessBufferToWrite(ByteBuffer.wrap(os.toByteArray()));
        int m = 0;
        if (bb.hasRemaining()) {
            int toWrite = bb.remaining();
            m = writeToChannel(bb);
            if (log.isLoggable(Level.FINE)) {
                log.fine("writePacket: bytes written " + m + ", needed to write: " + toWrite);
            }
            if (toWrite!=m) {
                throw new IOException("writePacket: bytes written " + m + ", needed to write: " + toWrite);
            }
        }
        return m;
    }

    /**
     * writes data to the channel
     * @param bb
     * @return the number of bytes written to the channel
     * @throws IOException
     */
    public final int writeToChannel(ByteBuffer bb) throws IOException {
        return myChannel.write(bb);
    }

    /**
     * does nothing, subclasses may override to preprocess the ByteBuffer before {@link #writeToChannel(java.nio.ByteBuffer) sending}
     * @param dataToSend
     * @return the preprocessed ByteBuffer
     */
    protected ByteBuffer preprocessBufferToWrite(ByteBuffer dataToSend) throws IOException {
        return dataToSend;
    }

    /**
    Close the connection
     */
    public void close() throws IOException {
        myChannel.close();
    }

    public String getRemoteHost() {
        Socket s = myChannel.socket();
        InetAddress address = s.getInetAddress();
        return address.getHostAddress();
    }

    public void configureBlocking() {
        try {
            myKey.cancel();
            myChannel.configureBlocking(true);
        } catch (Exception e) {
            Logger.getLogger(NIOJICPConnection.class.getName()).log(Level.SEVERE, "error configuring blocking", e);
        }
    }

    void init(SelectionKey key) throws ICPException {
        this.myKey = key;
        this.myChannel = (SocketChannel) key.channel();
    }
}

