/**
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
 */
package jade.domain;

//#J2ME_EXCLUDE_FILE

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.KBManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;
import jade.proto.SubscriptionResponder;

import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.leap.Properties;
import jade.util.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.Date;


/**
 * @author Elisabetta Cortese - TILab
 */
class DFDBKB extends DBKB {
  
  private static final int MAX_REGISTER_WITHOUT_CLEAN = 100;  
  private Logger logger;

  // Number of registrations after the last lease-time-cleanup
	private int regsCnt = 0;
	
	// prepared SQL statements
	private PreparedStatement stm_selAgentAddresses;
	private PreparedStatement stm_selAgentResolverAID;
	private PreparedStatement stm_selAgentUserDefSlot;
	private PreparedStatement stm_selLease;
	private PreparedStatement stm_selProtocols;
	private PreparedStatement stm_selLanguages;
	private PreparedStatement stm_selOntologies;
	private PreparedStatement stm_selServices;
  private PreparedStatement stm_selServiceProtocols;
  private PreparedStatement stm_selServiceLanguages;
  private PreparedStatement stm_selServiceOntologies;
  private PreparedStatement stm_selServiceProperties;
  private PreparedStatement stm_selExpiredDescr;
  
  private PreparedStatement stm_insAgentDescr;
  private PreparedStatement stm_insAgentAddress;
  private PreparedStatement stm_insAgentUserDefSlot;
  private PreparedStatement stm_insAgentResolverAID;
  private PreparedStatement stm_insLanguage;
  private PreparedStatement stm_insOntology;
  private PreparedStatement stm_insProtocol;
  private PreparedStatement stm_insService;
  private PreparedStatement stm_insServiceProtocol;
  private PreparedStatement stm_insServiceOntology;
  private PreparedStatement stm_insServiceLanguage;
  private PreparedStatement stm_insServiceProperty;
  private PreparedStatement stm_delAgentDescr;
  private PreparedStatement stm_delAgentUserDefSlot;
  private PreparedStatement stm_delAgentResolver;
  private PreparedStatement stm_delAgentAddress;
  
  private PreparedStatement stm_selDescrId; 
  private PreparedStatement stm_selServiceId;
  private PreparedStatement stm_delService;
  private PreparedStatement stm_delLanguage;
  private PreparedStatement stm_delProtocol;
  private PreparedStatement stm_delOntology;
  
  private PreparedStatement stm_delServiceLanguage;
  private PreparedStatement stm_delServiceOntology;
  private PreparedStatement stm_delServiceProtocol;
  private PreparedStatement stm_delServiceProperty;
	
 
	/**
   * Constructor
	 * @param maxResultLimit internal limit for the number of maximum search results.
	 * @param drv database driver
	 * @param url database URL
	 * @param user database user name
	 * @param passwd database password
	 * @throws SQLException an error occured while opening a connection to the database
	 */
	public DFDBKB(int maxResultLimit, String drv, String url, String user, String passwd) throws SQLException {
		super(drv, url, user, passwd, maxResultLimit);
	}
	
  /**
   * Initializes all used SQL statements, the DB tables and the logging
   */
	protected void setup() throws SQLException {
		logger = Logger.getMyLogger(this.getClass().getName());		
    conn.setAutoCommit(false); // deactivate auto commit for better performance
    dropDFTables();
    createDFTables();
    initPrepStmts();
		clean();
	}
	
