/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

package jade.domain.KBManagement;

//#MIDP_EXCLUDE_FILE

import java.util.Enumeration;
import java.util.Date;

import jade.util.leap.Iterator;

import jade.proto.SubscriptionResponder;
import jade.core.AID;
import jade.content.ContentManager;
import jade.content.abs.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.KBManagement.*;

/**
 * @author Elisabetta Cortese - TILab
 *
 */
public class DFMemKB extends MemKB{
	
	public DFMemKB(){
		super();
		clean();
	}

	public DFMemKB(int max){

		super();
		RESULT_TO_RETURN = max;
		if(RESULT_TO_RETURN > MAX_RESULT_TO_RETURN)
			RESULT_TO_RETURN = MAX_RESULT_TO_RETURN;

//		System.out.println("USO LA MEMORIA: "+RESULT_TO_RETURN);
		clean();
	}

	
	// clean
	//1. scorre tutta la hash
	//2. per ogni entry controlla il lease
	//3. per quelli scaduti li cancella dalla HashMap
	protected void clean(){
		Iterator iter = facts.values().iterator();		
		while(iter.hasNext()){
			DFAgentDescription dfd = (DFAgentDescription) iter.next();
			long leaseR = dfd.getRemainingLeaseTime();
			Date lease = dfd.getLeaseTime();
			if(leaseR < 0 && lease.compareTo(lm.INFINITE_LEASE_TIME) != 0 ){
				AID aidToClean = dfd.getName();
				facts.remove(aidToClean);
			}
		}
	}

