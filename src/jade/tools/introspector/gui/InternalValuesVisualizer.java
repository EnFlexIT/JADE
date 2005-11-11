package jade.tools.introspector.gui;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.*;
import jade.content.ContentManager;
import jade.content.onto.Ontology;
import jade.content.onto.basic.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.domain.FIPAException;
import jade.domain.introspection.IntrospectionOntology;
import jade.domain.introspection.GetKeys;
import jade.domain.introspection.GetValue;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class InternalValuesVisualizer extends JPanel {
	private Agent myAgent;
	private ContentManager myContentManager;
	private ACLMessage myRequest;
	private AID inspectedAgent;
	private String[] myKeys;
	private JTextField[] valuesTF;
	private JButton[] buttons;
	
	private static long cnt = 0; 
	
	private InternalValuesVisualizer(Agent a, ContentManager cm, ACLMessage request, AID id, String[] keys) {
		myAgent = a;
		myContentManager = cm;
		myRequest = request;
		inspectedAgent = id;
		myKeys = keys;
		valuesTF = new JTextField[myKeys.length];
		buttons = new JButton[myKeys.length];
		
		setLayout(new BorderLayout());
		
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
				
		for (int i = 0; i < myKeys.length; ++i) {
		    GridBagConstraints gridBagConstraints = new GridBagConstraints();
		    gridBagConstraints.gridx = 0;
		    gridBagConstraints.gridy = i;
		    gridBagConstraints.gridwidth = 2;
		    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			p.add(new JLabel(myKeys[i]), gridBagConstraints);
			
			valuesTF[i] = new JTextField(64);
			Dimension dim =  new Dimension(300, 24);
			valuesTF[i].setMinimumSize(dim);
			valuesTF[i].setPreferredSize(dim);
		    gridBagConstraints = new GridBagConstraints();
		    gridBagConstraints.gridx = 2;
		    gridBagConstraints.gridy = i;
		    gridBagConstraints.gridwidth = 3;
		    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			p.add(valuesTF[i], gridBagConstraints);
						
			buttons[i] = new JButton("Refresh");
			final int k = i;
			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					try {
						Object value = retrieveValue(myKeys[k]);
						if (value != null) {
							if (value.getClass().isArray()) {
								valuesTF[k].setText(stringifyArray((Object[]) value));
							}
							else {
								valuesTF[k].setText(value.toString());
							}
						}
						else {
							valuesTF[k].setText("null");
						}
					}
					catch (Exception e) {
						// FIXME: Show a warning dialog box
						e.printStackTrace();
					}
				}
			} );
		    gridBagConstraints = new GridBagConstraints();
		    gridBagConstraints.gridx = 5;
		    gridBagConstraints.gridy = i;
		    gridBagConstraints.gridwidth = 3;
		    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			p.add(buttons[i], gridBagConstraints);
		}
		
		add(p, BorderLayout.NORTH);
	}
	
	public static InternalValuesVisualizer createInternalValuesVisualizer(Agent a, AID id) throws Exception {
		ContentManager cm = new ContentManager();
		Ontology onto = IntrospectionOntology.getInstance();
		Codec codec = new SLCodec();
		cm.registerOntology(onto);
		cm.registerLanguage(codec);
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setLanguage(codec.getName());
		msg.setOntology(onto.getName());
		
		jade.content.onto.basic.Action actExpr = new jade.content.onto.basic.Action(id, new GetKeys());
		cm.fillContent(msg, actExpr);
		msg.clearAllReceiver();
		msg.addReceiver(id);
		String replyWith = "RW-"+System.currentTimeMillis()+"-"+cnt;
		cnt++;
		msg.setReplyWith(replyWith);
		a.send(msg);
		ACLMessage response = a.blockingReceive(MessageTemplate.MatchInReplyTo(replyWith), 10000);
		if (response != null) {
			if (response.getPerformative() == ACLMessage.INFORM) {
				Result r = (Result) cm.extractContent(response);
				
				jade.util.leap.List l = r.getItems();
				String[] keys = new String[l.size()];
				for (int i = 0; i < keys.length; ++i) {
					keys[i] = (String) l.get(i);
				}
				
				return new InternalValuesVisualizer(a, cm, msg, id, keys);
			}
			else {
				if (response.getPerformative() == ACLMessage.FAILURE && response.getSender().equals(a.getAMS())) {
					throw new FIPAException("Agent "+id.getName()+" not found");
				}
				else {
					throw new FIPAException("Error retrieving keys from agent "+id.getName()+": "+ACLMessage.getPerformative(response.getPerformative()));
				}
			}
		}
		else {
			throw new FIPAException("Error retrieving keys from agent "+id.getName()+": timeout expired.");
		}
	}
	
	private Object retrieveValue(String key) throws Exception {
		jade.content.onto.basic.Action actExpr = new jade.content.onto.basic.Action(inspectedAgent, new GetValue(key));
		myContentManager.fillContent(myRequest, actExpr);
		myRequest.clearAllReceiver();
		myRequest.addReceiver(inspectedAgent);
		String replyWith = "RW-"+System.currentTimeMillis()+"-"+cnt;
		cnt++;
		myRequest.setReplyWith(replyWith);
		myAgent.send(myRequest);
		ACLMessage response = myAgent.blockingReceive(MessageTemplate.MatchInReplyTo(replyWith), 10000);
		if (response != null) {
			if (response.getPerformative() == ACLMessage.INFORM) {
				Result r = (Result) myContentManager.extractContent(response);
				return r.getValue();
			}
			else {
				if (response.getPerformative() == ACLMessage.FAILURE && response.getSender().equals(myAgent.getAMS())) {
					throw new FIPAException("Agent "+inspectedAgent.getName()+" disappeared");
				}
				else {
					throw new FIPAException("Error retrieving value for key "+key+": "+ACLMessage.getPerformative(response.getPerformative()));
				}
			}
		}
		else {
			throw new FIPAException("Error retrieving value for key "+key+": timeout expired.");
		}
	}
	
	private String stringifyArray(Object[] array) {
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < array.length; ++i) {
			sb.append(array[i].toString());
			if (i < array.length - 1) {
				sb.append(", ");
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
