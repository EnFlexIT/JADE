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
package jade.domain.KBManagement;

//#J2ME_EXCLUDE_FILE

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
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
public class DFDBKB extends DBKB {

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
		try{
 		 	stmt = conn.createStatement();
 		 	// Tables for DF registrations      
  		stmt.executeUpdate( "CREATE TABLE dfagentdescr (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "lease   	 VARCHAR(20)  NOT NULL, "   +
         "PRIMARY KEY( aid,lease )"+")" );

  		stmt.executeUpdate( "CREATE TABLE aidaddress (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "address    VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, address )"+")" );

  		stmt.executeUpdate( "CREATE TABLE aidresolver (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "resolver 	 VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, resolver )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentprotocol (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "protocol   VARCHAR(30)	      NOT NULL, "   +
         "PRIMARY KEY( aid, protocol )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentontology (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "ontology 	 VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, ontology )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentlanguage (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "language   VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, language )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentservice (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "servicen   VARCHAR(30)  NOT NULL, "   +
         "type       VARCHAR(30)  NOT NULL, "   +
         "ownership  VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, servicen, type )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentserviceprotocol (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "servicen   VARCHAR(30)  NOT NULL, "   +
         "type       VARCHAR(30)  NOT NULL, "   +
         "protocol   VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, servicen, type, protocol )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentserviceontology (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "servicen   VARCHAR(30)  NOT NULL, "   +
         "type       VARCHAR(30)  NOT NULL, "   +
         "ontology   VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, servicen, type, ontology )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentservicelanguage (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "servicen   VARCHAR(30)  NOT NULL, "   +
         "type       VARCHAR(30)  NOT NULL, "   +
         "language   VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, servicen, type, language )"+")" );

  		stmt.executeUpdate( "CREATE TABLE agentserviceproperty (" +
         "aid		 VARCHAR(30)  NOT NULL, "   +
         "servicen   VARCHAR(30)  NOT NULL, "   +
         "type       VARCHAR(30)  NOT NULL, "   +
         "propertyn  VARCHAR(30)  NOT NULL, "   +
         "propertyv  VARCHAR(30)  NOT NULL, "   +
         "PRIMARY KEY( aid, servicen, type, propertyn )"+")" );

			// Tables for subscriptions registrations
  		stmt.executeUpdate( "CREATE TABLE subscription (" +
         "conversationid	 VARCHAR(70)  NOT NULL, "   +
         "aclm1	 	 TEXT         NOT NULL, " +
         "aclm2	 	 TEXT, " +
         "aclm3	 	 TEXT, " +
         "aclm4	 	 TEXT, " +
         "aclm5	 	 TEXT, " +
         "aclm6	 	 TEXT, " +
         "aclm7	 	 TEXT, " +
         "aclm8	 	 TEXT, " +
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

		Statement stmt = null;
		ResultSet rs = null;
		long currLease = System.currentTimeMillis();
		AID aidTemp = new AID();
		try{
			List l = new LinkedList();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT aid FROM dfagentdescr WHERE lease <'"+currLease+"' AND lease <>'-1'"); // seleziona gli scaduti
			while(rs.next()){
				aidTemp.setName(rs.getString("aid"));
				deregister(aidTemp);
			}
      		rs.close();
      		stmt.close();
		}catch(SQLException se){
			se.printStackTrace();
		}
	}
	
	/**
	   Removes DF subscriptions whose lease time has expired.
	 */
	private void cleanExpiredSubscriptions() {
		//FIXME: To be implemented
	}

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
	
