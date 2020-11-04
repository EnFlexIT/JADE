package examples.saveRestore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jade.core.Agent;
import jade.core.Restore;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This example shows how an agent can save its state somewhere (to a file in this
 * case) via the write() method and restore it at a later time via the restoreFields() 
 * method.
 * The Restore annotation can be used to customize the restoration mechanism either 
 * skipping the restoration of some fields or performing the restoration via a 
 * user-defined method when custom restoration is needed.
 */
public class RestoreAgent extends Agent {

	// Normal fields will be automatically restored
	private int cnt = 0;	
	private Map<Integer, String> informContentsMap = new TreeMap<Integer, String>();
	
	// This filed will not be restored
	@Restore(skip=true)
	private int cnt2 = 0;
	
	// This field will be restored via the user-defined method 
	@Restore(method="restoreContentsList")
	private List<String> informContentsList = new ArrayList<String>();
	
	protected void setup() {
		addBehaviour(new TickerBehaviour(this, 1000) {
			public void onTick() {
				cnt++;
				cnt2 += 2;
				System.out.println("cnt="+cnt+", cnt2="+cnt2);
			}
		});
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						System.out.println("Inform received. Content: "+msg.getContent());
						informContentsMap.put(cnt, msg.getContent());
						informContentsList.add(msg.getContent());
						System.out.println("Inform received up to now:");
						for (int cntVal : informContentsMap.keySet()) {
							System.out.println("Counter "+cntVal+": "+informContentsMap.get(cntVal));
						}
						System.out.println("List: "+informContentsList);
					}
					else if (msg.getPerformative() == ACLMessage.REQUEST) {
						String filename = getLocalName()+"_state.ser";
						if ("SAVE".equalsIgnoreCase(msg.getContent())) {
							System.out.println("SAVE Request received");
							try {
								myAgent.write(new FileOutputStream(filename));
							}
							catch (Exception e) {
								System.out.println("Error saving agent state to file "+filename);
								e.printStackTrace();
							}
						}
						else if ("RESTORE".equalsIgnoreCase(msg.getContent())) {
							System.out.println("RESTORE Request received");
							try {
								myAgent.restoreFields(new FileInputStream(filename));
							}
							catch (Exception e) {
								System.out.println("Error restoring agent state from file "+filename);
								e.printStackTrace();
							}
						}
					}
				}
				else {
					block();
				}
			}
		});
	}
	
	// User defined restoration method for the informContentsList field
	private void restoreContentsList(List l) {
		informContentsList.clear();
		informContentsList.addAll(l);
	}
}