	/**
	 * Initializes all used SQL  prepared statements
	 * @throws SQLException
	 */
	protected void initPrepStmts() {
    
	  try {
	    // select statements
	    stm_selAgentAddresses = conn.prepareStatement("SELECT address FROM agentaddress WHERE aid = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	    stm_selAgentResolverAID = conn.prepareStatement("SELECT resolveraid FROM agentresolver WHERE aid = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	    stm_selAgentUserDefSlot = conn.prepareStatement("SELECT slotkey, slotval FROM agentuserdefslot WHERE aid = ?");
	    stm_selLease = conn.prepareStatement("SELECT id, lease FROM dfagentdescr WHERE aid = ?");
	    stm_selProtocols = conn.prepareStatement("SELECT protocol FROM protocol WHERE descrid = ?");
	    stm_selLanguages = conn.prepareStatement("SELECT language FROM language WHERE descrid = ?");
	    stm_selOntologies = conn.prepareStatement("SELECT ontology FROM ontology WHERE descrid = ?");
	    stm_selServices = conn.prepareStatement("SELECT id, sname, stype, sownership FROM service WHERE descrid = ?");
	    stm_selServiceProtocols = conn.prepareStatement("SELECT protocol FROM serviceprotocol WHERE serviceid = ?");
	    stm_selServiceLanguages = conn.prepareStatement("SELECT ontology FROM serviceontology WHERE serviceid = ?");
	    stm_selServiceOntologies = conn.prepareStatement("SELECT language FROM servicelanguage WHERE serviceid = ?");
	    stm_selServiceProperties = conn.prepareStatement("SELECT propkey, propval FROM serviceproperty WHERE serviceid = ?");
      stm_selDescrId = conn.prepareStatement("SELECT id FROM dfagentdescr WHERE aid = ?"); 
      stm_selServiceId = conn.prepareStatement("SELECT id FROM service WHERE descrid = ?");
      stm_selExpiredDescr = conn.prepareStatement("SELECT aid FROM dfagentdescr WHERE lease < ? AND lease <> '-1'");
      
	    // insert statements
	    stm_insAgentDescr = conn.prepareStatement("INSERT INTO dfagentdescr VALUES (?, ?, ?)");
	    stm_insAgentAddress = conn.prepareStatement("INSERT INTO agentaddress VALUES (?, ?, ?)");
	    stm_insAgentUserDefSlot = conn.prepareStatement("INSERT INTO agentuserdefslot VALUES (?, ?, ?, ?)");
	    stm_insAgentResolverAID = conn.prepareStatement("INSERT INTO agentresolver VALUES (?, ?, ?)");
	    stm_insLanguage = conn.prepareStatement("INSERT INTO language VALUES (?, ?)");
	    stm_insOntology = conn.prepareStatement("INSERT INTO ontology VALUES (?, ?)");
	    stm_insProtocol = conn.prepareStatement("INSERT INTO protocol VALUES (?, ?)");
	    stm_insService = conn.prepareStatement("INSERT INTO service VALUES (?, ?, ?, ?, ?)");
	    stm_insServiceProtocol = conn.prepareStatement("INSERT INTO serviceprotocol VALUES (?, ?)");
	    stm_insServiceOntology = conn.prepareStatement("INSERT INTO serviceontology VALUES (?, ?)");
	    stm_insServiceLanguage = conn.prepareStatement("INSERT INTO servicelanguage VALUES (?, ?)");
	    stm_insServiceProperty = conn.prepareStatement("INSERT INTO serviceproperty VALUES (?, ?, ?)");
    
	    // delete statements
	    stm_delAgentDescr = conn.prepareStatement("DELETE FROM dfagentdescr WHERE aid = ?");
	    stm_delAgentUserDefSlot = conn.prepareStatement("DELETE FROM agentuserdefslot WHERE aid = ?");
	    stm_delAgentResolver = conn.prepareStatement("DELETE FROM agentresolver WHERE aid = ?");
	    stm_delAgentAddress = conn.prepareStatement("DELETE FROM agentaddress WHERE aid = ?");
	    stm_delLanguage = conn.prepareStatement("DELETE FROM language WHERE descrid = ?");
	    stm_delProtocol = conn.prepareStatement("DELETE FROM protocol WHERE descrid = ?");
	    stm_delOntology = conn.prepareStatement("DELETE FROM ontology WHERE descrid = ?");
	    stm_delService = conn.prepareStatement("DELETE FROM service WHERE descrid = ?"); 
	    stm_delServiceLanguage = conn.prepareStatement("DELETE FROM servicelanguage WHERE serviceid = ?");
	    stm_delServiceOntology = conn.prepareStatement("DELETE FROM serviceontology WHERE serviceid = ?");
	    stm_delServiceProtocol = conn.prepareStatement("DELETE FROM serviceprotocol WHERE serviceid = ?");
	    stm_delServiceProperty = conn.prepareStatement("DELETE FROM serviceproperty WHERE serviceid = ?");
   
    } catch (SQLException e) {
      if(logger.isLoggable(Logger.SEVERE)) 
        logger.log(Logger.SEVERE, "Error preparing SQL statements for DF", e);
    }
  }
	
	/**
	 * Returns a global unique identifier
	 */
	protected String getGUID() {
	    String localIPAddress;
	    try {
	      localIPAddress = InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
	      localIPAddress = "localhost";
	    } 
	    UID uid = new UID();
	    return localIPAddress + ":" + uid;
	}
	         
  /**
   * Drops a DB table or does nothing if the table doesn't exist.
   */                
	protected void dropTable(Statement stmt, String tableName) {
	   try {
       stmt.execute("DROP TABLE " + tableName); 
       conn.commit();
    } catch (SQLException e) {
       // table doesn't exist
    }
	}
  
  /**
   * Drops all existing DB tables used by the DF.
   */
  protected void dropDFTables() throws SQLException {    
    Statement stmt = conn.createStatement();

    dropTable(stmt, "subscription"); 
    dropTable(stmt, "serviceprotocol");
    dropTable(stmt, "serviceontology");
    dropTable(stmt, "servicelanguage");
    dropTable(stmt, "serviceproperty");
    dropTable(stmt, "service");
    dropTable(stmt, "language");
    dropTable(stmt, "ontology");
    dropTable(stmt, "protocol");
    dropTable(stmt, "agentuserdefslot");
    dropTable(stmt, "agentresolver");   
    dropTable(stmt, "agentaddress");
    dropTable(stmt, "dfagentdescr"); 
    
    stmt.close();
  }
	
	/**
	 * Creates a new DB table
	 * 
	 * @param stmt SQL statement object to use for the creation
	 * @param name name of the table
	 * @param entries array of column and constraint specifications
	 * @throws SQLException If the table cannot be created
	 */
  protected void createTable(Statement stmt, String name, String[] entries) throws SQLException {
    String sql = "CREATE TABLE " + name + " (";
    for (int i = 0; i < entries.length; i++) {
      sql += entries[i];
      if (i < entries.length - 1)
        sql += ", ";
      else
        sql += ")";
    }
    stmt.executeUpdate(sql);
    conn.commit();
  }

  /**
   * Adds explicit indices to the database to speed up queries
   * @param stmt SQL statement object to use
   */
  protected void createIndices(Statement stmt) {
    try {
      stmt.execute("CREATE INDEX dfagentDescrIdx ON dfagentdescr( aid )");
      stmt.execute("CREATE INDEX leaseIdx ON dfagentdescr( lease )");
      stmt.execute("CREATE INDEX agentAddressIdx ON agentaddress( aid )");
      stmt.execute("CREATE INDEX agentResolverIdx ON agentresolver( aid )");
      stmt.execute("CREATE INDEX agentUserdefslotIdx ON agentuserdefslot( aid )");
      stmt.execute("CREATE INDEX serviceLanguageIdx ON servicelanguage( serviceid )"); 
      stmt.execute("CREATE INDEX serviceProtocolIdx ON serviceprotocol( serviceid )"); 
      stmt.execute("CREATE INDEX serviceOntologyIdx ON serviceontology( serviceid )"); 
      stmt.execute("CREATE INDEX servicePropertyIdx ON serviceproperty( serviceid )");
      stmt.execute("CREATE INDEX ontologyIdx ON ontology( descrid )");
      stmt.execute("CREATE INDEX protocolIdx ON ontology( descrid )");
      stmt.execute("CREATE INDEX languageIdx ON ontology( descrid )");
      conn.commit();
      
    } catch (SQLException e) {
      if(logger.isLoggable(Logger.FINE)) 
        logger.log(Logger.FINE, "Indices for DF tables couldn't be created", e);
    }
  }
  
  
	/**
	 * Creates the proper DB tables.
	 */
  protected void createDFTables() {
		Statement stmt = null;

		try { 
	 		 	stmt = conn.createStatement();
	 		 	
        // Tables for DF registrations      
	 		  createTable(stmt, "dfagentdescr", new String[] {
          "id    VARCHAR(255)  NOT NULL",  
          "aid		 VARCHAR(255)  NOT NULL",  
	 		 	  "lease   	 VARCHAR(20)  NOT NULL",   
	 		 	  "PRIMARY KEY( id )"}); 
         
        createTable(stmt, "agentaddress", new String[] {
	  		  "id	VARCHAR(255)", 
	        "aid		 VARCHAR(255)  NOT NULL",   
	        "address    VARCHAR(255)  NOT NULL", 
	        "PRIMARY KEY( id )"});
        
	 		 	createTable(stmt, "agentresolver", new String[] {
	  		  "id	VARCHAR(255)",
	  		  "aid		 VARCHAR(255)  NOT NULL",
	        "resolveraid 	 VARCHAR(255)  NOT NULL",
	      	"PRIMARY KEY( id )"});	
        
	 		 	createTable(stmt, "agentuserdefslot", new String[] {
		  		 "id	VARCHAR(255)",
		  		 "aid	VARCHAR(255)	NOT NULL",
		       "slotkey	VARCHAR(255)	NOT NULL",
		       "slotval	VARCHAR(255)	NOT NULL",
		       "PRIMARY KEY( id )"});	 		 	
        
	 		  createTable(stmt, "protocol", new String[] {
          "descrid		VARCHAR(255)  NOT NULL",
	        "protocol   VARCHAR(32)	      NOT NULL", 
	        "PRIMARY KEY( descrid, protocol )",
          "FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"});

	 		  createTable(stmt, "ontology", new String[] {
	        "descrid		 VARCHAR(255)  NOT NULL",
	        "ontology 	 VARCHAR(32)  NOT NULL",
	        "PRIMARY KEY( descrid, ontology )",
          "FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"});
	 		  
	 		  createTable(stmt, "language", new String[] {
	        "descrid		 VARCHAR(255)  NOT NULL",
	        "language   VARCHAR(32)  NOT NULL",
	        "PRIMARY KEY( descrid, language )",
          "FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"});
	 		  
	 		  createTable(stmt, "service", new String[] {
	  		 "id	VARCHAR(255)",
	  		 "descrid		 VARCHAR(255)  NOT NULL",
	       "sname   VARCHAR(255)  NOT NULL",
	       "stype       VARCHAR(64)  NOT NULL",
	       "sownership  VARCHAR(64)  NOT NULL",
	       "PRIMARY KEY( id )",
         "FOREIGN KEY( descrid ) REFERENCES dfagentdescr( id )"});
	 		  
	 		  createTable(stmt, "serviceprotocol", new String[] {
	       "serviceid		 VARCHAR(255)  NOT NULL",
	       "protocol   VARCHAR(32)  NOT NULL",
	       "PRIMARY KEY( serviceid, protocol )",
         "FOREIGN KEY( serviceid ) REFERENCES service( id )"});
	 		  
	 		  createTable(stmt, "serviceontology", new String[] {
	       "serviceid		 VARCHAR(255)  NOT NULL",
	       "ontology   VARCHAR(32)  NOT NULL",
	       "PRIMARY KEY( serviceid, ontology )",
         "FOREIGN KEY( serviceid ) REFERENCES service( id )"});
	 		  
	 		  createTable(stmt, "servicelanguage", new String[] {
	  		  "serviceid		 VARCHAR(255)  NOT NULL",
	        "language   VARCHAR(32)  NOT NULL",
	        "PRIMARY KEY( serviceid, language )",
          "FOREIGN KEY( serviceid ) REFERENCES service( id )"});
	 		  
	 		  createTable(stmt, "serviceproperty", new String[] {
	  		  "serviceid		 VARCHAR(255)  NOT NULL",
	        "propkey  VARCHAR(32)  NOT NULL",
	        "propval  VARCHAR(255)  NOT NULL",
	        "PRIMARY KEY( serviceid, propkey )",
          "FOREIGN KEY( serviceid ) REFERENCES service( id )"});
	 		  
				// Tables for subscriptions registrations
	 		  createTable(stmt, "subscription", new String[] { 
	        "conversationid	 VARCHAR(255) NOT NULL"   ,
	        "aclm1	 	 VARCHAR(255) NOT NULL" ,
	        "aclm2	 	 VARCHAR(255)" ,
	        "aclm3	 	 VARCHAR(255)" ,
	        "aclm4	 	 VARCHAR(255)" ,
	        "aclm5	 	 VARCHAR(255)" ,
	        "aclm6	 	 VARCHAR(255)" ,
	        "aclm7	 	 VARCHAR(255)" ,
	        "aclm8	 	 VARCHAR(255)" ,
	        "PRIMARY KEY( conversationid )"});

        createIndices(stmt);
        
	 		  //DEBUG
	 		  if(logger.isLoggable(Logger.FINE))
	 		    logger.log(Logger.FINE,"Tables correctly created");
			
 		} catch(SQLException se) {

			if(logger.isLoggable(Logger.SEVERE)) 
				logger.log(Logger.SEVERE, "Error creating tables for DF", se);

       try {
          conn.rollback();
        } catch (SQLException re) {
          if(logger.isLoggable(Logger.SEVERE))
            logger.log(Logger.SEVERE,"Rollback for incomplete creation of DF tables failed.");
        }
		}
		finally {
			if (stmt != null) {
				try{
					stmt.close();
				}
				catch(SQLException see) {
					see.printStackTrace();
				}
			}
		}
	}

  /**
   * Builds an error message for a <code>BatchUpdateException</code>
   */
  private String getBatchUpdateErroMsg(BatchUpdateException e) {
    StringBuffer msg = new StringBuffer("SQLException: " + e.getMessage() + "\n");
    msg.append("SQLState:  " + e.getSQLState() + "\n");
    msg.append("Message:  " + e.getMessage() + "\n");
    msg.append("Vendor:  " + e.getErrorCode() + "\n");
    msg.append("Update counts: ");

    int [] updateCounts = e.getUpdateCounts();
    for (int i = 0; i < updateCounts.length; i++) {
      msg.append(updateCounts[i] + "   ");
    }
    return msg.toString();
  }
  
  /**
   * Stores the information of an AID of a resolver
   */
  private void saveResolverAID(AID aid, AID resolverAid) throws SQLException {
    saveAID(resolverAid);
    stm_insAgentResolverAID.setString(1, getGUID());
    stm_insAgentResolverAID.setString(2, aid.getName());
    stm_insAgentResolverAID.setString(3, resolverAid.getName());
    stm_insAgentResolverAID.addBatch(); 
  }
  
  /**
   * Stores the information of an AID in the database
   */
  private void saveAID(AID aid) throws SQLException {
    String name = aid.getName();
    
    // Addresses
    Iterator iter = aid.getAllAddresses();
    while( iter.hasNext()){
      stm_insAgentAddress.setString(1, getGUID());
      stm_insAgentAddress.setString(2, name);
      stm_insAgentAddress.setString(3, (String)iter.next());
      stm_insAgentAddress.addBatch();
    }
    stm_insAgentAddress.executeBatch();
    
    // User defined slots
    Properties props = aid.getAllUserDefinedSlot();
    if (props.size() > 0) {
      stm_insAgentUserDefSlot.clearBatch();
      java.util.Iterator pIter = props.entrySet().iterator();
      while (pIter.hasNext()) {
        Map.Entry entry = (Map.Entry)pIter.next();
        stm_insAgentUserDefSlot.setString(1, getGUID());
        stm_insAgentUserDefSlot.setString(2, name);
        stm_insAgentUserDefSlot.setString(3, (String)entry.getKey());
        stm_insAgentUserDefSlot.setString(4, (String)entry.getValue());
        stm_insAgentUserDefSlot.addBatch();
      }
      stm_insAgentUserDefSlot.executeBatch();
    }
    
    // Resolvers
    iter = aid.getAllResolvers();
    if (iter.hasNext()) {
      stm_insAgentResolverAID.clearBatch();
      while(iter.hasNext()){
        AID resolverAID = (AID)iter.next();
        saveResolverAID(aid, resolverAID);
      }  
      stm_insAgentResolverAID.executeBatch();
    }
  }
  
  /**
   * Stores a collection of services for a specific description Id in the database
   * @param descrId id of the DFD these services belong to
   * @param iter iterator for a collection of <code>ServiceDescription</code> instances
   * @throws SQLException
   */
  private void saveServices(String descrId, Iterator iter) throws SQLException {
    if (iter.hasNext()) {
      stm_insService.clearBatch();
      stm_insServiceOntology.clearBatch();
      stm_insServiceOntology.clearBatch();
      stm_insServiceLanguage.clearBatch();
      stm_insServiceProperty.clearBatch();
    
      while(iter.hasNext()){
        ServiceDescription service = (ServiceDescription)iter.next();
        String serviceId = getGUID();
        stm_insService.clearParameters();
        stm_insService.setString(1, serviceId);
        stm_insService.setString(2, descrId);
        stm_insService.setString(3, service.getName());
        stm_insService.setString(4, service.getType());
        stm_insService.setString(5, service.getOwnership());
        stm_insService.addBatch();
      
        // Service - Protocols
        Iterator iterS = service.getAllProtocols();
        while(iterS.hasNext()){
          stm_insServiceProtocol.setString(1, serviceId);
          stm_insServiceProtocol.setString(2, (String)iterS.next());
          stm_insServiceProtocol.addBatch();
        }

        // Service - Ontologies
        iterS = service.getAllOntologies();
        while(iterS.hasNext()){
          stm_insServiceOntology.setString(1, serviceId);
          stm_insServiceOntology.setString(2, (String)iterS.next());
          stm_insServiceOntology.addBatch();
        }
      
        // Service - Languages
        iterS = service.getAllLanguages();
        while(iterS.hasNext()){
          stm_insServiceLanguage.setString(1, serviceId);
          stm_insServiceLanguage.setString(2, (String)iterS.next());
          stm_insServiceLanguage.addBatch();
        }
      
        // Service - Properties
        iterS = service.getAllProperties();
        while(iterS.hasNext()){
          Property prop = (Property)iterS.next();
          stm_insServiceProperty.setString(1, serviceId);
          stm_insServiceProperty.setString(2, prop.getName());
          stm_insServiceProperty.setString(3, prop.getValue().toString());
          stm_insServiceProperty.addBatch();
        }
      }
      stm_insService.executeBatch();
      stm_insServiceProtocol.executeBatch();
      stm_insServiceOntology.executeBatch();
      stm_insServiceLanguage.executeBatch();
      stm_insServiceProperty.executeBatch();
    }
  }
  
	/**
	 *  Insert a new DFD object.
	 *  @return the previous DFD (if any) corresponding to the same AID
	 */
	public Object insert(Object name, Object fact) {
		
    DFAgentDescription dfd = (DFAgentDescription) fact;
    AID agentAID = dfd.getName();
    String agentName = agentAID.getName();
		DFAgentDescription dfdToReturn = null;
    String batchErrMsg = "";
    
    try {

  		// -- Remove the previous DFD if any
  		dfdToReturn = (DFAgentDescription) remove(dfd.getName());
      
      // -- add new DFD
      
      // DF Agent Description
      Date leaseTime = dfd.getLeaseTime();
      long lt = (leaseTime != null ? leaseTime.getTime() : -1);
      String descrId = getGUID();
     
      stm_insAgentDescr.setString(1, descrId);
      stm_insAgentDescr.setString(2, agentName);
      stm_insAgentDescr.setString(3, String.valueOf(lt));
      stm_insAgentDescr.executeUpdate();
      
      
      // AID
      saveAID(agentAID);
      
      // Languages
      Iterator iter = dfd.getAllLanguages();
      if (iter.hasNext()) {
        stm_insLanguage.clearBatch();
        while(iter.hasNext()){
          stm_insLanguage.setString(1, descrId);
          stm_insLanguage.setString(2, (String)iter.next());
          stm_insLanguage.addBatch();
        }
        stm_insLanguage.executeBatch();
      }
      
      // Ontologies
      iter = dfd.getAllOntologies();
      if (iter.hasNext()) {
        stm_insOntology.clearBatch();
        while(iter.hasNext()){
          stm_insOntology.setString(1, descrId);
          stm_insOntology.setString(2, (String)iter.next());
          stm_insOntology.addBatch();
        }
        stm_insOntology.executeBatch();
      }
      
      // Protocols
      iter = dfd.getAllProtocols();
      if (iter.hasNext()) {
        stm_insProtocol.clearBatch();
        while(iter.hasNext()){
          stm_insProtocol.setString(1, descrId);
          stm_insProtocol.setString(2, (String)iter.next());
          stm_insProtocol.addBatch();
          stm_insService.executeBatch();
        }
        stm_insProtocol.executeBatch();
      }
      
      // Services
      saveServices(descrId, dfd.getAllServices());    
     
  		regsCnt++;
  		// clear outdated entries after a certain number of new registrations
  		if(regsCnt > MAX_REGISTER_WITHOUT_CLEAN){
  			clean();
  			regsCnt = 0;
  		}
      
      conn.commit();
      
    } catch (BatchUpdateException bue) {
       batchErrMsg = "\n" + getBatchUpdateErroMsg(bue);

    } catch (Exception e) {
      if(logger.isLoggable(Logger.SEVERE))
        logger.log(Logger.SEVERE,"Error inserting DFD for agent "+dfd.getName()+batchErrMsg, e); 
    
      try {
        conn.rollback();
      } catch (SQLException re) {
        if(logger.isLoggable(Logger.SEVERE))
          logger.log(Logger.SEVERE,"Rollback for incomplete insertion of DFD for agent "+dfd.getName() + " failed.");
      }
    
    } 
  
    return dfdToReturn;
	}

	/**
	   Remove the DFD object corresponding to the indicated AID.
	   @return the removed DFD (if any) 
	 */
	protected Object remove(Object name) {
		AID agentAID = (AID) name;
		String n = agentAID.getName();
		
		DFAgentDescription dfd = getDFD(n);
		if (dfd != null) {
        remove(n);
		}
		
		return dfd;
	}
	
	/**
	   Retrieve the DFDs matching the given template
	 */
	public List search(Object template, int maxResult){
		List matchingAIDs = new ArrayList();

		// Get the names of all DFDs matching the template
		Statement s = null;
		ResultSet rs = null;
		String select = createSelect((DFAgentDescription) template);
		
		try{
			s = conn.createStatement();
      s.setMaxRows(maxResult);
      s.setFetchSize(maxResult);
      
			rs = s.executeQuery(select);
			int cnt = 0;
			while(rs.next() && (cnt < maxResult)) { 
    		String aidS = rs.getString("aid");
        matchingAIDs.add(aidS);
        cnt++;
  		}
      
      
		} catch(SQLException se){
			if(logger.isLoggable(Logger.SEVERE)) {
				logger.log(Logger.SEVERE,"Error searching for DFDs matching template. DB operation: "+select, se);
      }
    }
		finally {
			try {
				if (rs != null) rs.close();
				if (s != null) s.close();
			}
			catch (SQLException se1) {
				se1.printStackTrace();
			}
		}
		
		// For each matching AID reconstruct the complete DFD
		List dfds = new ArrayList(matchingAIDs.size());
		Iterator it = matchingAIDs.iterator();
		while (it.hasNext()) {
			dfds.add(getDFD((String) it.next()));
		}
  
		return dfds;
	}
	
	/**
	 * Reconstructs an AID object corresponding to the given AID name
	 * @throws SQLException
	 */
	private AID getAID(String aidN) throws SQLException {
		
			ResultSet rs = null;
			AID id = new AID(aidN, AID.ISGUID);
			
			// AID addresses
			stm_selAgentAddresses.setString(1, aidN);
			rs = stm_selAgentAddresses.executeQuery();
			while(rs.next()){
				id.addAddresses(rs.getString(1));
			}
			
			// AID resolvers 
			ArrayList resolvers = new ArrayList();
      stm_selAgentResolverAID.setString(1, aidN);
			rs = stm_selAgentResolverAID.executeQuery();
			while(rs.next()){
				resolvers.add(rs.getString(1));
			}
      
      Iterator iter = resolvers.iterator();
      while (iter.hasNext()) {
        id.addResolvers(getAID((String)iter.next()));
      }
			
			// AID User defined slots
			stm_selAgentUserDefSlot.setString(1, aidN);
			rs = stm_selAgentUserDefSlot.executeQuery();
			while(rs.next()) {
			    String key = rs.getString("slotkey");
			    String value = rs.getString("slotval");
			    id.addUserDefinedSlot(key, value);
			}
			
			return id;
	}
	
	/**
	   Reconstruct the DFD corresponding to the given AID name (if any)
	 */
	private DFAgentDescription getDFD(String aidN){
		DFAgentDescription dfd = null;
		AID id = null;
		
		String select = null;
		Statement s = null;
		ResultSet rs = null;
		String descrId = null;
    
		try{
			s = conn.createStatement();
			
			// Check if there is a DFD corresponding to aidN and get lease time
			stm_selLease.setString(1, aidN);
			rs = stm_selLease.executeQuery();
			if (rs.next()) {
				dfd = new DFAgentDescription();
				id = getAID(aidN);
				dfd.setName(id);
				String sLease = rs.getString("lease");
        descrId = rs.getString("id");
				long lease = Long.parseLong(sLease);
				if (lease != -1) {
					dfd.setLeaseTime(new Date(lease));
				}
			}
			else {
				return null;
			}
			
			// Protocols
			stm_selProtocols.setString(1, descrId);
			rs = stm_selProtocols.executeQuery();
			while(rs.next()){
				dfd.addProtocols(rs.getString("protocol"));
			}
			// Languages
			stm_selLanguages.setString(1, descrId);
			rs = stm_selLanguages.executeQuery();
			while(rs.next()){
				dfd.addLanguages(rs.getString("language"));
			}
			// Ontologies
			stm_selOntologies.setString(1, descrId);
			rs = stm_selOntologies.executeQuery();
			while(rs.next()){
				dfd.addOntologies(rs.getString("ontology"));
			}
			
			// Services
			stm_selServices.setString(1, descrId);
			rs = stm_selServices.executeQuery();
			while(rs.next()){
				ServiceDescription sd = new ServiceDescription();
				String serviceId = rs.getString("id");
        sd.setName(rs.getString("sname"));
				sd.setType(rs.getString("stype"));
				sd.setOwnership(rs.getString("sownership"));
				
				Statement sS = conn.createStatement();
				String selectS = null;
				ResultSet rsS = null;
        String name = sd.getName();
				
				// Service protocols
        stm_selServiceProtocols.setString(1, serviceId);
				rsS = stm_selServiceProtocols.executeQuery();
        while(rsS.next()){
					sd.addProtocols(rsS.getString("protocol"));
				}	
        
				// Service languages
        stm_selServiceLanguages.setString(1, serviceId);
				rsS = stm_selServiceLanguages.executeQuery();
        while(rsS.next()){
					sd.addOntologies(rsS.getString("ontology"));
				}	
        
				// Service ontologies
        stm_selServiceOntologies.setString(1, serviceId);
				rsS = stm_selServiceOntologies.executeQuery();
        while(rsS.next()){
					sd.addLanguages(rsS.getString("language"));
				}
        
				// Service properties
        stm_selServiceProperties.setString(1, serviceId);
        rsS = stm_selServiceProperties.executeQuery();
				while(rsS.next()){
					Property prop = new Property();
					prop.setName(rsS.getString("propkey"));
					prop.setValue(rsS.getString("propval"));
					sd.addProperties(prop);
				}	
        
        conn.commit();
				rsS.close();
				sS.close();
				dfd.addServices(sd);
			}
		}
		catch(SQLException se){
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,"Error reconstructing DFD for agent "+aidN, se);
			
		}
		finally {
			try {
				if (rs != null) rs.close();			
				if (s != null) s.close();
			}
			catch (SQLException se1) {
				se1.printStackTrace();
			}
		}
		return dfd;
	}
	
  
  /**
   * Deletes the AID corresponding to the indicated agent name
   */
  private void removeAID(String nameAg) throws SQLException {
    stm_delAgentUserDefSlot.setString(1, nameAg);
    stm_delAgentUserDefSlot.execute();
    stm_delAgentResolver.setString(1, nameAg);
    stm_delAgentResolver.execute();
    stm_delAgentAddress.setString(1, nameAg);
    stm_delAgentAddress.execute();
  }
  
