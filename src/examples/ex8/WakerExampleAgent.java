package Support;

import jade.core.Agent;
import java.util.*;

public class WakerExampleAgent extends Agent {

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
  reset(t);
}
}



protected void setup() {
  System.out.println(getLocalName()+" setup executed at date "+(new Date()).toString()); 
  addBehaviour(new Example1Behaviour(this,10000));
  addBehaviour(new Example2Behaviour(this,new Date(System.currentTimeMillis()+15000)));
}
}
