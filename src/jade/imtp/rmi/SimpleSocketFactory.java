package jade.imtp.rmi;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.net.ServerSocket;

import java.io.IOException;
import java.io.Serializable;

import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;


public class SimpleSocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory, Serializable {
	public Socket createSocket(String host, int port) throws IOException {
		return new Socket(host, port);
	}
	public ServerSocket createServerSocket(int port) throws IOException {
		return new ServerSocket(port);
	}
}

