/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
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


package jade.core;

import jade.util.leap.LEAPProperties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.net.*;
import java.util.Hashtable;

/**
 * This class allows retrieving configuration-dependent classes.
 * 
 * @author  Federico Bergenti
 * @author  Giovanni Caire - TILAB
 * @author  Giovanni Rimassa - Universita` di Parma
 * @version 1.0, 22/11/00
 * 
 * @see jade.util.leap.LEAPProperties
 */
public class ProfileImpl extends Profile {

  private LEAPProperties  props = new LEAPProperties();


  private MainContainer   myMain = null;
  private IMTPManager     myIMTPManager = null;
  private acc             myACC = null;
  private MobilityManager myMobilityManager = null;

  /**
   * Creates a Profile implementation with the default configuration
   */
  public ProfileImpl() {
    props = new LEAPProperties();

    try {
      // Set default values
      String host = InetAddress.getLocalHost().getHostName();
      props.setProperty(MAIN, "true");
      props.setProperty(MAIN_PROTO, "rmi");
      props.setProperty(MAIN_HOST, host);
      props.setProperty(MAIN_PORT, "1099");
      props.setProperty(PLATFORM_ID, host+":1099/JADE");
    } 
    catch (UnknownHostException uhe) {
      uhe.printStackTrace();
    } 
    catch (IOException ioe) {
      ioe.printStackTrace();
    } 
  }

 	public ProfileImpl(String host, String port, String platformID) {
     	this(); // Call default constructor
     	props.setProperty(MAIN, "false");
     	if(host != null)
       		props.setProperty(MAIN_HOST, host);
     	if(port != null)
       		props.setProperty(MAIN_PORT, port);
     	if(platformID != null)
       		props.setProperty(PLATFORM_ID, platformID);
     	else {
       		String h = props.getProperty(MAIN_HOST);
       		String p = props.getProperty(MAIN_PORT);
       		props.setProperty(PLATFORM_ID, h + ":" + p + "/JADE");
     	}
 	}
      
  /**
   * Method declaration
   *
   * @param key
   * @param value
   *
   * @see
   */
  public void putProperty(String key, String value) {
    props.put(key, value);
  } 

  /**
   * Method declaration
   *
   * @param key
   * @param value
   *
   * @see
   */
  public void putSpecifierList(String key, List value) {
    props.put(key, value);
  } 

  /**
   */
  protected MainContainer getMain() throws ProfileException {
    if (myMain == null) {
      createMain();
    } 

    return myMain;
  } 

  /**
   */
  protected IMTPManager getIMTPManager() throws ProfileException {
    if (myIMTPManager == null) {
      createIMTPManager();
    } 

    return myIMTPManager;
  } 

  /**
   */
  protected acc getAcc() throws ProfileException {
    if (myACC == null) {
      createACC();
    } 

    return myACC;
  } 

  /**
   */
  protected MobilityManager getMobilityManager() throws ProfileException {
    if (myMobilityManager == null) {
      createMobilityManager();
    } 

    return myMobilityManager;
  } 

  /**
   * Method declaration
   *
   * @throws ProfileException
   *
   * @see
   */
  private void createMain() throws ProfileException {
    try {
      String isMain = props.getProperty(MAIN);
      if (isMain == null || isMain.equalsIgnoreCase("true")) {
        // The real Main
        myMain = new MainContainerImpl(this);

        myIMTPManager.remotize(myMain);
      } 
      else {
        // A proxy to the main
        myMain = new MainContainerProxy(this, myIMTPManager.getMain());
      } 
    } 
    catch (IMTPException imtpe) {
      throw new ProfileException(imtpe.getMessage());
    } 
  } 

  /**
   * Method declaration
   *
   * @throws ProfileException
   *
   * @see
   */
  private void createIMTPManager() throws ProfileException {
    // Use the RMI IMTP by default
    String className = new String("jade.imtp.rmi.RMIIMTPManager");

    try {
      myIMTPManager = (IMTPManager) Class.forName(className).newInstance();
    } 
    catch (Exception e) {
      e.printStackTrace();

      throw new ProfileException("Error loading IMTPManager class "+className);
    } 
  } 

  /**
   * Method declaration
   *
   * @throws ProfileException
   *
   * @see
   */
  private void createACC() throws ProfileException {
    // Use the Full ACC by default
    String className = new String("jade.core.FullAcc");
    try {
      myACC = (acc) Class.forName(className).newInstance();
    } 
    catch (Exception e) {
      throw new ProfileException("Error loading acc class "+className);
    } 
  } 

  /**
   * Method declaration
   *
   * @throws ProfileException
   *
   * @see
   */
  private void createMobilityManager() throws ProfileException {
    // Use the RealMobilityManager by default
    String className = new String("jade.core.RealMobilityManager");
    try {
      myMobilityManager = (MobilityManager) Class.forName(className).newInstance();
    } 
    catch (Exception e) {
      throw new ProfileException("Error loading MobilityManager class "+className);
    } 
  } 

  /**
   * Retrieve a String value from the configuration properties.
   * If no parameter corresponding to the specified key is found,
   * null is returned.
   * @param key The key identifying the parameter to be retrieved
   * among the configuration properties.
   */
  public String getParameter(String key) throws ProfileException {
    return props.getProperty(key);
  } 

  /**
   * Retrieve a list of Specifiers from the configuration properties.
   * Agents, MTPs and other items are specified among the configuration
   * properties in this way.
   * If no list of Specifiers corresponding to the specified key is found,
   * an empty list is returned.
   * @param key The key identifying the list of Specifiers to be retrieved
   * among the configuration properties.
   */
  public List getSpecifiers(String key) throws ProfileException {
    List l = (List)props.get(key);
    if(l == null)
      l = new ArrayList(0);
    return l;
  } 

}

