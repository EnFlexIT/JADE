/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

The updating of this file to JADE 2.0 has been partially supported by the
IST-1999-10211 LEAP Project

This file refers to parts of the FIPA 99/00 Agent Message Transport
Implementation Copyright (C) 2000, Laboratoire d'Intelligence
Artificielle, Ecole Polytechnique Federale de Lausanne

GNU Lesser General Public License

This library is free software; you can redistribute it sand/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation,
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

/**
 * KeepAlive.java
 *
 * @author Jose Antonio Exposito
 * @author MARISM-A Development group ( marisma-info@ccd.uab.es )
 * @version 0.1
 * @author Nicolas Lhuillier (Motorola Labs)
 * @version 1.0
 */


package jade.mtp.http;

import java.io.*;
import java.util.Vector;
import java.net.Socket;
import java.net.URL;

import jade.util.Logger;


/**
 * This class represents a connection to a remote server
 */
public class KeepAlive {
    
   private static Logger logger = Logger.getMyLogger(KeepAlive.class.getName()); 
  /*
   * Inner structure to contain all connection information 
   */
  public static class KAConnection {
    private Socket         socket;
    private OutputStream   out;
    private InputStream    in;
    private HTTPAddress    address;
    private Vector         connections;
    
    KAConnection(Socket s, HTTPAddress a) throws IOException {
      socket = s;
      out = new BufferedOutputStream(socket.getOutputStream());
      in = new BufferedInputStream(socket.getInputStream());
      address = a;
    }
    
    OutputStream getOut() {
      return out;
    }
    
    InputStream getIn() {
      return in;
    }

    public HTTPAddress getAddress() {
      return address;
    }
    
    public boolean equals(HTTPAddress a) {
      return address.equals(a);
    }


    void close() {
      try {
        in.close();
        out.close();
      }
      catch(IOException ioe) {
        logger.log(Logger.WARNING,"Exception while closing KA connection: "+ioe);
      }
    }
    
  } // End of KAConnection inner class
  
  private Vector connections;
  private int    dim;
  
  /** Constructor */ 
  public  KeepAlive (int dim) { 
    connections = new Vector(dim);
    this.dim = dim;
  }
  
  /** add a new connection */
  public void add(KAConnection c) {
    try {
      //The vectors are full.
      if (connections.size() == dim) {
        remove(0); //Remove the first element of vectors, is the older element
      }
      connections.addElement(c);
      //System.out.println("DEBUG: Added Ka conn: "+connections.size()+"/"+dim+" with "+c.getAddress().getPortNo());
    }
    catch(Exception ioe) {
      logger.log(Logger.WARNING,ioe.getMessage());
    }
  }
  
  /** delete an exisiting connection, based on position */  
  private void remove(int pos) {
    try {
      KAConnection old = getConnection(pos);
      connections.removeElementAt(pos);
      old.close();
    }
    catch(Exception ioe) {
      logger.log(Logger.WARNING,ioe.getMessage());
    }
  }
  
  /** delete an exisiting connection, based on its address */ 
  public void remove (HTTPAddress addr) {
    connections.removeElement(search(addr));
  }
  
  /** delete an exisiting connection*/ 
  public void remove (KAConnection ka) {
    connections.removeElement(ka);
  }



  /** get the socket of the connection when addr make matching */  
  private KAConnection getConnection(int pos) {
    return (KAConnection)connections.elementAt(pos);
  }
  
  private KAConnection search(HTTPAddress addr) {
    if (addr != null) {
      KAConnection c;
      for(int i=(connections.size()-1); i >= 0; i--) {
        if ((c=(KAConnection)getConnection(i)).equals(addr)) { 
          return c;
        }
      }
    }
    return null;
  }
  
  /** get the socket of the connection when addr make matching */  
  public KAConnection getConnection(HTTPAddress addr) {
    return search(addr);
  }
  
  /** get the dimension of Vectors */
  public  int getDim(){
    return dim;
  }
    
  /** get the capacity of Vectors */
  public int capacity() {
    //System.out.println("DIMENSION: "+dim+"  "+"TAMVECT: "+addresses.size());
    return (dim - connections.size());    
  }
       
  /** Search the last pos of addr in the connection vector*/
  /*
    public int search(String addr) {  
    int pos = -1;
    if (addr != null) {
    for(int i= (connections.size()-1); i >= 0; i--) {
    if (addr.equals(getAddress(i))) {
    pos = i;
    break;
    }
    }
    }
    return pos;
    }
  */
  
  public synchronized void swap (KAConnection c) {
    try { 
      //if only have 1 socket isn't necessary make swap function	   
      if ((dim > 1)&&(!(connections.indexOf(c)==(connections.size()-1)))) {
        //remove the elements at former position
        connections.removeElement(c);
        //put the elements at the end 
        connections.addElement(c);
	    }
    }
    catch(Exception ioe) {
     logger.log(Logger.WARNING,ioe.getMessage());
    }
  } 
  
} //End of class KeepAlive