	// match
	public boolean match(Object template, Object fact) {

		try {
		  DFAgentDescription templateDesc = (DFAgentDescription)template;
		  DFAgentDescription factDesc = (DFAgentDescription)fact;
		  
		  // Match name
		  AID id1 = templateDesc.getName();
		  if(id1 != null) {
		    AID id2 = factDesc.getName();
		    if((id2 == null) || (!matchAID(id1, id2)))
		      return false;
		  }
		  // Match lease ritorno false se il lease e' scaduto
		  Date lease = factDesc.getLeaseTime();	
		  long leaseR = factDesc.getRemainingLeaseTime();
		  
	    if(lease.compareTo(lm.INFINITE_LEASE_TIME) != 0 && leaseR < 0)
		      return false;
	
		  // Match protocol set
		  Iterator itTemplate = templateDesc.getAllProtocols();
		  while(itTemplate.hasNext()) {
		    String templateProto = (String)itTemplate.next();
		    boolean found = false;
		    Iterator itFact = factDesc.getAllProtocols();
		    while(!found && itFact.hasNext()) {
		      String factProto = (String)itFact.next();
		      found = templateProto.equalsIgnoreCase(factProto);
		    }
		    if(!found)
		      return false;
		  }
	
		  // Match ontologies set
		  itTemplate = templateDesc.getAllOntologies();
		  while(itTemplate.hasNext()) {
		    String templateOnto = (String)itTemplate.next();
		    boolean found = false;
		    Iterator itFact = factDesc.getAllOntologies();
		    while(!found && itFact.hasNext()) {
		      String factOnto = (String)itFact.next();
		      found = templateOnto.equalsIgnoreCase(factOnto);
		    }
		    if(!found)
		      return false;
		  }
	
		  // Match languages set
		  itTemplate = templateDesc.getAllLanguages();
		  while(itTemplate.hasNext()) {
		    String templateLang = (String)itTemplate.next();
		    boolean found = false;
		    Iterator itFact = factDesc.getAllLanguages();
		    while(!found && itFact.hasNext()) {
		      String factLang = (String)itFact.next();
		      found = templateLang.equalsIgnoreCase(factLang);
		    }
		    if(!found)
		      return false;
		  }
	
		  // Match services set
		  itTemplate = templateDesc.getAllServices();
		  while(itTemplate.hasNext()) {
		    ServiceDescription templateSvc = (ServiceDescription)itTemplate.next();
		    boolean found = false;
		    Iterator itFact = factDesc.getAllServices();
		    while(!found && itFact.hasNext()) {
		      ServiceDescription factSvc = (ServiceDescription)itFact.next();
		      found = matchServiceDesc(templateSvc, factSvc);
		    }
		    if(!found)
		      return false;
		  }
	
		  return true;
		}
		catch(ClassCastException cce) {
		  return false;
		}
	}

/*************** Main method, for testing purposes ******************************/
  public static void main(String[] args) {
  	
  		DFMemKB db = new DFMemKB();
  		db.setLeaseManager(new DFLeaseManager());

		long startTime = System.currentTimeMillis();  	

//		// CREO IL DFD
	    for(int i = 0; i<60000; i++){
		    DFAgentDescription dfd = new DFAgentDescription();
			AID aidAgent;
		    ServiceDescription sd = new ServiceDescription();
		    aidAgent = new AID("rosalba@napoli.it"+i, true);
		    aidAgent.addAddresses("http://cit.it");
		    dfd.setName(aidAgent);
		    dfd.setLeaseTime(new Date(System.currentTimeMillis()+(600000*24)));
		    dfd.addLanguages("potentino");
		    dfd.addLanguages("kif");
		    dfd.addProtocols("tlc-application-protocol");
		    sd.setName("wireless");
		    sd.setType("web-services");
		    sd.setOwnership("Free");
		    sd.addLanguages("SQL");
		    sd.addProtocols("FIPA-REQUEST");
		    dfd.addServices(sd);
		   	// REGISTRO
		    db.register("rosalba@napoli.it"+i, dfd);
	    }
		
		long endTime = System.currentTimeMillis();
		System.out.println("Registration time Mem: "+(endTime-startTime));

//		// CREO IL DFD
//	    for(int i = 0; i<5000; i++){
//		    DFAgentDescription dfd = new DFAgentDescription();
//			AID aidAgent;
//		    ServiceDescription sd = new ServiceDescription();
//		    aidAgent = new AID("rosalba@napoli.it"+i, true);
//		    aidAgent.addAddresses("http://cit.it");
//		    dfd.setName(aidAgent);
//		    dfd.setLease(System.currentTimeMillis()+(600000*24));
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
// 		System.out.println("FINITO");
//
//	    for(int i = 5000; i<10000; i++){
//		    DFAgentDescription dfd = new DFAgentDescription();
//		    AID aidAgent1 = new AID("filippo@lecce.com"+i, true);
//		    aidAgent1.addAddresses("http://lecce.com");
//		    dfd.setName(aidAgent1);
//		    dfd.setLease(System.currentTimeMillis()+(60000*30));
//		    dfd.addLanguages("kif");
//		    dfd.addLanguages("pugliese");
//		    ServiceDescription sd= new ServiceDescription();
//		    sd.setType("seller");
//		    dfd.addServices(sd);
//			
//			db.register("filippo@lecce.com"+i, dfd);
// 		}
// 		System.out.println("FINITO");
//		
//
//	    for(int i = 10000; i<20000; i++){
//		    DFAgentDescription dfd = new DFAgentDescription();
//		    AID aidAgent2 = new AID("Lisa@napoli.it"+i, true);
//		    dfd.setName(aidAgent2);
//		    dfd.setLease(System.currentTimeMillis()+(600000*48));
//		    dfd.addProtocols("FIPA-REQUEST");
//		    dfd.addLanguages("napoletano");
//		    dfd.addLanguages("kif");
//		    ServiceDescription sd= new ServiceDescription();
//		    sd.setName("pizzaiola");
//		    sd.setType("food");
//		    dfd.addServices(sd);
//			
//			db.register("lisa@napoli.it"+i,dfd);
// 		}
// 		System.out.println("FINITO");
////
////
//	    for(int i = 20000; i<30000; i++){
//		    DFAgentDescription dfd = new DFAgentDescription();
//		    AID aidAgent2 = new AID("infinito@napoli.it"+i, true);
//		    dfd.setName(aidAgent2);
//		    dfd.setLease(-1);
//		    dfd.addLanguages("SQL");
//		    dfd.addLanguages("kif");
//			db.register("infinito@napoli.it"+i,dfd);
// 		}
// 		System.out.println("FINITO");
//
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
//		DFAgentDescription dfdT = new DFAgentDescription();
//		dfdT.addLanguages("napoletano");
//		db.search(dfdT);
//
//
//		long endTime = System.currentTimeMillis();
//		
//		System.out.println("TEMPO IMPIEGATO: "+(endTime-startTime));
//
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
////		DFAgentDescription dfdT = new DFAgentDescription();
////		dfdT.addLanguages("napoletano");
////		//dfdT.setLease(System.currentTimeMillis());		
////		db.search(dfdT);
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