	// Passi per fare register
	// 1. prendere il dfd e trasformarlo in una insert
	// 2. deregistra il precedente se c'era
	// 3. esegue la insert
	// 4. ritornare null se l'oggetto non esisteva prima altrimenti il vecchio
  	public Object insert(Object name, Object fact) {
		Statement s = null;
		int insertedRows = 0;
		// qui dai controlli effettuati dal DFService sono 
		// sicura che il campo AID dell'agente del dfd esiste
		DFAgentDescription dfd = (DFAgentDescription) fact;
		DFAgentDescription dfdToReturn = null;

		dfdToReturn = (DFAgentDescription) deregister(dfd.getName());
		
		// a questo punto sono sicura che il dfd non esiste
		List l = dfdToInsertQueries(dfd);
		try{
			conn.setAutoCommit(false);
			s = conn.createStatement();
			for(int i = 0; i < l.size(); i++){
				String queryInsert = (String)l.get(i);
				s.addBatch(queryInsert);				
			}
			int [] updateCounts = s.executeBatch();
			conn.setAutoCommit(true);
			s.close();
			regsCnt++;
			// se sono stati inseriti molte tuple 
			//allora effettuo un controllo dei lease
			if(regsCnt > MAX_REGISTER_WITHOUT_CLEAN){
				clean();
				regsCnt = 0;
			}
		}catch(SQLException se){
			System.out.println("DFAgentDescription already exist: "+se.getMessage());
		}

    	return dfdToReturn;
  	}


	// ritorna la lista delle insert che si devono effettuare 
	// sulle varie tabelle del db per fare una registrazione
	private List dfdToInsertQueries(DFAgentDescription dfd){

		ArrayList listQuery = new ArrayList();
		AID agentAID = dfd.getName();
		
		String agentName = agentAID.getName();
		String query = ""; 
		
		Date leaseTime = dfd.getLeaseTime();
		long lt = (leaseTime != null ? leaseTime.getTime() : -1);
		query = "INSERT INTO dfagentdescr VALUES ('"+agentName+"', '"+lt+"')";
		listQuery.add(query);
		
		Iterator iter = agentAID.getAllAddresses();
		while( iter.hasNext()){
			query = "INSERT INTO aidaddress VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			listQuery.add(query);
		}

		iter = agentAID.getAllResolvers();
		while( iter.hasNext() ){
			query = "INSERT INTO agentresolver VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			listQuery.add(query);
		}
		
		iter = dfd.getAllLanguages();
		while(iter.hasNext()){
			query = "INSERT INTO agentlanguage VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			listQuery.add(query);
		}
		
		iter = dfd.getAllOntologies();
		while(iter.hasNext()){
			query = "INSERT INTO agentontology VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			listQuery.add(query);
		}

		iter = dfd.getAllProtocols();
		while( iter.hasNext() ){
			query = "INSERT INTO agentprotocol VALUES ('"+agentName+"', '"+(String)iter.next()+"')";
			listQuery.add(query);
		}
		
		iter = dfd.getAllServices();
		while(iter.hasNext()){
			ServiceDescription service = (ServiceDescription)iter.next();
			String serviceName = service.getName();
			String serviceType = service.getType();
			String serviceOwner = service.getOwnership();
			query = "INSERT INTO agentservice VALUES ('"+agentName+"', '"+serviceName+"', '"+serviceType+"', '"+serviceOwner+"')";
			listQuery.add(query);

			Iterator iterS = service.getAllProtocols();
			while( iterS.hasNext() ){
				query = "INSERT INTO agentserviceprotocol VALUES ('"+agentName+"', '"+serviceName+"', '"+serviceType+"', '"+(String)iterS.next()+"')";
				listQuery.add(query);
			}

			iterS = service.getAllOntologies();
			while( iterS.hasNext() ){
				query = "INSERT INTO agentserviceontology VALUES ('"+agentName+"', '"+serviceName+"', '"+serviceType+"', '"+(String)iterS.next()+"')";
				listQuery.add(query);
			}

			iterS = service.getAllLanguages();
			while( iterS.hasNext() ){
				query = "INSERT INTO agentservicelanguage VALUES ('"+agentName+"', '"+serviceName+"', '"+serviceType+"', '"+(String)iterS.next()+"')";
				listQuery.add(query);
			}

			iterS = service.getAllProperties();
			while( iterS.hasNext() ){
				Property prop = (Property)iterS.next();
				query = "INSERT INTO agentserviceproperty VALUES ('"+agentName+"', '"+serviceName+"', '"+serviceType+"', '"+(String)prop.getName()+"', '"+prop.getValue().toString()+"')";
				listQuery.add(query);
			}
		}
		
		return listQuery;			
	}
	