  /**
   * Deletes all services corresponding to the indicated description ID
   * @throws SQLException
   */
  private void removeServices(String descrId) throws SQLException {
    ResultSet rs = null;
    stm_selServiceId.setString(1, descrId);
    rs = stm_selServiceId.executeQuery();
    
    while (rs.next()) {;
      String serviceId = rs.getString("id");
    
      stm_delServiceLanguage.setString(1, serviceId);
      stm_delServiceLanguage.addBatch();
    
      stm_delServiceOntology.setString(1, serviceId);
      stm_delServiceOntology.addBatch();
    
      stm_delServiceProtocol.setString(1, serviceId);
      stm_delServiceProtocol.addBatch();
   
      stm_delServiceProperty.setString(1, serviceId);
      stm_delServiceProperty.addBatch();
  
      stm_delService.setString(1, descrId);
      stm_delService.addBatch();
    }
    rs.close();

    stm_delServiceLanguage.executeBatch();
    stm_delServiceOntology.executeBatch();
    stm_delServiceProtocol.executeBatch();
    stm_delServiceProperty.executeBatch();
    stm_delService.executeBatch();
  }
  
	/**
	 *  Delete the DFD object corresponding to the indicated agent name.
	 */
	private void remove(String nameAg) {  		
	
		try {   
      // get description ID
      stm_selDescrId.setString(1, nameAg);
      ResultSet rs = stm_selDescrId.executeQuery();
      
      if (rs.next()) {
        String descrId = rs.getString("id");
        rs.close();
      
        // AID
        removeAID(nameAg);
        
        // services
        removeServices(descrId);
        
        // languages
        stm_delLanguage.setString(1, descrId);
        stm_delLanguage.execute();

        // protocols
        stm_delProtocol.setString(1, descrId);
        stm_delProtocol.execute();
      
        // ontologies
        stm_delOntology.setString(1, descrId);
        stm_delOntology.execute();

        stm_delAgentDescr.setString(1, nameAg);
        stm_delAgentDescr.execute();
      
      } else {
        if(logger.isLoggable(Logger.FINE))
          logger.log(Logger.FINE,"No DF description found to remove for agent " + nameAg);
      }
		}
		catch(SQLException se){

      if(logger.isLoggable(Logger.WARNING))
        logger.log(Logger.WARNING, "Error removing DFD for agent "+nameAg, se);
		}
	}

	
	/**
	   Convert a template DFAgentDescription into the SQL SELECT
	   operation that returns the AID names whose DFAgentDescriptions
	   match the given template.
	 */
	private String createSelect(DFAgentDescription dfdTemplate){
	
		StringBuffer select = new StringBuffer("SELECT dfagentdescr.aid, dfagentdescr.lease FROM dfagentdescr");
		
		List lAs = new ArrayList();
		List lWhere = new ArrayList();
		
		// Name
		AID agentAID = dfdTemplate.getName();
		if(agentAID != null){
			lWhere.add(" dfagentdescr.aid = '"+agentAID.getName()+"'");
		}
		// Lease time
		Date lease = dfdTemplate.getLeaseTime();
		if(lease != null){
			lWhere.add(" dfagentdescr.lease = '-1' OR dfagentdescr.lease > '"+lease.getTime()+"'");
		}
		// Languages
		Iterator iter = dfdTemplate.getAllLanguages();
		int i=0;
		while(iter.hasNext()){
			String tmp = "language"+i;
			lAs.add(", language AS "+tmp);
			lWhere.add(tmp+".language='"+(String)iter.next()+"'");
			lWhere.add(tmp+".aid=dfagentdescr.aid");
			i++;
		}
		// Ontologies
		iter = dfdTemplate.getAllOntologies();
		i = 0;
		while(iter.hasNext()){
			String tmp = "ontology"+i;
			lAs.add(", ontology AS "+tmp);
			lWhere.add(tmp+".ontology='"+(String)iter.next()+"'");
			lWhere.add(tmp+".aid=dfagentdescr.aid");
			i++;
		}
		// Protocols
		iter = dfdTemplate.getAllProtocols();
		i = 0;
		while(iter.hasNext()){
			String tmp = "protocol"+i;
			lAs.add(", protocol AS "+tmp);
			lWhere.add(tmp+".protocol='"+(String)iter.next()+"'");
			lWhere.add(tmp+".aid=dfagentdescr.aid");
			i++;
		}
		// Services
		iter = dfdTemplate.getAllServices();
		i = 0;
		while(iter.hasNext()){
			ServiceDescription service = (ServiceDescription)iter.next();
			String serviceName = service.getName();
			String serviceType = service.getType();
			String serviceOwner = service.getOwnership();
			// Service name, type and ownership
			String tmp = "service"+i;
			lAs.add(", service AS "+tmp);
			if(serviceName != null){
				lWhere.add(tmp+".sname='"+serviceName+"'");
			}
			if(serviceType != null){
				lWhere.add(tmp+".stype='"+serviceType+"'");
			}
			if(serviceOwner != null){
				lWhere.add(tmp+".sownership='"+serviceOwner+"'");
			}
			lWhere.add(tmp+".aid=dfagentdescr.aid");
			i++;

			// Service languages
			Iterator iterS = service.getAllLanguages();
			int j = 0;
			while(iterS.hasNext()){
				String tmp1 = "servicelanguage"+j;
				lAs.add(", servicelanguage AS "+tmp1);
				lWhere.add(tmp1+".language='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".aid=dfagentdescr.aid");
				j++;
			}
			// Service ontologies
			iterS = service.getAllOntologies();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = "serviceontology"+j;
				lAs.add(", serviceontology AS "+tmp1);
				lWhere.add(tmp1+".ontology='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".aid=dfagentdescr.aid");
				j++;
			}
			// Service protocols
			iterS = service.getAllProtocols();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = "serviceprotocol"+j;
				lAs.add(", serviceprotocol AS "+tmp1);
				lWhere.add(tmp1+".protocol='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".aid=dfagentdescr.aid");
				j++;
			}
			// Service properties
			iterS = service.getAllProperties();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = "serviceproperty"+j;
				lAs.add(", serviceproperty AS "+tmp1);
				Property prop = (Property) iterS.next();
				lWhere.add(tmp1+".propkey='"+prop.getName()+"' and "+tmp1+".propval='"+prop.getValue().toString()+"'");
				lWhere.add(tmp1+".aid=dfagentdescr.aid");
				j++;
			}			
		}
		
		// Concatenate all AS
		iter = lAs.iterator();
		while (iter.hasNext()) {
			select.append((String) iter.next());
		}
		// Concatenate all WHERE
		if (lWhere.size() > 0) {
			select.append(" WHERE ");
		}
		iter = lWhere.iterator();
		i = 0;
		while (iter.hasNext()) {
			if(i > 0) {
				select.append(" and ");
			}
			select.append((String) iter.next());
			++i;
		}
    
		return select.toString();
	}

