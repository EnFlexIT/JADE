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


package examples.subdf;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;
import jade.domain.RequestDFActionBehaviour;
import jade.lang.acl.ACLMessage;
/**
This is an example of an agent that plays the role of a sub-df by 
registering with a parent DF.
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

public class SubDF extends jade.domain.df {

  // This behaviour implements the RequestDFActionBehaviour in order to perform a register action with a DF
	private class myRegisterWithDFBehaviour extends RequestDFActionBehaviour
  {
  	jade.domain.df mySelf;
  	
  	myRegisterWithDFBehaviour(jade.domain.df a,String dfName,AgentManagementOntology.DFAgentDescriptor dfd) throws FIPAException
  	{
  		super(a,dfName,AgentManagementOntology.DFAction.REGISTER, dfd);
  		mySelf = a;
  	}
  	
  	public void handleInform(ACLMessage msg)
  	{
  	
  		addParent(msg.getSource());
  		mySelf.showGui();
  	}
  }
  
	public void setup() {

    // Input df name
    int len = 0;
    byte[] buffer = new byte[1024];

   try {

      String parentName = "df";
      String insertedparentName = null;
      BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));

      System.out.print("Enter parent DF name (ENTER uses platform default DF): ");
      
      insertedparentName = buff.readLine();

      			
			if (insertedparentName.length() != 0)
				parentName=insertedparentName;
			
			AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
      dfd.setName(getName());
      dfd.addAddress(getAddress());
      dfd.setType("fipa-df");
      dfd.addInteractionProtocol("fipa-request");
      dfd.setOntology("fipa-agent-management");
      dfd.setOwnership("JADE");
      dfd.setDFState("active");

      AgentManagementOntology.ServiceDescriptor sd = new AgentManagementOntology.ServiceDescriptor();
      sd.setName(getLocalName() + "-sub-df");
      sd.setType("fipa-df");

      dfd.addAgentService(sd);
      
      addBehaviour(new myRegisterWithDFBehaviour(this,parentName,dfd));
		
      //Execute the setup of jade.domain.df which includes all the default behaviours of a df 
      //(i.e. register, unregister,modify, and search).
     super.setup();
     
     //Use this method to modify the current description of this df. 
     setDescriptionOfThisDF(getDescription());
     
     //Show the default Gui of a df.
     //super.showGui();
    
    }catch(InterruptedIOException iioe) {
      doDelete();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
    catch(FIPAException fe){fe.printStackTrace();}
  }
  
  private AgentManagementOntology.DFAgentDescriptor getDescription()
  {
  	AgentManagementOntology.DFAgentDescriptor out = new AgentManagementOntology.DFAgentDescriptor();
  	
		out.setName(getName());
		out.addAddress(getAddress());
		try{
		  	out.setOwnership(InetAddress.getLocalHost().getHostName());
		}catch (java.net.UnknownHostException uhe){
		 out.setOwnership("unknown");}
		 out.setType("fipa-df");
		 out.setDFState("active");
		 out.setOntology("fipa-agent-management");
		  //thisDF.setLanguage("SL0"); not exist method setLanguage() for dfd in Fipa97
		 out.addInteractionProtocol("fipa-request");
		 out.addInteractionProtocol("fipa-contract-net");
		 AgentManagementOntology.ServiceDescriptor sd = new 	AgentManagementOntology.ServiceDescriptor();
	   sd.setType("fipa-df");
	   sd.setName("federate-df");
	   sd.setOntology("fipa-agent-management");
		 out.addAgentService(sd);
		 return out;
  }

}