	// torna l'oggetto cancellato se esisteva
	// FIXME: Currently it does not return the deleted DFD but an
	// empty DFD with just the name field set.
  	public Object deregister(Object name) {

  		AID agentAID = (AID) name;
  		String nameAg = agentAID.getName();
  		
		Statement s = null;
		String queryDel = "";
		int deleteRows = 0;
		boolean cancelled =false;
		
  		try{
			conn.setAutoCommit(false);
			s = conn.createStatement();
			
	  		for(int i = 0; i < tableNemas.length-1; i++){
	  			queryDel ="DELETE FROM "+tableNemas[i]+" WHERE aid = '"+nameAg+"'";
	  			s.addBatch(queryDel);
	  		}
	  		int [] updateCounts = s.executeBatch();
	  		conn.setAutoCommit(true);
	  		s.close();
	  		for(int i = 0; i < updateCounts.length; i++){
		  		if(updateCounts[i] != 0 ){
					cancelled = true;
	  			}
	  		}
  		}catch(SQLException se){
  			System.out.println("Deregistration error: "+se.getMessage());
  		}
  		
  		DFAgentDescription dfd = null;
			if(cancelled){
				dfd = new DFAgentDescription();
				dfd.setName(agentAID);
			}
    	return dfd;
  	}


	// La search si svolge in piu' passi:
	// 1. effettua una SELECT per reperire tutti gli AID
	//	  degli agenti che soddisfano la richiesta
	// 2. degli AID tornati ne prende solo i primi MAXAIDToReturn
	//    che non abbiano lease scaduto
	// 3. per ogni aid interroga il db per ricostruire il DFAgentDescription
	// 4. ritorna la lista dei DFAgentDescription
  	public List search(Object template){

  		LinkedList aidList = new LinkedList();
  		DFAgentDescription dfd = (DFAgentDescription) template;

  		// PRIMA SELELCT PER AID
  		String querySel = dfdToSelectQuery(dfd);
			
  		Statement s = null;
  		ResultSet rs = null;
  		try{
  			s = conn.createStatement();
  			rs = s.executeQuery(querySel);
  			int AIDfound = 0;
  			while(rs.next() && AIDfound < maxResults)   // for each row of data until to 100
      		{
        		String aidS = rs.getString("aid");

				String sLease = rs.getString("lease");
		        long lease = Long.parseLong(sLease);
		        if (!lm.isExpired(lease != -1 ? new Date(lease) : null)) {
		        //long currentLease = System.currentTimeMillis();
						// se ancora deve scadere oppure il leaseRis del db e' infinito
		        //if(currentLease < leaseRis || leaseRis == (lm.INFINITE_LEASE_TIME).getTime()){
		        	aidList.add(aidS);
		        	AIDfound++;
		        }
      		}
      		rs.close();
      		s.close();
  		}catch(SQLException se){
  			se.printStackTrace();
  		}

  		List l = new LinkedList();
		l = makeListDfd(aidList);
		return l;
  	}


