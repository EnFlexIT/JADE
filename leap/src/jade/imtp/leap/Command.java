/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
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
 * Copyright (C) 2001 Siemens AG.
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

package jade.imtp.leap;

import jade.core.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import java.io.IOException;

/**
 * The <code>Command</code> object is used to represent a platform command.
 * @author Michael Watzke
 * @author Steffen Rusitschka
 * @version 1.0, 09/11/2000
 */
public class Command {


  /**
   * Unspecified object id
   */
  static final int        DUMMY_ID = -1;

  /**
   * bit encoded info, if the command returns a result
   * is public for getCommandType()
   */
  public static final int HAS_RESULT = 0x8000;

  /**
   * Command identifier code for response command.
   */
  static final int        NO_OPERATION = 0;
  static final int        OK = 1;
  static final int        ERROR = 2;
  static final int        GET_PLATFORM_NAME = 3|HAS_RESULT;
  static final int        ADD_CONTAINER = 4|HAS_RESULT;
  static final int        REMOVE_CONTAINER = 5;
  static final int        LOOKUP = 6|HAS_RESULT;
  static final int        BORN_AGENT = 7;
  static final int        DEAD_AGENT = 8;
  static final int        NEW_MTP = 9;
  static final int        DEAD_MTP = 10;
  static final int        TRANSFER_IDENTITY = 11|HAS_RESULT;
  static final int        GET_PROXY = 12|HAS_RESULT;
  static final int        CREATE_AGENT_FROM_NAME = 13;
  static final int        CREATE_AGENT_FROM_DATA = 14;
  static final int        FETCH_CLASS_FILE = 15|HAS_RESULT;
  static final int        SUSPEND_AGENT = 16;
  static final int        RESUME_AGENT = 17;
  static final int        WAIT_AGENT = 18;
  static final int        WAKE_AGENT = 19;
  static final int        MOVE_AGENT = 20;
  static final int        COPY_AGENT = 21;
  static final int        KILL_AGENT = 22;
  static final int        EXIT = 23;
  static final int        POST_TRANSFER_RESULT = 24;
  static final int        DISPATCH = 25;
  public static final int PING = 26;             // see 38, blocking ping!
  static final int        INSTALL_MTP = 27|HAS_RESULT;
  static final int        UNINSTALL_MTP = 28;
  static final int        UPDATE_ROUTING_TABLE = 29;
  static final int        ROUTE_OUT = 30;
  static final int        ENABLE_SNIFFER = 31;
  static final int        DISABLE_SNIFFER = 32;
  static final int        ENABLE_DEBUGGER = 33;
  static final int        DISABLE_DEBUGGER = 34;
  static final int        SUSPENDED_AGENT = 35;
  static final int        RESUMED_AGENT = 36;
  static final int        FORWARD = 37;
  public static final int BLOCKING_PING = 38;    // make public for getCommandType()
  static final int        CHANGED_AGENT_PRINCIPAL = 39;
  static final int        SIGN = 40|HAS_RESULT;
  static final int        CHANGE_AGENT_PRINCIPAL = 41;
  static final int        CHANGE_CONTAINER_PRINCIPAL = 42;
  static final int        GET_PUBLIC_KEY = 43|HAS_RESULT;
  static final int        GET_AGENT_PRINCIPAL = 44|HAS_RESULT;


  /**
   * Code defining the type of command.
   */
  private int             commandCode;

  /**
   * Identifier of the remote object this Command is directed to.
   */
  private int             objectID;

  /**
   * This list represents the argument list of this platform command.
   */
  private List            commandParameters;

  /**
   * Construct a default NO_OPERATION command.
   */
  Command() {
    commandCode = NO_OPERATION;
    objectID = DUMMY_ID;
    commandParameters = new ArrayList();
  }

  /**
   */
  Command(int code) {
    commandCode = code;
    objectID = DUMMY_ID;
    commandParameters = new ArrayList();
  }

  /**
   */
  Command(int code, int id) {
    commandCode = code;
    objectID = id;
    commandParameters = new ArrayList();
  }

  /**
   * Return the command identifier code of this command.
   * @return the command identifier code specifying the type of command
   */
  int getCode() {
    return commandCode;
  } 

