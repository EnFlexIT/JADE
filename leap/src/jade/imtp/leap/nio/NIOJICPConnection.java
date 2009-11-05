package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE
import jade.imtp.leap.ICPException;
import jade.imtp.leap.JICP.*;

import java.io.IOException;
import java.io.EOFException;
import java.io.ByteArrayOutputStream;
import java.nio.*;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
@author Giovanni Caire - TILAB
 */
public class NIOJICPConnection extends Connection implements MoreDataNotifier {
    // type+info+session+recipient-length+recipient(255)+payload-length(4)

    public static final int MAX_HEADER_SIZE = 263;
    private SocketChannel myChannel;
    private ByteBuffer headerBuf = ByteBuffer.allocateDirect(MAX_HEADER_SIZE);
    private ByteBuffer payloadBuf = ByteBuffer.allocateDirect(JICPPacket.MAX_SIZE);
    private ByteBuffer tmpBuffer = ByteBuffer.allocateDirect(JICPPacket.MAX_SIZE);
    private ByteBuffer socketData = ByteBuffer.allocateDirect(JICPPacket.MAX_SIZE+MAX_HEADER_SIZE);
    private byte type;
    private byte info;
    private byte sessionID;
    private String recipientID;
    private boolean idle = true;
    private static final Logger log = Logger.getLogger(NIOJICPConnection.class.getName());
    private boolean reuseSocketData = false;
    private boolean useTmp = false;
    private MoreDataHandler handler;
    private List<BufferTransformer> transformers = new LinkedList<BufferTransformer>();

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
            read();
            int n = transformAndCopyAfterRead(headerBuf,socketData);
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(headerBuf, "headerBuf after transform");
                NIOHelper.logBuffer(socketData, "socketData after transform");
            }
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

                    if (log.isLoggable(Level.FINE)) {
                        log.fine("limiting payload to: " + payloadLength);
                    }

                    payloadBuf.limit(payloadLength);

                    NIOHelper.copyAsMuchAsFits(payloadBuf, headerBuf);

                    if (payloadBuf.hasRemaining()) {
                        transformAndCopyAfterRead(payloadBuf,socketData);
                    }
                    if (payloadBuf.hasRemaining()) {
                        if (!socketData.hasRemaining()) {
                            throw new PacketIncompleteException();
                        }
                    } else {
                        return buildPacket();
                    }
                } else {
                    return buildPacket();
                }
            } else {
                if (!socketData.hasRemaining()) {
                    throw new PacketIncompleteException();
                }
            }
        } else {
            // We are in the middle of reading the payload of a packet
            read();
            transformAndCopyAfterRead(payloadBuf,socketData);
            if (payloadBuf.hasRemaining()) {
                if (!socketData.hasRemaining()) {
                    throw new PacketIncompleteException();
                }
            } else {
                return buildPacket();
            }
        }
        return null;
    }

    private void read() throws IOException {
        clear();
        int n = readFromChannel(socketData);
        if (n == -1) {
            idle = true;
            throw new EOFException("Channel closed");
        }
        if (log.isLoggable(Level.FINE)) {
            NIOHelper.logBuffer(socketData,"read from channel in socketBuffer: " + n);
        }
        socketData.flip();
    }

    private void clear() throws IOException {
        if (log.isLoggable(Level.FINE)) {
            NIOHelper.logBuffer(socketData, "socketData");
        }
        if (reuseSocketData) {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(socketData,"compacting socketData");
            }
            socketData.compact();
            reuseSocketData = false;
        } else {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(socketData,"clearing socketData");
            }
            socketData.clear();
        }
        if (headerBuf.position() > 0 && headerBuf.hasRemaining()) {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(headerBuf, "compacting headerBuf");
            }
            headerBuf.compact();
        } else {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(headerBuf, "clearing headerBuf");
            }
            headerBuf.clear();
        }
    }

    /**
     * reads data from the socket into a buffer
     * @param b
     * @return number of bytes read
     * @throws IOException
     */
    private final int readFromChannel(ByteBuffer b) throws IOException {
        return myChannel.read(b);
    }

    private JICPPacket buildPacket() {
        payloadBuf.flip();
        byte[] payload = new byte[payloadBuf.remaining()];
        payloadBuf.get(payload, 0, payload.length);
        JICPPacket pkt = new JICPPacket(type, info, recipientID, payload);
        pkt.setSessionID(sessionID);
        idle = true;
        recipientID = null;
        payloadBuf.clear();
        return pkt;
    }

    /**
     * Write a JICPPacket on the connection, first calls {@link #preprocessBufferToWrite(java.nio.ByteBuffer) }.
     * When the buffer returned by {@link #preprocessBufferToWrite(java.nio.ByteBuffer) }, no write will be performed.
     * @return number of application bytes written to the socket
     */
    public final synchronized int writePacket(JICPPacket pkt) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int n = pkt.writeTo(os);
        if (log.isLoggable(Level.FINE)) {
            log.fine("writePacket: number of bytes before preprocessing: " + n);
        }
        ByteBuffer toSend = ByteBuffer.wrap(os.toByteArray());
        ByteBuffer bb = transformBeforeWrite(toSend);
        if (toSend.hasRemaining()&&transformers.size()>0) {
            // for direct JICPConnections the data from the packet are used directly
            // for subclasses the subsequent transformers must transform all data from the packet before sending
            throw new IOException("still need to transform: " + toSend.remaining());
        }
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

    private int transformAndCopyAfterRead(ByteBuffer dst, ByteBuffer data) throws IOException {
        if (!data.hasRemaining()&&!useTmp) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("no bytes available for transformation");
            }
            return 0;
        }
        int n = 0;
        if (useTmp) {
            n = NIOHelper.copyAsMuchAsFits(dst, tmpBuffer);
            if (log.isLoggable(Level.FINE)) {
                log.fine("used from tmpBuffer: " + n);
            }
            if (tmpBuffer.hasRemaining()) {
                tmpBuffer.compact();
            } else {
                tmpBuffer.clear();
                useTmp = false;
            }
        }
        if (dst.hasRemaining()) {
            for (ListIterator<BufferTransformer> it = transformers.listIterator(transformers.size()); it.hasPrevious();) {
                BufferTransformer btf = it.previous();
                data = btf.postprocessBufferRead(data);
                if (!data.hasRemaining()&&it.hasPrevious()) {
                    if (btf.needSocketData()) {
                        notifyMoreDataAvailable();
                    }
                    log.warning("no data available for next transformation after " + btf.getClass().getName());
                    break;
                }
            }
            if (data.hasRemaining()) {
                n += NIOHelper.copyAsMuchAsFits(dst, data);
                if (log.isLoggable(Level.FINE)) {
                    log.fine("transformed and copied bytes: " + n);
                }
                if (data.hasRemaining()) {
                    tmpBuffer.put(data);
                    tmpBuffer.flip();
                    useTmp = true;
                }
            }
        }
        // possibly not all data from socket buffer consumed, use them in subsequent calls
        if (socketData.hasRemaining()) {
            if (log.isLoggable(Level.FINE)) {
                NIOHelper.logBuffer(socketData, "triggering extra read for socketData");
            }
            reuseSocketData = true;
            notifyMoreDataAvailable();
        }
        return n;
    }

    private ByteBuffer transformBeforeWrite(ByteBuffer data) throws IOException {
        for (BufferTransformer btf : transformers) {
            data = btf.preprocessBufferToWrite(data);
        }
        return data;
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
    Close the connection
     */
    public void close() throws IOException {
        myChannel.close();
    }

    public String getRemoteHost() {
        return myChannel.socket().getInetAddress().getHostAddress();
    }

    /**
     * sets the channel for this connection
     * @param channel
     * @throws ICPException
     */
    void init(SocketChannel channel) throws ICPException {
        this.myChannel = (SocketChannel) channel;
    }

    /**
     * Subclasses that are stateless should override this methods and let it do nothing. When, after reading and {@link #addBufferTransformer(jade.imtp.leap.nio.BufferTransformer) transforming}
     * data are left in the socketbuffer the {@link MoreDataHandler} is notified to trigger another read/write. This is pointless for subclasses that close the connection after a read/write.
     *
     */
    public void notifyMoreDataAvailable() {
        /* TODO
         *
         * possibly the channel is closed, we won't read/write then, what to do?
         *
         * this situation occurs when an extra read is triggered because data are left in the socketbuffer
         *
         * after a previous read/write in case of the HTTP protocol, the connection will be closed
         *
         * a subsequent read may be successful and may yield a reply, which cannot be send
         *
         */
        if (handler != null) {
            handler.handleExtraData();
        }
    }

    public void setMoreDataHandler(MoreDataHandler handler) {
        this.handler = handler;
    }

    public MoreDataHandler removeMoreDataHandler() {
        MoreDataHandler h = this.handler;
        this.handler = null;
        return h;
    }

    public void addBufferTransformer(BufferTransformer transformer) {
        transformers.add(transformer);
    }
}