	// STEP
	// 1. per ogni tabella faccio la SELECT sull'AID
	// 2. per ogni resultSet riempio i campi opportuni dell'oggetto dfd
	// 3. ritorno l'oggetto
	private List makeListDfd(List aidList){
		List l = new LinkedList();
		
		for(int i =0; i<aidList.size(); i++){
			String aidN = (String) aidList.get(i);
			DFAgentDescription dfd = new DFAgentDescription();
			
			String query = "";
			query = "SELECT * FROM dfagentdescr WHERE aid='"+aidN+"'";
			Statement s = null;
			ResultSet rs = null;
			
			try{
				s = conn.createStatement();
				rs = s.executeQuery(query);
	
				AID	aidAg = new AID();	
				while(rs.next()){
					aidAg.setName(rs.getString("aid"));
					dfd.setName(aidAg);
					String sLease = rs.getString("lease");
					long lease = Long.parseLong(sLease);
					if (lease != -1) {
						dfd.setLeaseTime(new Date(lease));
					}
				}
				query = "SELECT address FROM aidaddress WHERE aid='"+aidN+"'";
				rs = s.executeQuery(query);
				while(rs.next()){
					aidAg.addAddresses(rs.getString("address"));
				}
				query = "SELECT resolver FROM aidresolver WHERE aid='"+aidN+"'";
				rs = s.executeQuery(query);
				while(rs.next()){
					aidAg.addResolvers(new AID(rs.getString("resolver"), true));
				}
				query = "SELECT protocol FROM agentprotocol WHERE aid='"+aidN+"'";
				rs = s.executeQuery(query);
				while(rs.next()){
					dfd.addProtocols(rs.getString("protocol"));
				}
				query = "SELECT language FROM agentlanguage WHERE aid='"+aidN+"'";
				rs = s.executeQuery(query);
				while(rs.next()){
					dfd.addLanguages(rs.getString("language"));
				}
				query = "SELECT ontology FROM agentontology WHERE aid='"+aidN+"'";
				rs = s.executeQuery(query);
				while(rs.next()){
					dfd.addOntologies(rs.getString("ontology"));
				}
				query = "SELECT servicen, type, ownership FROM agentservice WHERE aid='"+aidN+"'";
				rs = s.executeQuery(query);
				while(rs.next()){
					ServiceDescription sd = new ServiceDescription();
					sd.setName(rs.getString("servicen"));
					sd.setType(rs.getString("type"));
					sd.setOwnership(rs.getString("ownership"));
					String queryServ = "SELECT protocol FROM agentserviceprotocol WHERE aid='"+aidN+"'";
					if(sd.getName() != null)
						queryServ +=" and servicen='"+sd.getName()+"'";
					if(sd.getType() != null)
						queryServ +=" and type='"+sd.getType()+"'";
					
					Statement s1 =conn.createStatement();
					ResultSet rsServ = s1.executeQuery(queryServ);
					while(rsServ.next()){
						sd.addProtocols(rsServ.getString("protocol"));
					}	
					queryServ = "SELECT ontology FROM agentserviceontology WHERE aid='"+aidN+"'";
					if(sd.getName() != null)
						queryServ +=" and servicen='"+sd.getName()+"'";
					if(sd.getType() != null)
						queryServ +=" and type='"+sd.getType()+"'";
					
					rsServ = s1.executeQuery(queryServ);
					while(rsServ.next()){
						sd.addOntologies(rsServ.getString("ontology"));
					}	
					queryServ = "SELECT language FROM agentservicelanguage WHERE aid='"+aidN+"'";
					if(sd.getName() != null)
						queryServ +=" and servicen='"+sd.getName()+"'";
					if(sd.getType() != null)
						queryServ +=" and type='"+sd.getType()+"'";
					
					rsServ = s1.executeQuery(queryServ);
					while(rsServ.next()){
						sd.addLanguages(rsServ.getString("language"));
					}
					queryServ = "SELECT propertyn, propertyv FROM agentserviceproperty WHERE aid='"+aidN+"'";
					if(sd.getName() != null)
						queryServ +=" and servicen='"+sd.getName()+"'";
					if(sd.getType() != null)
						queryServ +=" and type='"+sd.getType()+"'";
					
					rsServ = s1.executeQuery(queryServ);
					while(rsServ.next()){
						Property prop = new Property();
						prop.setName(rsServ.getString("propertyn"));
						prop.setValue(rsServ.getString("propertyv"));
						sd.addProperties(prop);
					}	
					rsServ.close();
					s1.close();
					dfd.addServices(sd);
				}
				rs.close();			
				s.close();
			}catch(SQLException se){
				se.printStackTrace();
			}
			l.add(dfd);
		}		
		return l;
	}



