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

package examples.behaviours;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

import java.util.Date;

/**
This example shows the usage of the WakerBehaviour. The agent object, at its setup, adds
2 WakerBehaviours. The first behaviour wakes up after the given timeout. The second
behaviour wakes up at the given date and time. When they wake up, the method
<code>handleElapsedTimeout()</code> is executed.
Notice that in the first behaviour, the execution of the method <code>reset(t)</code>
makes repetitive the behaviour itself; that is it wakes up every <code>t</code> milliseconds.
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class WakerAgent extends Agent {

public class Example1Behaviour extends WakerBehaviour {
  private long t;
public Example1Behaviour(Agent a, long timeout){
  super(a,timeout);
  t=timeout;
}
protected void handleElapsedTimeout() {
  System.out.println(myAgent.getLocalName()+" elapsed timeout at date "+(new Date()).toString()+" and called method handleElapsedTimeout in example1.");
  reset(t);
}
}



public class Example2Behaviour extends WakerBehaviour {
  private long t;
public Example2Behaviour(Agent a, Date time){
  super(a,time);
  t=time.getTime()-System.currentTimeMillis();
}
protected void handleElapsedTimeout() {
  System.out.println(myAgent.getLocalName()+" elapsed timeout at date "+(new Date()).toString()+" and called method handleElapsedTimeout in example2.");
  
}
}



protected void setup() {
  System.out.println(getLocalName()+" setup executed at date "+(new Date()).toString()); 
  addBehaviour(new Example1Behaviour(this,10000));
  addBehaviour(new Example2Behaviour(this,new Date(System.currentTimeMillis()+15000)));
}
}
