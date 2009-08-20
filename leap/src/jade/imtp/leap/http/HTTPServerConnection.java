/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Motorola.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.imtp.leap.http;

//#MIDP_EXCLUDE_FILE
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.JICPProtocol;

import java.io.*;
import java.net.*;

/**
 * Connection class to handle HTTP requests/responses on 
 * the server side.
 * Note that unlike the HTTPClientConnection that "looks 
 * like" a permanent pipe where several request/response sessions 
 * can occur, the HTTPServerConnection handles a single
 * request/response session.
 * @author Giovanni Caire - TILAB
 */
class HTTPServerConnection extends Connection {

    private Socket sc;
    private InputStream is;
    private OutputStream os;
    private boolean readAvailable;
    private boolean writeAvailable;

    /**
     * Constructor declaration
     */
    public HTTPServerConnection(Socket s) {
        sc = s;
        readAvailable = true;
        writeAvailable = false;
    }

    public JICPPacket readPacket() throws IOException {
        if (readAvailable) {
            // Read an HTTP request from the network
            HTTPRequest request = new HTTPRequest();
            is = sc.getInputStream();
            request.readFrom(is);
            readAvailable = false;
            writeAvailable = true;
            if (request.getMethod().equals("GET")) {
                // This is a CONNECT_MEDIATOR
                String recipientID = request.getField(HTTPClientConnection.RECIPIENT_ID_FIELD);
                JICPPacket pkt = new JICPPacket(JICPProtocol.CONNECT_MEDIATOR_TYPE, JICPProtocol.DEFAULT_INFO, recipientID, null);
                return pkt;
            } else {
                // Read the JICPPacket from the HTTP request payload
                ByteArrayInputStream bis = new ByteArrayInputStream(request.getPayload());
                return JICPPacket.readFrom(bis);
            }
        } else {
            throw new IOException("Read not available");
        }
    }

    public int writePacket(JICPPacket pkt) throws IOException {
        if (writeAvailable) {
            try {
                // Transform the JICPPacket into a sequence of bytes
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int ret = pkt.writeTo(bos);
                // Create an HTTPResponse and set the serialized JICPPacket as payload
                HTTPResponse response = new HTTPResponse();
                response.setCode("200");
                response.setMessage("OK");
                response.setHttpType("HTTP/1.1");
                response.setPayload(bos.toByteArray());
                // Write the HTTPResponse to os and close the connection
                os = sc.getOutputStream();
                response.writeTo(os);
                os.flush();
                readAvailable = true;
                writeAvailable = false;
                return ret;
            } finally {
                try {
                    close();
                } catch (Exception e) {
                }
            }
        } else {
            throw new IOException("Write not available");
        }
    }

    /**
     */
    public void close() throws IOException {
        readAvailable = false;
        writeAvailable = false;
        try {
            is.close();
        } catch (Exception e) {
        }
        is = null;
        try {
            os.close();
        } catch (Exception e) {
        }
        os = null;
        try {
            sc.close();
        } catch (Exception e) {
        }
        sc = null;
    }

    /**
     */
    public String getRemoteHost() throws Exception {
        return sc.getInetAddress().getHostAddress();
    }
}

