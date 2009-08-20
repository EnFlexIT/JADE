/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jade.imtp.leap.nio;

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.JICPProtocol;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author eduard
 */
public class NIOHTTPConnection extends NIOJICPConnection {
    static final String RECIPIENT_ID_FIELD = "recipient-id";
    
    ByteBuffer socketData = ByteBuffer.allocateDirect(1024);
    ByteArrayOutputStream appData = new ByteArrayOutputStream(1024);

    Logger log = Logger.getLogger(NIOHTTPConnection.class.getName());

    protected ByteBuffer preprocessBufferToWrite(ByteBuffer dataToSend) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(dataToSend.remaining());
        while (dataToSend.hasRemaining()) {
            out.write(dataToSend.get());
        }
        out.flush();
        if (log.isLoggable(Level.FINE)) {
            log.fine("packet: " + out);
        }
        // Create an HTTPResponse and set the serialized JICPPacket as payload
        NIOHTTPResponse response = new NIOHTTPResponse();
        response.setCode("200");
        response.setMessage("OK");
        response.setHttpType("HTTP/1.1");
        response.setPayload(out.toByteArray());
        out.reset();
        response.writeResponse(out);
        return ByteBuffer.wrap(out.toByteArray());
    }

    protected synchronized int readHeader(ByteBuffer headerBuf) throws IOException {

        socketData.clear();

        // here we receive a JICPPacket wrapped in a http request
        int n = super.readHeader(socketData);

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
                socketData.flip();
                while (socketData.hasRemaining()) {
                    appData.write(socketData.get());
                }
                // Read an HTTP request from the network
                NIOHTTPRequest request = new NIOHTTPRequest();

                request.readRequest(new ByteArrayInputStream(appData.toByteArray()));

                if (request.getMethod().equals("GET")) {
                    // This is a CONNECT_MEDIATOR
                    String recipientID = request.getField(RECIPIENT_ID_FIELD);
                    JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, recipientID, null);
                    ByteArrayOutputStream out = new ByteArrayOutputStream(pkt.getLength());
                    pkt.writeTo(out);
                    headerBuf.put(out.toByteArray());
                    return out.size();
                } else {
                    JICPPacket pkt = JICPPacket.readFrom(new ByteArrayInputStream(request.getPayload()));
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("request payload: " + request.getPayload().length + ", jicp packet length " + pkt.getLength());
                    }
                    ByteArrayOutputStream out = new ByteArrayOutputStream(pkt.getLength());
                    pkt.writeTo(out);
                    
                    socketData = ByteBuffer.allocateDirect(out.size());
                    socketData.put(out.toByteArray());
                    socketData.flip();

                    if (log.isLoggable(Level.FINE)) {
                        NIOSSLHelper.logBuffer(socketData, "socketData");
                    }
                    // current position in header
                    int pos = headerBuf.position();
                    if (log.isLoggable(Level.FINE)) {
                        NIOSSLHelper.logBuffer(headerBuf, "headerBuf");
                    }

                    // read from socketdata as much as fits in header
                    int limit = socketData.limit();
                    if (limit > headerBuf.remaining()) {
                        // data from socket does not fit, set limit so that data will fit
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("setting limit of socketbuffer buffer to " + headerBuf.remaining());
                        }
                        socketData.limit(headerBuf.remaining());
                    }

                    headerBuf.put(socketData);

                    // reset limit, to make rest of data available to put in payload buffer
                    socketData.limit(limit);
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("bytes copied to header " + (headerBuf.position() - pos));
                    }

                    if (log.isLoggable(Level.FINE)) {
                        NIOSSLHelper.logBuffer(socketData, "socketData");
                        NIOSSLHelper.logBuffer(headerBuf, "headerBuf");
                    }
                    // return number of data read
                    return headerBuf.position() - pos;
                }
        }

    }

    protected synchronized int readPayload(ByteBuffer payloadBuf) throws IOException {
        // copy data from forPayload into headerBuf, return number of data copied
        if (log.isLoggable(Level.FINE)) {
            log.fine("reading payload " + getRemoteHost());
        }
        int pos = payloadBuf.position();
        if (socketData.hasRemaining()) {
            payloadBuf.put(socketData);
        }
        return payloadBuf.position() - pos;
    }

}
