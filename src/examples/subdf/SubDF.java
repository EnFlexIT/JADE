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
import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;

/**
This is an example of an agent that plays the role of a sub-df by 
registering with a parent DF.
@author Giovanni Rimassa - Università di Parma
@version $Date$ $Revision$
*/

public class SubDF extends jade.domain.df {

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

      try {
      	registerWithDF(parentName, dfd);
      }catch(FIPAException fe) {
       fe.printStackTrace();
      }
		
      //Execute the setup of jade.domain.df which includes all the default behaviours of a df 
      //(i.e. register, unregister,modify, and search).
     super.setup();
     //Show the default Gui of a df.
     super.showGui();
    
    }catch(InterruptedIOException iioe) {
      doDelete();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }

  }


}