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
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.KBManagement.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.proto.SubscriptionResponder;

import jade.util.leap.Iterator;
import jade.util.leap.LinkedList;
import jade.util.leap.ArrayList;
import jade.util.leap.List;


import java.sql.*;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Date;

/**
 * @author Elisabetta Cortese - TILab
 */
class DFDBKB extends DBKB {

	private static final String[] tableNemas = {"dfagentdescr","aidaddress","aidresolver","agentprotocol","agentontology", 
												  "agentlanguage","agentservice", "agentserviceprotocol",
												  "agentserviceontology","agentservicelanguage","agentserviceproperty","subscription" };

	private int maxResults;
	
	private static final int MAX_REGISTER_WITHOUT_CLEAN = 100;
	// Number of registrations after the last lease-time-cleanup
	private int regsCnt = 0;

	// COSTRUCTORS
	public DFDBKB(int max, String drv, String url, String user, String passwd) throws SQLException {
		super(drv, url, user, passwd);
		maxResults = max;
	}
	
	protected void setup() throws SQLException {
		createTables();
		clean();
	}
	
	/**
	   Create the proper DB tables unless already there
	 */
	private void createTables() {
		Statement stmt = null;
// dfagdescr war 64
		try{
 		 	stmt = conn.createStatement();
 		 	// Tables for DF registrations      
  		stmt.executeUpdate( "CREATE TABLE dfagentdescr (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "lease   	 VARCHAR(20)  NOT NULL, "   +
         "PRIMARY KEY( aid )"+")" );

  		stmt.executeUpdate( "CREATE TABLE aidaddress (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "address    VARCHAR(255)  NOT NULL, "   +
         "PRIMARY KEY( aid, address )"+")" );

  		stmt.executeUpdate( "CREATE TABLE aidresolver (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "resolver 	 VARCHAR(255)  NOT NULL, "   +
         "PRIMARY KEY( aid, resolver )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentprotocol (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "protocol   VARCHAR(32)	      NOT NULL, "   +
         "PRIMARY KEY( aid, protocol )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentontology (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "ontology 	 VARCHAR(32)  NOT NULL, "   +
         "PRIMARY KEY( aid, ontology )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentlanguage (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "language   VARCHAR(32)  NOT NULL, "   +
         "PRIMARY KEY( aid, language )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentservice (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "sname   VARCHAR(255)  NOT NULL, "   +
         "stype       VARCHAR(64)  NOT NULL, "   +
         "sownership  VARCHAR(64)  NOT NULL, "   +
         "PRIMARY KEY( aid, sname )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentserviceprotocol (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "sname   VARCHAR(255)  NOT NULL, "   +
         "protocol   VARCHAR(32)  NOT NULL, "   +
         "PRIMARY KEY( aid, sname, protocol )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentserviceontology (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "sname   VARCHAR(255)  NOT NULL, "   +
         "ontology   VARCHAR(32)  NOT NULL, "   +
         "PRIMARY KEY( aid, sname, ontology )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentservicelanguage (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "sname   VARCHAR(255)  NOT NULL, "   +
         "language   VARCHAR(32)  NOT NULL, "   +
         "PRIMARY KEY( aid, sname, language )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentserviceproperty (" +
         "aid		 VARCHAR(255)  NOT NULL, "   +
         "sname   VARCHAR(32)  NOT NULL, "   +
         "propkey  VARCHAR(32)  NOT NULL, "   +
         "propval  VARCHAR(255)  NOT NULL, "   +
         "PRIMARY KEY( aid, sname, propkey )"+")" );

			// Tables for subscriptions registrations
  		stmt.executeUpdate( "CREATE TABLE subscription (" +
         "conversationid	 VARCHAR(255)  NOT NULL, "   +
         "aclm1	 	 VARCHAR(255)         NOT NULL, " +
         "aclm2	 	 VARCHAR(255), " +
         "aclm3	 	 VARCHAR(255), " +
         "aclm4	 	 VARCHAR(255), " +
         "aclm5	 	 VARCHAR(255), " +
         "aclm6	 	 VARCHAR(255), " +
         "aclm7	 	 VARCHAR(255), " +
         "aclm8	 	 VARCHAR(255), " +
         "PRIMARY KEY( conversationid )"+")" );
    	//DEBUG
      System.out.println("Tables correctly created");
		}
		catch(SQLException se) {
			// We interpret an SQLException here as a sign that tables 
			// are already there.
			// FIXME: We should distinguish the case of actual creation errors.
    	//DEBUG
			System.out.println("Tables already present");
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
	   Insert a new DFD object.
	   @return the previous DFD (if any) corresponding to the same 
	   AID
	 */
	public Object insert(Object name, Object fact) {
		DFAgentDescription dfd = (DFAgentDescription) fact;
		DFAgentDescription dfdToReturn = null;

		// Remove the previous DFD if any
		dfdToReturn = (DFAgentDescription) remove(dfd.getName());
		
		List l = createInserts(dfd);
		Statement s = null;
		try{
			conn.setAutoCommit(false);
			s = conn.createStatement();
			for(int i = 0; i < l.size(); i++){
				String queryInsert = (String)l.get(i);
//				System.out.println(queryInsert);
				s.addBatch(queryInsert);				
			}
			int [] updateCounts = s.executeBatch();
			conn.setAutoCommit(true);
			regsCnt++;
			// se sono stati inseriti molte tuple 
			// allora effettuo un controllo dei lease
			if(regsCnt > MAX_REGISTER_WITHOUT_CLEAN){
				clean();
				regsCnt = 0;
			}
		}
		catch(SQLException se){
			System.out.println("Error inserting DFD for agent "+dfd.getName());
			se.printStackTrace();
		}
		finally {
			try {
				if (s != null) s.close();
			}
			catch (SQLException se1) {
				se1.printStackTrace();
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
			delete(n);
		}
		
		return dfd;
	}
	
	/**
	   Retrieve the DFDs matching the given template
	 */
	public List search(Object template){
		List matchingAIDs = new ArrayList();

		// Get the names of all DFDs matching the template
		Statement s = null;
		ResultSet rs = null;
		String select = createSelect((DFAgentDescription) template);
		
		try{
			s = conn.createStatement();
			rs = s.executeQuery(select);
			int cnt = 0;
			while(rs.next() && (cnt < maxResults || maxResults < 0)) { 
    		String aidS = rs.getString("aid");

      	// Skip DFDs whose lease time has already expired
    		// FIXME: we should schedule them for removal
				String sLease = rs.getString("lease");
        long lease = Long.parseLong(sLease);
        if (!lm.isExpired(lease != -1 ? new Date(lease) : null)) {
        	matchingAIDs.add(aidS);
        	cnt++;
        }
  		}
		}
		catch(SQLException se){
			System.out.println("Error searching for DFDs matching template. DB operation: "+select);
			se.printStackTrace();
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
	   Reconstruct the DFD corresponding to the given AID name (if any)
	 */
	private DFAgentDescription getDFD(String aidN){
		DFAgentDescription dfd = null;
		AID id = null;
		
		String select = null;
		Statement s = null;
		ResultSet rs = null;
		
		try{
			s = conn.createStatement();
			
			// Check if there is a DFD corresponding to aidN and get lease time
			select = "SELECT lease FROM dfagentdescr WHERE aid='"+aidN+"'";
			rs = s.executeQuery(select);
			if (rs.next()) {
				dfd = new DFAgentDescription();
				id = new AID(aidN, AID.ISGUID);
				dfd.setName(id);
				String sLease = rs.getString("lease");
				long lease = Long.parseLong(sLease);
				if (lease != -1) {
					dfd.setLeaseTime(new Date(lease));
				}
			}
			else {
				return null;
			}
			// AID addresses
			select = "SELECT address FROM aidaddress WHERE aid='"+aidN+"'";
			rs = s.executeQuery(select);
			while(rs.next()){
				id.addAddresses(rs.getString("address"));
			}
			// AID resolvers FIXME: Resolvers are not handled properly
			select = "SELECT resolver FROM aidresolver WHERE aid='"+aidN+"'";
			rs = s.executeQuery(select);
			while(rs.next()){
				id.addResolvers(new AID(rs.getString("resolver")));
			}
			// Protocols
			select = "SELECT protocol FROM agentprotocol WHERE aid='"+aidN+"'";
			rs = s.executeQuery(select);
			while(rs.next()){
				dfd.addProtocols(rs.getString("protocol"));
			}
			// Languages
			select = "SELECT language FROM agentlanguage WHERE aid='"+aidN+"'";
			rs = s.executeQuery(select);
			while(rs.next()){
				dfd.addLanguages(rs.getString("language"));
			}
			// Ontologies
			select = "SELECT ontology FROM agentontology WHERE aid='"+aidN+"'";
			rs = s.executeQuery(select);
			while(rs.next()){
				dfd.addOntologies(rs.getString("ontology"));
			}
			// Services
			select = "SELECT sname, stype, sownership FROM agentservice WHERE aid='"+aidN+"'";
			rs = s.executeQuery(select);
			while(rs.next()){
				ServiceDescription sd = new ServiceDescription();
				sd.setName(rs.getString("sname"));
				sd.setType(rs.getString("stype"));
				sd.setOwnership(rs.getString("sownership"));
				
				Statement sS = conn.createStatement();
				String selectS = null;
				ResultSet rsS = null;
				
				// Service protocols
				selectS = "SELECT protocol FROM agentserviceprotocol WHERE aid='"+aidN+"'";
				if(sd.getName() != null)
					selectS +=" and sname='"+sd.getName()+"'";
				rsS = sS.executeQuery(selectS);
				while(rsS.next()){
					sd.addProtocols(rsS.getString("protocol"));
				}	
				// Service languages
				selectS = "SELECT ontology FROM agentserviceontology WHERE aid='"+aidN+"'";
				if(sd.getName() != null)
					selectS +=" and sname='"+sd.getName()+"'";
				rsS = sS.executeQuery(selectS);
				while(rsS.next()){
					sd.addOntologies(rsS.getString("ontology"));
				}	
				// Service ontologies
				selectS = "SELECT language FROM agentservicelanguage WHERE aid='"+aidN+"'";
				if(sd.getName() != null)
					selectS +=" and sname='"+sd.getName()+"'";
				rsS = sS.executeQuery(selectS);
				while(rsS.next()){
					sd.addLanguages(rsS.getString("language"));
				}
				// Service properties
				selectS = "SELECT propkey, propval FROM agentserviceproperty WHERE aid='"+aidN+"'";
				if(sd.getName() != null)
					selectS +=" and sname='"+sd.getName()+"'";				
				rsS = sS.executeQuery(selectS);
				while(rsS.next()){
					Property prop = new Property();
					prop.setName(rsS.getString("propkey"));
					prop.setValue(rsS.getString("propval"));
					sd.addProperties(prop);
				}	
				rsS.close();
				sS.close();
				dfd.addServices(sd);
			}
		}
		catch(SQLException se){
			System.out.println("Error reconstructing DFD for agent "+aidN);
			se.printStackTrace();
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
	   Delete the DFD object corresponding to the indicated agent name.
	 */
	private void delete(String nameAg) {  		
		Statement s = null;
		try{
			conn.setAutoCommit(false);
			s = conn.createStatement();
			
  		for(int i = 0; i < tableNemas.length-1; i++){
  			String delete ="DELETE FROM "+tableNemas[i]+" WHERE aid = '"+nameAg+"'";
  			s.addBatch(delete);
  		}
  		s.executeBatch();
  		conn.setAutoCommit(true);
		}
		catch(SQLException se){
			System.out.println("Error removing DFD for agent "+nameAg);
			se.printStackTrace();
		}
		finally {
			try {
				if (s != null) s.close();
			}
			catch (SQLException se1) {
				se1.printStackTrace();
			}
		}
	}

	/**
	   Return the SQL INSERT operations (as a List) that must be
	   executed to insert a given DFAgentDescription into the DB
	 */
	private List createInserts(DFAgentDescription dfd){

		List updates = new ArrayList();
		AID agentAID = dfd.getName();
		
		String agentName = agentAID.getName();
		Date leaseTime = dfd.getLeaseTime();
		long lt = (leaseTime != null ? leaseTime.getTime() : -1);
		String u = "INSERT INTO dfagentdescr VALUES ('"+agentName+"', '"+lt+"')";
		updates.add(u);
		
		Iterator iter = agentAID.getAllAddresses();
		while( iter.hasNext()){
			u = "INSERT INTO aidaddress VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			updates.add(u);
		}

		// FIXME: This should be recursive
		iter = agentAID.getAllResolvers();
		while( iter.hasNext() ){
			u = "INSERT INTO agentresolver VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			updates.add(u);
		}
		
		iter = dfd.getAllLanguages();
		while(iter.hasNext()){
			u = "INSERT INTO agentlanguage VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			updates.add(u);
		}
		
		iter = dfd.getAllOntologies();
		while(iter.hasNext()){
			u = "INSERT INTO agentontology VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			updates.add(u);
		}

		iter = dfd.getAllProtocols();
		while( iter.hasNext() ){
			u = "INSERT INTO agentprotocol VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			updates.add(u);
		}
		
		iter = dfd.getAllServices();
		while(iter.hasNext()){
			ServiceDescription service = (ServiceDescription)iter.next();
			String serviceName = service.getName();
			String serviceType = service.getType();
			String serviceOwner = service.getOwnership();
			u = "INSERT INTO agentservice VALUES ('"+agentName+"', '"+serviceName+"', '"+serviceType+"', '"+serviceOwner+"')";
			updates.add(u);

			Iterator iterS = service.getAllProtocols();
			while( iterS.hasNext() ){
				u = "INSERT INTO agentserviceprotocol VALUES ('"+agentName+"', '"+serviceName+"', '"+(String)iterS.next()+"')";
				updates.add(u);
			}

			iterS = service.getAllOntologies();
			while( iterS.hasNext() ){
				u = "INSERT INTO agentserviceontology VALUES ('"+agentName+"', '"+serviceName+"', '"+(String)iterS.next()+"')";
				updates.add(u);
			}

			iterS = service.getAllLanguages();
			while( iterS.hasNext() ){
				u = "INSERT INTO agentservicelanguage VALUES ('"+agentName+"', '"+serviceName+"', '"+(String)iterS.next()+"')";
				updates.add(u);
			}

			iterS = service.getAllProperties();
			while( iterS.hasNext() ){
				Property prop = (Property)iterS.next();
				u = "INSERT INTO agentserviceproperty VALUES ('"+agentName+"', '"+serviceName+"', '"+(String)prop.getName()+"', '"+prop.getValue().toString()+"')";
				updates.add(u);
			}
		}
		
		return updates;			
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
			lAs.add(", agentlanguage AS "+tmp);
			lWhere.add(tmp+".language='"+(String)iter.next()+"'");
			lWhere.add(tmp+".aid=dfagentdescr.aid");
			i++;
		}
		// Ontologies
		iter = dfdTemplate.getAllOntologies();
		i = 0;
		while(iter.hasNext()){
			String tmp = "ontology"+i;
			lAs.add(", agentontology AS "+tmp);
			lWhere.add(tmp+".ontology='"+(String)iter.next()+"'");
			lWhere.add(tmp+".aid=dfagentdescr.aid");
			i++;
		}
		// Protocols
		iter = dfdTemplate.getAllProtocols();
		i = 0;
		while(iter.hasNext()){
			String tmp = "protocol"+i;
			lAs.add(", agentprotocol AS "+tmp);
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
			lAs.add(", agentservice AS "+tmp);
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
				lAs.add(", agentservicelanguage AS "+tmp1);
				lWhere.add(tmp1+".language='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".aid=dfagentdescr.aid");
				j++;
			}
			// Service ontologies
			iterS = service.getAllOntologies();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = "serviceontology"+j;
				lAs.add(", agentserviceontology AS "+tmp1);
				lWhere.add(tmp1+".ontology='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".aid=dfagentdescr.aid");
				j++;
			}
			// Service protocols
			iterS = service.getAllProtocols();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = "serviceprotocol"+j;
				lAs.add(", agentserviceprotocol AS "+tmp1);
				lWhere.add(tmp1+".protocol='"+(String)iterS.next()+"'");
				lWhere.add(tmp1+".aid=dfagentdescr.aid");
				j++;
			}
			// Service properties
			iterS = service.getAllProperties();
			j = 0;
			while(iterS.hasNext()){
				String tmp1 = "serviceproperty"+j;
				lAs.add(", agentserviceproperty AS "+tmp1);
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
			s = conn.createStatement();
			rs = s.executeQuery("SELECT aid FROM dfagentdescr WHERE lease <'"+currTime+"' AND lease <>'-1'"); // seleziona gli scaduti
			while(rs.next()){
				delete(rs.getString("aid"));
			}
		}
		catch(SQLException se){
			se.printStackTrace();
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
				ACLMessage aclSub = conv.decode(aclB);
				
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

				ACLMessage aclSub = conv.decode(aclB);
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
			System.out.println("No subscription to delete.");
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

  ////////////////////////////////////////
  // Utility methods currently not used
  ////////////////////////////////////////

	// Passo una lista di AID name corrispondente
	// alla lista dei dfd registrati da cancellare
	// le operazioni sono di tipo batch
	private void deregisterSetOfDfd(List l){
		
		Statement s = null;
		String queryDel = "";
		int deleteRows = 0;
		boolean cancelled =false;
		String nameAg = null;
  		try{
			conn.setAutoCommit(false);
			s = conn.createStatement();
			for(int j=0; j<l.size(); j++){
				nameAg = (String)l.get(j);
		  		for(int i = 0; i < tableNemas.length-1; i++){
		  			queryDel ="DELETE FROM "+tableNemas[i]+" WHERE aid = '"+nameAg+"'";
		  			s.addBatch(queryDel);
		  		}
			}
	  		int [] updateCounts = s.executeBatch();
	  		conn.setAutoCommit(true);
	  		s.close();
  		}catch(SQLException se){
  			se.printStackTrace();
  		}
	}


	// Cancella le tabelle la procedura e' di tipo batch
	private void dropTables(){
		Statement s = null;
		String q="";
		try{
			s = conn.createStatement();
			for(int i = 0; i<tableNemas.length; i++){
				q = "DROP table "+tableNemas[i];
				//System.out.println(q);
				s.executeUpdate(q);
			}
			s.close();
		}catch(SQLException se){
			if(s != null)
				try{
					s.close();
				}catch(SQLException see){
					see.printStackTrace();
				}
			se.printStackTrace();
		}
		System.out.println("Tables dropped.");
	}
}
