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

package jade.core.behaviours;

//#MIDP_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.leap.LEAPCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.domain.mobility.*;
import jade.domain.FIPAAgentManagement.ExceptionVocabulary;
import jade.util.leap.List;
import jade.util.leap.Iterator;

import java.util.Hashtable;
import java.util.zip.*;
import java.io.*;

/**
   This behaviour serves behaviour-loading requests
   according to the Behaviour-loading ontology.
   When an agent runs an instance of this behaviour it becomes able
   to load and execute completely new behaviours i.e. behaviours
   whose code is not included in the classpath of the JVM where the 
   agent lives.
   Loading behaviour requests must have the <code>ACLMessage.REQUEST</code>
   performative and must use the BehaviourLoading ontology and the
   LEAP language.
   @see jade.domain.mobility.LoadBehaviour
   @see jade.domain.mobility.BehaviourLoadingOntology
   @see jade.content.lang.leap.LEAPCodec
   @author Giovanni Caire - TILAB
 */
public class LoaderBehaviour extends Behaviour {
	private Codec codec = new LEAPCodec();
	private Ontology onto = BehaviourLoadingOntology.getInstance();
	private ContentManager myContentManager = new ContentManager();
	
	private MessageTemplate myTemplate = MessageTemplate.and(
		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
		MessageTemplate.and(
			MessageTemplate.MatchLanguage(codec.getName()),
			MessageTemplate.MatchOntology(onto.getName())
		)
	);
	
	private boolean finished = false;
	
	public LoaderBehaviour() {
		super();
		init();
	}
	
	public LoaderBehaviour(Agent a) {
		super(a);
		init();
	}
	
	public void action() {
		if (!finished) {
			ACLMessage msg = myAgent.receive(myTemplate);
			if (msg != null) {
				ACLMessage reply = msg.createReply();
				
				if (accept(msg)) {
					try {
						LoadBehaviour lb = (LoadBehaviour) myContentManager.extractContent(msg);
						
						// Load the behaviour
						String className = lb.getClassName();
						Behaviour b = null;
						byte[] code = lb.getCode();
						byte[] zip = lb.getZip();
						if (code != null) {
							// Try from the class byte code
							b = loadFromCode(className, code);
						}
						else if (zip != null) {
							// Try from the zip
							b = loadFromZip(className, zip);
						}
						else {
							// Try from the local classpath
							b = (Behaviour) Class.forName(className).newInstance();
						}
						
						// Set parameters
						setParameters(b, lb.getParameters());
						
						// Start the behaviour and prepare a positive reply
						addBehaviour(b, msg);
						reply.setPerformative(ACLMessage.INFORM);
					}
					catch (Codec.CodecException ce) {
						ce.printStackTrace();
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent("(("+ExceptionVocabulary.UNRECOGNISEDVALUE+" content))");
					}
					catch (OntologyException oe) {
						oe.printStackTrace();
						reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
						reply.setContent("(("+ExceptionVocabulary.UNRECOGNISEDVALUE+" content))");
					}
					catch (Exception e) {
						e.printStackTrace();
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("((internal-error \""+e.toString()+"\"))");
					}
				}
				else {
					reply.setPerformative(ACLMessage.REFUSE);
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}
	
	public boolean done() {
		return finished;
	}

	/**
	   Make this behaviour terminate.
	 */
	public void stop() {
		finished = true;
		restart();
	}

	/**
	   Subclasses may redefine this method to handle the behaviour 
	   addition operation in an application specific way.
	   @param b The <code>Behaviour</code> to be added.
	   @param request The <code>ACLMessage</code> carrying the 
	   <code>LoadBehaviour</code> request.
	 */
	protected void addBehaviour(Behaviour b, ACLMessage request) {
		myAgent.addBehaviour(b);
	}

	/**
	   Suclasses myredefine this method to prevent the behaviour 
	   loading operation under specific conditions
	 */
	protected boolean accept(ACLMessage msg) {
		return true;
	}
	
	///////////////////////////
	// Private utility methods
	///////////////////////////
	private void init() {
		// Register LEAP language and BehaviourLoading ontology in a
		// local ContentManager
		myContentManager.registerLanguage(codec);
		myContentManager.registerOntology(onto);
	}
		
	private Behaviour loadFromCode(String className, final byte[] code) throws ClassNotFoundException, InstantiationException, IllegalAccessException {	
		Hashtable classes = new Hashtable(1);
		classes.put(className, code);
		return load(className, classes);
	}
	
	private Behaviour loadFromZip(String className, byte[] zip) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Hashtable classes = new Hashtable();
		try {
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip));
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] tmp = new byte[1024];
				int k = zis.read(tmp, 0, tmp.length);
				while (k > 0) {
					baos.write(tmp, 0, k);
					k = zis.read(tmp, 0, tmp.length);
				}
				classes.put(ze.getName(), baos.toByteArray());
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			return load(className, classes);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ClassNotFoundException("Error reading zip for class "+className+". "+e);
		}
	}
	
	private Behaviour load(String className, Hashtable classes) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		ClassLoader loader = new HashClassLoader(classes, getClass().getClassLoader());
		
		Class c = loader.loadClass(className);
		return (Behaviour) c.newInstance();
	}
	
	private void setParameters(Behaviour b, List params) {
		DataStore ds = b.getDataStore();
		Iterator it = params.iterator();
		while (it.hasNext()) {
			Parameter p = (Parameter) it.next();
			ds.put(p.getName(), p.getValue());
		}
	}
	
	/**
	   Inner class HashClassLoader
	 */
	private class HashClassLoader extends ClassLoader {
		private Hashtable classes;
		
		public HashClassLoader(Hashtable ht, ClassLoader parent) {
			//#PJAVA_EXCLUDE_BEGIN
			super(parent);
			//#PJAVA_EXCLUDE_END
			/*#PJAVA_INCLUDE_BEGIN
			super();
			#PJAVA_INCLUDE_END*/
			classes = ht;
		}
			
    protected Class findClass(String name) throws ClassNotFoundException {
	    String fileName = name.replace('.', '/') + ".class";
    	byte[] code = (byte[]) classes.get(fileName);
    	if (code != null) {
    		System.out.println("Definig class "+name+". code is "+code.length);
		  	return defineClass(name, code, 0, code.length);
    	}
    	else {
    		throw new ClassNotFoundException("Class "+name+" does not exist");
    	}
  	}

		/*#PJAVA_INCLUDE_BEGIN In PersonalJava loadClass(String, boolean) is abstract --> we must implement it
    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
	  	// 1) Try to see if the class has already been loaded
	  	Class c = findLoadedClass(name);
		  	
			// 2) Try to load the class using the system class loader
	  	if(c == null) {
		    try {
		        c = findSystemClass(name);
		    }
		    catch (ClassNotFoundException cnfe) {
		    }
			}
		  	
	  	// 3) If still not found, try to load the class from the code
	  	if(c == null) {
  	    c = findClass(name);
	  	}
		  	
	  	if(resolve) {
	  	    resolveClass(c);
	  	}
		    
	  	return c;
    }
		#PJAVA_INCLUDE_END*/
	}  // END of inner class HashClassLoader		
}