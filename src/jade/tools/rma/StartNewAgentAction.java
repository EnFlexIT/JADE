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

package jade.tools.rma;



import java.awt.Frame;
import jade.gui.AgentTree;
import jade.Boot;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */
class StartNewAgentAction extends ContainerAction {

  private rma myRMA;
  private Frame mainWnd;

  public StartNewAgentAction(rma anRMA, Frame f,ActionProcessor actPro) {
    super ("StartNewAgentActionIcon","Start New Agent",actPro);
    myRMA = anRMA;
    mainWnd = f;
  }

  public void doAction(AgentTree.ContainerNode node ) {

               String containerName = node.getName();
              int result = doStartNewAgent(containerName);
  }

  private int doStartNewAgent(String containerName) {
    int result = StartDialog.showStartNewDialog(containerName, mainWnd);
    if (result == StartDialog.OK_BUTTON) {

      String agentName = StartDialog.getAgentName();
      String className = StartDialog.getClassName();
      String container = StartDialog.getContainer();
      String arguments = StartDialog.getArguments();
     
      if((agentName.trim().length() > 0) && (className.trim().length() >0))
      {
      	  String agentSpecifier = agentName + ":" + className + "(" + arguments +")";
      	  //not remove '"'and '\'
      	  ArrayList al = Boot.T2(agentSpecifier,true);
      	  
      	  ArrayList argList = new ArrayList();
      	  //return a list of lists in the form [[agentName1, class, arg1...argN]....[agentNameN, class, arg1, ...argM]]
        	for (Iterator it = Boot.getCommandLineAgentSpecifiers(al); it.hasNext();)
          {
    	      List l1 = (List)it.next();
          	//System.out.println("Agent Name: "+ l1.get(0));
    	      //System.out.println("Agent class: " + l1.get(1));
    	      for(int i = 2; i<l1.size(); i++)
    		     {
    		     	//System.out.println("Arg["+i+"]: " +l1.get(i));
    		     	argList.add(l1.get(i));
    		     }
           }
          
          String[] arg = new String[argList.size()];
      	  for(int n=0; n<argList.size(); n++)
      	  	arg[n] = (String)argList.get(n);
      	  
      	  myRMA.newAgent(agentName, className, arg, container);
      }
    }
    return result;
  }

}  // End of StartNewAgentAction