	// Questa restituisce solo la prima select:
	// cioe' quella che restituisce gli AID degli agenti che 
	// offrono servizi che match con il template
	private String dfdToSelectQuery(DFAgentDescription dfdTemplate){
	
		String query ="SELECT dfagentdescr.aid, dfagentdescr.lease FROM dfagentdescr";
		
		LinkedList lT = new LinkedList();
		LinkedList lW = new LinkedList();
		
		AID agentAID = null;
		agentAID = dfdTemplate.getName();
		// FIXME non dovrebbe esserci nulla da cercare se si conosce AID??
		if(agentAID != null){
			lW.add(" dfagentdescr.aid = '"+agentAID.getName()+"'");
		}
		// Da migliorare!!!
		Date lease = dfdTemplate.getLeaseTime();
		
		if(lease != null){
			lW.add(" dfagentdescr.lease > '"+lease.getTime()+"'");
		}
		Iterator iter = dfdTemplate.getAllLanguages();
		int i=0;
		String sub ="agentlanguage";
		while(iter.hasNext()){
			sub = sub.substring(0,7)+i;
			lT.add(", agentlanguage AS "+sub);
			lW.add(sub+".language='"+(String)iter.next()+"'");
			i++;
		}
		for(int k = 0; k <i; k++){
			sub = sub.substring(0,7)+k;
			lW.add(sub+".aid=dfagentdescr.aid");
		}
		i = 0;
		iter = dfdTemplate.getAllOntologies();
		sub="agentontology";
		while(iter.hasNext()){
			sub = sub.substring(0,7)+i;
			lT.add(", agentontology AS "+sub);
			lW.add(sub+".ontology='"+(String)iter.next()+"'");
			i++;
		}
		for(int k = 0; k <i; k++){
			sub = sub.substring(0,7)+k;
			lW.add(sub+".aid=dfagentdescr.aid");
		}
		i = 0;
		iter = dfdTemplate.getAllProtocols();
		sub="agentprotocol";
		while(iter.hasNext()){
			sub = sub.substring(0,7)+i;
			lT.add(", agentprotocol AS "+sub+"");
			lW.add(sub+".protocol='"+(String)iter.next()+"'");
			i++;
		}
		for(int k = 0; k <i; k++){
			sub = sub.substring(0,7)+k;
			lW.add(sub+".aid=dfagentdescr.aid");
		}
		i = 0;
		iter = dfdTemplate.getAllServices();
		sub = "agentservice";
		while(iter.hasNext()){
			ServiceDescription service = (ServiceDescription)iter.next();
			String serviceName = service.getName();
			String serviceType = service.getType();
			String serviceOwner = service.getOwnership();
			sub = sub.substring(0,7)+i;
			lT.add(", agentservice AS "+sub+"");
			i++;
			if(serviceName != null){
				lW.add(sub+".servicen='"+serviceName+"'");
			}
			if(serviceType != null){
				lW.add(sub+".type='"+serviceType+"'");
			}
			if(serviceOwner != null){
				lW.add(sub+".ownership='"+serviceOwner+"'");
			}
			int j = 0;
			Iterator iterS = service.getAllLanguages();
			String sub1 ="agentservicelanguage";
			while(iterS.hasNext()){
				sub1 = sub1.substring(0,14)+j; 
				lT.add(", agentservicelanguage AS "+sub1);
				lW.add(sub1+".language='"+(String)iterS.next()+"'");
				j++;
			}
			for(int k = 0; k <j; k++){
				sub1 = sub1.substring(0,14)+k;
				lW.add(sub1+".aid=dfagentdescr.aid");
			}
			j=0;	
			iterS = service.getAllOntologies();
			sub1 ="agentserviceontology";
			while(iterS.hasNext()){
				sub1 = sub1.substring(0,14)+j;
				lT.add(", agentserviceontology AS "+sub1);
				lW.add(sub1+".ontology='"+(String)iterS.next()+"'");
				j++;
			}
			for(int k = 0; k <j; k++){
				sub1 = sub1.substring(0,14)+k;
				lW.add(sub1+".aid=dfagentdescr.aid");
			}
			j=0;	
			iterS = service.getAllProtocols();
			sub1 ="agentserviceprotocol";
			while(iterS.hasNext()){
				sub1 = sub1.substring(0,14)+j;
				lT.add(", agentserviceprotocol AS "+sub1);
				lW.add(sub1+".protocol='"+(String)iterS.next()+"'");
				j++;
			}
			for(int k = 0; k <j; k++){
				sub1 = sub1.substring(0,14)+k;
				lW.add(sub1+".aid=dfagentdescr.aid");
			}
			j=0;	
			iterS = service.getAllProperties();
			sub1 = "agentserviceproperty";
			while(iterS.hasNext()){
				Property prop = (Property) iterS.next();
				sub1 = sub1.substring(0,14)+j;
				lT.add(", agentserviceproperty AS "+sub1);
				lW.add(sub1+".propertyn='"+prop.getName()+"' and "+sub1+".propertyv='"+prop.getValue().toString()+"'");
				j++;
			}
			for(int k = 0; k <j; k++){
				sub1 = sub1.substring(0,14)+k;
				lW.add(sub1+".aid=dfagentdescr.aid");
			}
		}
		for(int k = 0; k <i; k++){
			sub = sub.substring(0,7)+k;
			lW.add(sub+".aid=dfagentdescr.aid");
		}
		for(i = 0; i < lT.size(); i++){
			query +=lT.get(i);
		}
		if (lW.size() > 0) {
			query += " WHERE ";
		}
		for(i = 0; i < lW.size(); i++){
			if(i > 0)
				query += " and ";
			query += lW.get(i);
		}
		return query;
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
				
				SubscriptionResponder.Subscription sub = new SubscriptionResponder.Subscription(sr, aclSub);
				vRis.add(sub);			
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
				SubscriptionResponder.Subscription sub = new SubscriptionResponder.Subscription(sr, aclSub);
				vRis.add(sub);			
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


/*************** Main method, for testing purposes ******************************/
  public static void main(String[] args) {
  	
  	
//		DFDBKB db = new DFDBKB(20, "com.mysql.jdbc.Driver","jdbc:mysql://localhost/DFDB");
 		try {
 			DFDBKB db = new DFDBKB(20, null,"jdbc:odbc:DFDB", null, null);
  		db.dropTables();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		

//		db.createTables();

//		db.lm = new DFLeaseManager();
//
//	    DFAgentDescription dfd = new DFAgentDescription();
//	    AID aidAgent1 = new AID("lisa@napoli.com", true);
//	    aidAgent1.addAddresses("http://napoli.com");
//	    dfd.setName(aidAgent1);
//
//		Date d = new Date(System.currentTimeMillis()+(60000*30));
//		System.out.println("Data: "+d.toString());
//	    dfd.setLeaseTime( d );
//	    dfd.addLanguages("kif");
//	    ServiceDescription sd= new ServiceDescription();
//	    sd.setType("seller");
//	    dfd.addServices(sd);
//		
//		db.register("lisa@napoli.com", dfd);



//
//		long start = System.currentTimeMillis();
//		// CREO IL DFD
//	    for(int i = 0; i<1000; i++){
//		    DFAgentDescription dfd = new DFAgentDescription();
//			AID aidAgent;
//		    ServiceDescription sd = new ServiceDescription();
//		    aidAgent = new AID("rosalba@napoli.it"+i, true);
//		    aidAgent.addAddresses("http://cit.it");
//		    dfd.setName(aidAgent);
//		    dfd.setLease(System.currentTimeMillis()+(600000));
//		    dfd.addLanguages("potentino");
//		    dfd.addLanguages("kif");
//		    dfd.addProtocols("tlc-application-protocol");
//		    sd.setName("wireless");
//		    sd.setType("web-services");
//		    sd.setOwnership("Free");
//		    sd.addLanguages("SQL");
//		    sd.addProtocols("FIPA-REQUEST");
//		    dfd.addServices(sd);
//		   	// REGISTRO
//		    db.register("rosalba@napoli.it"+i, dfd);
//	    }
//		
//		long end = System.currentTimeMillis();
//		System.out.println("Total Registration time: "+(end-start));
//
////		// SEARCH
//		DFAgentDescription dfdTempalete = new DFAgentDescription();
//		dfdTempalete.addLanguages("kif");
//		ServiceDescription sdT = new ServiceDescription();
//		sdT.setName("wireless");
//		sdT.setType("web-services");
//		dfdTempalete.addServices(sdT);
//
//		start = System.currentTimeMillis();		
//		db.search(dfdTempalete);
//		end = System.currentTimeMillis();
//		System.out.println("Search time: "+(end-start));
//
//
//		// CREO IL DFD
////	    for(int i = 0; i<5000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////			AID aidAgent;
////		    ServiceDescription sd = new ServiceDescription();
////		    aidAgent = new AID("rosalba@napoli.it"+i, true);
////		    aidAgent.addAddresses("http://cit.it");
////		    dfd.setName(aidAgent);
////		    dfd.setLease(System.currentTimeMillis()+(600000*24));
////		    dfd.addLanguages("potentino");
////		    dfd.addLanguages("kif");
////		    dfd.addProtocols("tlc-application-protocol");
////		    sd.setName("wireless");
////		    sd.setType("web-services");
////		    sd.setOwnership("Free");
////		    sd.addLanguages("SQL");
////		    sd.addProtocols("FIPA-REQUEST");
////		    dfd.addServices(sd);
////		   	// REGISTRO
////		    db.register("rosalba@napoli.it"+i, dfd);
////	    }
//// 		System.out.println("FINITO");
////
////	    for(int i = 5000; i<10000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent1 = new AID("filippo@lecce.com"+i, true);
////		    aidAgent1.addAddresses("http://lecce.com");
////		    dfd.setName(aidAgent1);
////		    dfd.setLease(System.currentTimeMillis()+(60000*30));
////		    dfd.addLanguages("kif");
////		    dfd.addLanguages("pugliese");
////		    ServiceDescription sd= new ServiceDescription();
////		    sd.setType("seller");
////		    dfd.addServices(sd);
////			
////			db.register("filippo@lecce.com"+i, dfd);
//// 		}
//// 		System.out.println("FINITO");
////		
////
////	    for(int i = 10000; i<20000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent2 = new AID("Lisa@napoli.it"+i, true);
////		    dfd.setName(aidAgent2);
////		    dfd.setLease(System.currentTimeMillis()+(600000*48));
////		    dfd.addProtocols("FIPA-REQUEST");
////		    dfd.addLanguages("napoletano");
////		    dfd.addLanguages("kif");
////		    ServiceDescription sd= new ServiceDescription();
////		    sd.setName("pizzaiola");
////		    sd.setType("food");
////		    dfd.addServices(sd);
////			
////			db.register("lisa@napoli.it"+i,dfd);
//// 		}
//// 		System.out.println("FINITO");
////
////
////	    for(int i = 20000; i<30000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent2 = new AID("infinito@napoli.it"+i, true);
////		    dfd.setName(aidAgent2);
////		    dfd.setLease(-1);
////		    dfd.addLanguages("SQL");
////		    dfd.addLanguages("kif");
////			db.register("infinito@napoli.it"+i,dfd);
//// 		}
//// 		System.out.println("FINITO");
////		
////
////	    for(int i = 0; i<100; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent2 = new AID("Lisa@napoli.it"+i, true);
////		    dfd.setName(aidAgent2);
////		    dfd.setLease(System.currentTimeMillis()+(600000*48));
////		    dfd.addProtocols("FIPA-REQUEST");
////		    dfd.addLanguages("napoletano");
////		    dfd.addLanguages("kif");
////		    ServiceDescription sd= new ServiceDescription();
////		    sd.setName("pizzaiola");
////		    sd.setType("food");
////		    dfd.addServices(sd);
////			
////			db.register("lisa@napoli.it"+i,dfd);
//// 		}
//// 		System.out.println("FINITO");
//
////
////	    for(int i = 15000; i<20000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent2 = new AID("infinito@napoli.it"+i, true);
////		    dfd.setName(aidAgent2);
////		    dfd.setLease(-1);
////		    dfd.addLanguages("SQL");
////		    dfd.addLanguages("kif");
////			db.register("infinito@napoli.it"+i,dfd);
//// 		}
//// 		System.out.println("FINITO");
//// 		
////	    for(int i = 20000; i<40000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent2 = new AID("giosue@napoli.it"+i, true);
////		    dfd.setName(aidAgent2);
////		    dfd.setLease(System.currentTimeMillis()+(600000*48));
////		    dfd.addProtocols("FIPA-REQUEST");
////		    dfd.addLanguages("napoletano");
////		    dfd.addLanguages("kif");
////		    ServiceDescription sd= new ServiceDescription();
////		    sd.setName("pizzaiola");
////		    sd.setType("food");
////		    dfd.addServices(sd);
////			
////			db.register("giosue@napoli.it"+i,dfd);
//// 		}
//// 		System.out.println("FINITO ");
////
////	    for(int i =40000; i<60000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent1 = new AID("roberto@ariano.com"+i, true);
////		    dfd.setName(aidAgent1);
////		    dfd.setLease(System.currentTimeMillis()+(600000*20));
////		    dfd.addLanguages("kif");
////		    dfd.addLanguages("SQL");
////		    ServiceDescription sd= new ServiceDescription();
////		    sd.setType("seller");
////		    dfd.addServices(sd);
////			
////			db.register("roberto@ariano.com"+i, dfd);
//// 		}
//// 		System.out.println("FINITO");
////
////	    for(int i =60000; i<80000; i++){
////		    DFAgentDescription dfd = new DFAgentDescription();
////		    AID aidAgent1 = new AID("maurizio@napoli.com"+i, true);
////		    dfd.setName(aidAgent1);
////		    dfd.setLease(System.currentTimeMillis()+(600000*10));
////		    dfd.addLanguages("napoletano");
////		    dfd.addLanguages("SQL");
////		    ServiceDescription sd= new ServiceDescription();
////		    sd.setType("seller");
////		    dfd.addServices(sd);
////			
////			db.register("maurizio@napoli.com"+i, dfd);
//// 		}
//// 		System.out.println("FINITO 80000");
//////
//
//
//////
//////		// SEARCH
////		DFAgentDescription dfdTempalete = new DFAgentDescription();
////		dfdTempalete.addOntologies("meeting-scheduler");
////		dfdTempalete.addLanguages("fipa-sl0");
////		dfdTempalete.addLanguages("kif");
////		ServiceDescription sdT = new ServiceDescription();
////		sdT.setName("profiling");
////		sdT.setType("user-profiling");
////		dfdTempalete.addServices(sdT);
////		
////		db.search(dfdTempalete);
//
//
////		DFAgentDescription dfdT = new DFAgentDescription();
////		dfdT.addLanguages("napoletano");
////		db.search(dfdT);
////
////
////		long endTime = System.currentTimeMillis();
////		
////		System.out.println("TEMPO IMPIEGATO: "+(endTime-startTime));
////
//
////		DFAgentDescription dfdT2 = new DFAgentDescription();
////		dfdT2.addLanguages("fipa-sl0");
////		dfdT2.addLanguages("kif");
////		ServiceDescription sdT2 = new ServiceDescription();
////		sdT2.addLanguages("SQL");
////		dfdT2.addServices(sdT2);
////		
////		db.search(dfdT2);
//
//
//	    //System.out.println("Effattuata registrazione! ");
//		//DEREGISTRO		
////		for(int i =0; i<1001; i++)
////		    db.deregister(new AID("rosalba@napoli.it"+i, true));
////	    db.deregister(aidAgent1);
////	    db.deregister(aidAgent2);
////	    System.out.println("Effattuata deregistrazione! ");

  }

}
