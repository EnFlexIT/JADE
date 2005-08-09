/*
 * (c) Copyright Hewlett-Packard Company 2001 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE and no warranty that the program does not infringe 
 * the Intellectual Property rights of a third party. See the GNU Lesser General 
 * Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software Foundation, Inc., 
 * 675 Mass Ave, Cambridge, MA 02139, USA. 
 */ 

package jade.tools.SocketProxyAgent;

import jade.lang.acl.*;
import jade.util.Logger;

import java.net.*;
import java.io.*;

/**
 * Send an ACL message to the JADE socket proxy agent which is running on
 * a host system and listening on a particular port.
 */
public class JadeBridge {

    /** default host */
    public static final String DEFAULT_AGENT_PROXY_HOST = 
        "localhost";
    /** default port */
    public static final int DEFAULT_AGENT_PROXY_PORT = 
        SocketProxyAgent.DEFAULT_PORT;
    
    /** my logger */
    private final static Logger logger = 
        Logger.getMyLogger(JadeBridge.class.getName());

    /** host */
    private String host;
    /** port */
    private int port;

    /**
     * Constructor - uses default host and port number.
     */
    public JadeBridge() {
        this(DEFAULT_AGENT_PROXY_HOST, DEFAULT_AGENT_PROXY_PORT);
    }
    
    /**
     * Constructor with specified host name and port number.
     * @param aHost Name of host.
     * @param aPort Port number.
     */
    public JadeBridge(String aHost, int aPort) {
        host = aHost;
        port = aPort;
	logger.log( Logger.CONFIG, "bridge constructed for "+host+":"+port);
    }

    /**
     * get host for this bridge
     * @return host for this bridge
     */
    public String getHost() {
	return host;
    }

    /**
     * get port for this bridge
     * @return port for this bridge
     */
    public int getPort() {
	return port;
    }

    /**
     * send and receive ACL messages using java.lang.String
     * @param aMsg The message to send as a string.
     * @return The response as a string or error message if anything goes wrong.
     */
    public String sendMessage(String aMsg) {
        if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "msg to send:"+aMsg);
	}
        String response = null;
        try {
            response = sendACL(aMsg).toString();
        }
        catch (Exception e) {
            response = "Exception when sending ACL:" + e;
        }
        if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "response:"+response);
	}
        return response;
    }


    /**
     * send and receive ACL messages using <code>ACLMessage</code>
     * @param aMsg The message to send 
     * @return The response or error message if anything goes wrong.
     */
    public ACLMessage sendMessage(ACLMessage aMsg) {
        if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "msg to send:"+aMsg);
	}
        ACLMessage response = null;
        try {
            response = sendACL(aMsg.toString());
        }
        catch (Exception e) {
            response = new ACLMessage( ACLMessage.FAILURE );
            response.setContent( "Exception when sending ACL:" + e );
        }
        if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "response:"+response);
	}
        return response;
    }
    
    /**
     * Sends an ACLMessage (in String form) to gateway and returns the reply.
     * @param aMsg The message to send.
     * @return ACLMessage response from gateway.
     * @throws IOException If any error occurs during sending or receiving.
     * @throws SocketException If the socket can't be opened.
     * @throws UnknownHostException If we can't connect to the host.
     */
    public ACLMessage sendACL(String aMsg)
                    throws IOException, UnknownHostException, SocketException {
        if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "msg to send:"+aMsg);
	}
        final int ONE_SEC_AS_MS = 1000;
        int timeout = 60 * ONE_SEC_AS_MS;
        Socket socket = new Socket(host, port); // open socket to gateway

        socket.setSoTimeout(timeout);    //Wait before timing out

        if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "created socket to host \""+host+
			"\", port "+port+
			", timeout "+timeout+"ms");
	}

        ACLMessage response = null;
        PrintStream out = new PrintStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        out.println(aMsg);  // send the message
        out.flush();        // flush it completely

        java.util.Date startTime = new java.util.Date();

        if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "trying to get response...");
	}
        try {
            ACLParser parser = new ACLParser(in);  // parser works off input

            response = parser.Message();
	    if ( logger.isLoggable( Logger.FINE ) ) {
		logger.log( Logger.FINE, "response:"+response);
	    }
        }
        catch (Throwable any) {

	    if ( logger.isLoggable( Logger.WARNING ) ) {
		logger.log( Logger.WARNING, "caught "+any+
			    " trying to get response", any );
	    }

            // Unfortunately we get a ParseException for all errors (even
            // those caused by a timeout on the socket).  So do a check
            // ourselves to see if it likely a timeout caused the parse
            // exception.
            java.util.Date endTime = new java.util.Date();
            long millisecs = endTime.getTime() - startTime.getTime();

            if (millisecs > timeout * 95 / 100) {
                response = new ACLMessage(ACLMessage.FAILURE);

                response.setContent(
                    "( \"Timeout waiting for response from SocketProxy.\" )");
            }
            else {
                response = new ACLMessage(ACLMessage.FAILURE);

                response.setContent(
                "( \"JadeBridge error in parsing ACL response from SocketProxy:"
                    + any + "\" )");
            }
        }

        socket.close();

	if ( logger.isLoggable( Logger.FINE ) ) {
	    logger.log( Logger.FINE, "returning...");
	}
        return response;
    }
}
