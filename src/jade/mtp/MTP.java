/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
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

package jade.mtp;

import jade.domain.FIPAAgentManagement.Envelope;

/**
   Abstract interface for Message Transport Protocols

   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$

*/
public interface MTP {

  /**
     Generic exception class for MTP errors.
   */
  public static class MTPException extends Exception {

    /**
       @serial
    */
    private Throwable nested;

    /**
       Constructor for exception class.
       @param msg A message detailing the cause of the exception.
       @param t The exception wrapped by this object.
     */
    public MTPException(String msg, Throwable t) {
      super(msg);
      nested = t;
    }

    /**
      Reads the exception wrapped by this object.
      @return the <code>Throwable</code> object that is the exception thrown by
      the concrete MTP subsystem.
    */
    public Throwable getNested() {
      return nested;
    }

  } // End of MTPException class.


  /**
     Activates an MTP handler for incoming messages on a default
     address.
     @return A <code>TransportAddress</code>, corresponding to the
     chosen default address.
     @exception MTPException Thrown if some MTP initialization error
     occurs.
  */
  TransportAddress activate() throws MTPException;

  /**
     Activates an MTP handler for incoming messages on a specific
     address.
     @param ta A <code>TransportAddress</code> object, representing
     the transport address to listen to.
     @exception MTPException Thrown if some MTP initialization error
     occurs.
   */
  void activate(TransportAddress ta) throws MTPException;

  /**
     Deactivates the MTP handler listening at a given transport
     address.
     @param ta The <code>TransportAddress</code> object the handle to
     close is listening to.
     @exception MTPException Thrown if some MTP cleanup error occurs.
   */
  void deactivate(TransportAddress ta);

  /**
     Deactivates all the MTP handlers.
     @exception MTPException Thrown if some MTP cleanup error occurs.
   */
  void deactivate();

  /**
     Delivers to the specified address an ACL message, encoded in some
     concrete message representation, using the given envelope as a
     transmission header.
     @param ta The transport address to deliver the message to. It
     must be a valid address for this MTP.
     @param env The message envelope, containing various fields
     related to message recipients, encoding, and timestamping.
     @payload The byte sequence that contains the encoded ACL message.
     @exception MTPException Thrown if some MTP delivery error occurs.
   */
  void deliver(TransportAddress ta, Envelope env, byte[] payload) throws MTPException;

  /**
     Converts a string representing a valid address in this MTP to a
     <code>TransportAddress</code> object.
     @param rep The string representation of the address.
     @return A <code>TransportAddress</code> object, created from the
     given string.
     @exception MTPException If the given string is not a valid
     address according to this MTP.
   */
  TransportAddress strToAddr(String rep) throws MTPException;

  /**
     Converts a <code>TransportAddress</code> object into a string
     representation.
     @param ta The <code>TransportAddress</code> object.
     @return A string representing the given address.
     @exception MTPException If the given
     <code>TransportAddress</code> is not a valid address for this
     MTP.
   */
  String addrToStr(TransportAddress ta) throws MTPException;

  /**
     Reads the name of the message transport protocol managed by this
     MTP. The FIPA standard message transport protocols have a name
     starting with <code><b>"fipa.mts.mtp"</b></code>.
     @return A string, that is the name of this MTP.
   */
  String getName();

}
