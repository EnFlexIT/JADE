package jade.onto;
import jade.domain.FIPAAgentManagement.AID;

public class Action {
AID actor;
Object action;
public void set_0(AID a) { actor=a;}
public AID get_0() {return actor;}
public void set_1(Object a) { action=a;}
public Object get_1() {return action;}
public AID getActor() { return get_0(); }
public void setActor(AID a) {set_0(a); }
public Object getAction() { return get_1();}
public void setAction(Object a) {set_1(a);}

}
