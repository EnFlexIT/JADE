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
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Broadcom Eireann Research.
 * Copyright (C) 2001 Siemens AG.
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

package jade.imtp.leap;

import jade.core.AgentContainer;
import jade.core.AgentProxy;
import jade.core.AID;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;
import jade.lang.acl.ACLMessage;

/**
 * Class declaration
 * 
 * @author LEAP
 */
class RemoteContainerProxy implements AgentProxy {

  protected AgentContainer remoteContainer;
  protected AID            receiver;

  /**
   * Constructor declaration
   * 
   * @param ac
   * @param recv
   * 
   */
  public RemoteContainerProxy(AgentContainer remoteContainer, AID receiver) {
    this.remoteContainer = remoteContainer;
    this.receiver = receiver;
  }

  /**
   * Method declaration
   * 
   * @return
   * 
   * @see
   */
  public AgentContainer getRemoteContainer() {
    return remoteContainer;
  } 

  /**
   * Method declaration
   * 
   * @return
   * 
   * @see
   */
  public AID getReceiver() {
    return receiver;
  } 

  /**
   * @param msg
   * @throws NotFoundException
   */
  public void dispatch(ACLMessage message) throws NotFoundException, UnreachableException {
    try {
      remoteContainer.dispatch(message, receiver);
    } 
    catch (IMTPException imtpe) {
      throw new UnreachableException("Unreachable remote object. "+imtpe.getMessage());
    } 
  } 

  /**
   * @throws UnreachableException
   */
  public void ping() throws UnreachableException {
    try {
      remoteContainer.ping(false);
    } 
    catch (IMTPException imtpe) {
      throw new UnreachableException("Unreachable remote object");
    } 
  } 

}