  /**
   * Set the command identifier code of this command and empty the mutated
   * command's argument list.
   * @param command_code the command identifier code this command object
   * shall mutate to
   */
  void setCode(int command_code) {
    commandCode = command_code;
    commandParameters = new ArrayList();
  } 

  /**
   * Method declaration
   * 
   * @return
   * 
   * @see
   */
  int getObjectID() {
    return objectID;
  } 

  /**
   * Method declaration
   * 
   * @param id
   * 
   * @see
   */
  void setObjectID(int id) {
    objectID = id;
  } 

  /**
   * Add a deliverable parameter, i.e., an object implementing the
   * <code>Deliverable</code> interface or a <code>java.lang.String</code> or a
   * <code>java.lang.StringBuffer</code> object to the end of the
   * argument list of this command object.
   * @param param the parameter object to be added at the end of the argument
   * list
   * @see Deliverable
   * @see DeliverableDataInputStream#readObject()
   * @see DeliverableDataOutputStream#writeObject( java.lang.Object )
   */
  void addParam(Object param) {
    commandParameters.add(param);
  } 

  /**
   * Return the number of parameters in this command object.
   */
  int getParamCnt() {
    return commandParameters.size();
  } 

  /**
   * Return the parameter at the specified index of this command object.
   * @param index the parameter index
   * @return the parameter at the specified index
   */
  Object getParamAt(int index) {
    return commandParameters.get(index);
  } 

  // Since the Deliverable interface is obsolete, the following two
  // methods are no more needed.

  /**
   * Serialize this command object to the given data output stream according
   * to the LEAP surrogate serialization mechanism.
   * @param ddout the data output stream this command object is serialized to
   * @exception LEAPSerializationException if there occurs an error
   * during the LEAP surrogate serialization
   */
  /*
   * public void serialize(DeliverableDataOutputStream ddout)
   * throws LEAPSerializationException {
   * try {
   * ddout.writeInt(commandCode);
   * ddout.writeInt(objectID);
   * ddout.writeDeliverable((Deliverable) commandParameters);
   * }
   * catch (IOException e) {
   * throw new LEAPSerializationException("I/O error during serialization of Command",
   * e);
   * }
   * }
   */

  /**
   * Deserialize this command object from the given data input stream according
   * to the LEAP surrogate serialization mechanism.
   * @param ddin the data input stream this command object is deserialized from
   * @exception LEAPSerializationException if there occurs an error
   * during the LEAP surrogate serialization
   */
  /*
   * public void deserialize(DeliverableDataInputStream ddin)
   * throws LEAPSerializationException {
   * try {
   * commandCode = ddin.readInt();
   * objectID = ddin.readInt();
   * commandParameters = (ArrayList) ddin.readDeliverable();
   * }
   * catch (IOException e) {
   * throw new LEAPSerializationException("I/O error during deserialization of Command",
   * e);
   * }
   * }
   */

  /**
   * attention: this is a hack! should be removed in the future
   * by adding different commandCodes for PING and BLOCKING_PING.
   * 
   * @return PING:            if it is a non blocking ping command
   * BLOCKING_PING:   if it is a blocking ping command
   * any other value: some other command
   * the bit "HAS_RESULT" is set, if the command returns a
   * result.
   */
  public static int getCommandType(byte[] serializedCommand) {
    // the command code is an int at the beginning of the
    // byte array (see serializeCommand() in DeliverableDataOutputStream
    int ret = 0;
    for (int i = 0; i < 4; ++i) {
      // System.out.print(" " + serializedCommand[i]);
      ret <<= 8;
      ret |= ((int) serializedCommand[i])&255;
    } 

    // System.out.println(" -> " + ret);
    return ret;
  } 

  /**
   * Method declaration
   * 
   * @return
   * 
   * @see
   */
  public static byte[] getSerializedOk() {
    DeliverableDataOutputStream ddos = new DeliverableDataOutputStream(null);
    try {
      ddos.serializeCommand(new Command(OK));
    } 
    catch (LEAPSerializationException lse) {
      lse.printStackTrace();

      return null;
    } 

    return ddos.getSerializedByteArray();
  } 

}