	////////////////////////////////////////
	// DB cleaning methods
	////////////////////////////////////////
	
	/**
	   Removes DF registrations and subscriptions whose lease time 
	   has expired.
	   This method is called at startup and each MAX_REGISTER_WITHOUT_CLEAN
	   registrations.
	 */
	private void clean(){
		cleanExpiredRegistrations();
		cleanExpiredSubscriptions();
	}

	/**
	   Removes DF registrations whose lease time has expired.
	 */
	private void cleanExpiredRegistrations(){
		Statement s = null;
		ResultSet rs = null;
		long currTime = System.currentTimeMillis();
		try{
			List l = new ArrayList();
			stm_selExpiredDescr.setString(1, String.valueOf(currTime));
      rs = stm_selExpiredDescr.executeQuery();
			while(rs.next()){
				remove(rs.getString("aid"));
			}
		}
		catch(SQLException se){
      if(logger.isLoggable(Logger.WARNING))
        logger.log(Logger.WARNING, "Error cleaning expired DF registrations", se);
		}
		finally {
			try {
      	if (rs != null) rs.close();
      	if (s != null) s.close();
			}
			catch (SQLException se1) {
				se1.printStackTrace();
			}
		}
	}
	
	/**
	   Removes DF subscriptions whose lease time has expired.
	 */
	private void cleanExpiredSubscriptions() {
		//FIXME: To be implemented
	}

    /**
     * This method must be used when inserting text values into a DB.
     * It escapes all the chars "'" that are SQL-reserved with a double "''" one.
     * If the passed parameter is null, then an empty string is returned.
     **/
    protected String DBescape(String s) {
		if (s == null){
		    return "";
		}
		String result = replace(s,"'","''");
		result = replace(result,"\"","\"\"" );
		
		return result;
    }


    static String replace(String str, String pattern, String replaceWith) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
    
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            if(replaceWith != null)
	            result.append(replaceWith);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }


	public void subscribe(Object dfd, SubscriptionResponder.Subscription s) throws NotUnderstoodException{
		ACLMessage aclM = s.getMessage();
		String ACLString = aclM.toString();
		String convID = aclM.getConversationId();
		registerSubscription(dfd, convID, ACLString);
	}

	private void registerSubscription(Object fact, String convID, String aclM){
		Statement s = null;
		int insertedRows = 0;

		DFAgentDescription dfd = (DFAgentDescription) fact;
		// Divido il messaggio ACL di sottoscrizione in
		// pezzi da 245 byte, poiche' il TEXT con ACCESS e' di
		// 255 byte, 10 byte me li tengo per eventuali caratteri
		// di escape.
		int aclPieces = aclM.length()/245;
		if(aclM.length()%245 != 0)
			aclPieces += 1;

		String queryInsert = "INSERT  INTO subscription VALUES ('"+convID+"', '"+DBescape(aclM.substring(0,245))+"'";
		//inserisco fino al penultimo pezzo
		for(int i = 1; i < aclPieces-1; i++){
			queryInsert += ", '"+DBescape(aclM.substring(245*i,245*(i+1)))+"'";
		}
		queryInsert += ", '"+aclM.substring(245*(aclPieces-1), aclM.length())+"'";
		for(int i = aclPieces; i < 7; i++){
			queryInsert += ", NULL";
		}
		queryInsert += ", NULL)";
				
		try{
			s = conn.createStatement();
			insertedRows= s.executeUpdate(queryInsert);
			s.close();
		}catch(SQLException se){
			se.printStackTrace();
		}
	}


	
	// Deve tornare la lista di tutte le sottoscrizioni.
	// Passi
	// 1. Select di tutti gli aclmessage dalla tabella subscription
	// 2. trasformare le stringhe in ACLMessage
	// 3. costruire per ogni acl il subscription
	public Enumeration getSubscriptions(){

		Vector vRis = new Vector();
		String query ="SELECT * FROM subscription";
 		Statement s = null;
  		ResultSet rs = null;
 		StringACLCodec conv = new StringACLCodec();
		
  		try{
  			s = conn.createStatement();
  			rs = s.executeQuery(query);
  			while(rs.next())
     		{
     			String aclS = replace(rs.getString("aclm1"), "\"\"", "\"");;
     			aclS = replace(aclS, "''", "'");
     			for(int i = 2; i< 9; i++){
     				String temp = rs.getString("aclm"+i);
     				if(temp != null){
				   		temp = replace(temp, "\"\"", "\"");
				   		temp = replace(temp, "''", "'");
     					aclS += temp;
     				}
     				else
     					i = 10;
     			}
		   		byte[] aclB = aclS.getBytes();
				ACLMessage aclSub = conv.decode(aclB,ACLCodec.DEFAULT_CHARSET);
				
				//SubscriptionResponder.Subscription sub = new SubscriptionResponder.Subscription(sr, aclSub);
				vRis.add(sr.createSubscription(aclSub));			
      		}
      		rs.close();
      		s.close();
  		}catch(Exception e){
  			e.printStackTrace();
  		}
 		return vRis.elements();
	}
	
	int offSetForSubscriptionToReturn = 1;
	// Deve tornare la lista di tutte le sottoscrizioni.
	// Passi
	// 1. Select di tutti gli aclmessage dalla tabella subscription
	// 2. trasformare le stringhe in ACLMessage
	// 3. costruire per ogni acl il subscription
	public Enumeration getSubscriptions(int offset){

		Vector vRis = new Vector();
		String query ="SELECT aclmessage FROM subscription";
 		Statement s = null;
  		ResultSet rs = null;
 		StringACLCodec conv = new StringACLCodec();
		
  		try{
  			s = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE);
  			rs = s.executeQuery(query);
  			String aclS = null;
  			// mi sposto dall'ultima tupla visitata
  			rs.absolute(offSetForSubscriptionToReturn);
  			int j = 0;
  			while(rs.next() && j<offset )
     		{	
     			j++;
      			aclS = rs.getString("aclmessage");
		   		aclS = replace(aclS, "\"\"", "\"");
		   		aclS = replace(aclS, "''", "'");
		   		byte[] aclB = aclS.getBytes();

				ACLMessage aclSub = conv.decode(aclB,ACLCodec.DEFAULT_CHARSET);
				//SubscriptionResponder.Subscription sub = new SubscriptionResponder.Subscription(sr, aclSub);
				vRis.add(sr.createSubscription(aclSub));			
      		}
      		rs.close();
      		s.close();
      		offSetForSubscriptionToReturn += offset;
  		}catch(Exception e){}
  		
 		return vRis.elements();
	}
	

	public void unsubscribe(SubscriptionResponder.Subscription sub){
		ACLMessage aclM = sub.getMessage();
		String convID = aclM.getConversationId();
		String res = deregisterSubscription(convID);
		if(res == null)
			if(logger.isLoggable(Logger.WARNING))
				logger.log(Logger.WARNING,"No subscription to delete.");
	}


	// torna la chiave dell'oggetto cancellato se esisteva
  	public String deregisterSubscription(String convID) {
  		String key = convID;
		Statement s = null;
		String queryDel = "";
		int deleteRows = 0;
		boolean cancelled =false;
		
  		try{
			s = conn.createStatement();
  			queryDel ="DELETE FROM subscription WHERE conversationid = '"+convID+"'";
  			deleteRows = s.executeUpdate(queryDel);
	  		s.close();
	  		if(deleteRows != 0 )
				cancelled = true;
  		}catch(SQLException se){
  			se.printStackTrace();
  		}
		if(cancelled == false)
			key = null;
    	return key;
  	}

		private void closeConnection(){
		try { 
			conn.close(); 
    	}catch( Exception e ) {}
	}
}
