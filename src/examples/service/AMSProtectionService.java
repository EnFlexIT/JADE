package examples.service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.AgentContainer;
import jade.core.BaseService;
import jade.core.Filter;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.VerticalCommand;
import jade.core.messaging.GenericMessage;
import jade.core.messaging.MessagingSlice;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.security.JADESecurityException;

/** 
 * This class provides an example of how a service can prevent certain 
 * platform management actions (e.g. ShutdownPlatform or KillContainer) to be requested 
 * to the AMS by application agents.<br>
 * The service gets a permission configuration file (permissions.properties) containing 
 * rows of the form<br>
 * management-action = list of agents allowed to request the action<br>
 * <br> 
 * e.g.<br>
 * ShutdownPlatform = rma<br>
 * KillContainer = rma, foo, bar<br>
 * 
 * In the above example a REQUEST to perform the ShutdownPlatform action will be allowed 
 * only if requested by an agent called rma. Similarly a REQUEST to perform the KillContainer 
 * action will be allowed only if requested by an agent called rma or foo or bar.<br>
 * All actions not explicitly mentioned in the permission configuration file will 
 * be allowed regardless of who is requesting it.<br>
 * <br>
 * By default agents living in the Main-Container are trusted and can request any action.
 * This behaviour can be overwritten by setting the examples_service_AMSProtectionService_trustlocalagents configuration
 * property to false.
 * <br>
 * NOTE: This class is provided as example and is not intended to be a real security service.
 * 
 * @author Caire
 *
 */
public class AMSProtectionService extends BaseService {
	public static final String NAME = "AMSProtection";
	
	public static final String PERMISSIONS_FILE = "examples_service_AMSProtectionService_permissionsfile";
	public static final String TRUST_LOCAL_AGENTS = "examples_service_AMSProtectionService_trustlocalagents";
	

	private AgentContainer myContainer;
	private Map<String, List<String>> permissions = new HashMap<String, List<String>>();
	private ContentManager cm = new ContentManager();
	
	private Filter incomingFilter  = new AMSProtectionFilter();

	// Service Name
	@Override
	public String getName() {
		return NAME;
	}
	
	// Service Initialization
	@Override
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		// Check the service is installed in the Main Container
		if (!p.isMain()) {
			throw new ProfileException(NAME+" service must be installed in the Main-Container");
		}

		// Read permissions from the permissions file
		String permissionsFile = p.getParameter(PERMISSIONS_FILE, "permissions.properties");
		Properties pp = new Properties();
		try {
			pp.load(new FileReader(permissionsFile));
			Set<String> actions = pp.stringPropertyNames();
			for (String a : actions) {
				String str = pp.getProperty(a);
				String[] names = str.split(",");
				List<String> allowedAgents = new ArrayList<String>();
				for (String n : names) {
					allowedAgents.add(n.trim());
				}
				permissions.put(a, allowedAgents);
			}
		}
		catch (Exception e) {
			throw new ProfileException("Cannot read permissions file "+permissionsFile, e);
		}
		
		// Initialize the ContentManager used to inspect incoming AMS requests
		// According to FIPA specs actions can be requested to the AMS in all possible SL styles
		Codec codec = new SLCodec();
		cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);
		cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL1);
		cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL2);
		cm.registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);
		cm.registerOntology(JADEManagementOntology.getInstance());
		
		// If local agents are trusted, keep a handle to the local container to skip checks when 
		// incoming requests are from local agents
		if (p.getBooleanProperty(TRUST_LOCAL_AGENTS, true)) {
			myContainer = ac;
		}
	}

	
	@Override
	public Filter getCommandFilter(boolean direction) {
		if (direction == Filter.INCOMING) {
			return incomingFilter;
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Inner class AMSProtectionFilter.
	 * This is the Filter intercepting SEND_MESSAGE incoming commands end 
	 * blocking those representing REQUESTs to the AMS from not allowed agents
	 */
	private class AMSProtectionFilter extends Filter {
		
		@Override
		protected boolean accept(VerticalCommand cmd) {
			if (cmd.getName().equals(MessagingSlice.SEND_MESSAGE)) {
				AID sender = (AID) cmd.getParam(0);
				if (sender != null) {
					GenericMessage gMsg = (GenericMessage) cmd.getParam(1);
					ACLMessage msg = gMsg.getACLMessage();
					AID receiver = (AID) cmd.getParam(2);

					if (receiver.getLocalName().equalsIgnoreCase("ams") && msg.getPerformative() == ACLMessage.REQUEST) {
						// This is a REQUEST for the AMS
						if (myContainer != null && myContainer.isLocalAgent(sender)) {
							// Sender is a trusted local agent --> skip checks
						}
						else {
							// Check if sender is allowed 
							try {
								Action actExpr = (Action) cm.extractContent(msg);
								String requestedAction =  actExpr.getAction().getClass().getSimpleName();
								List<String> allowedAgents = permissions.get(requestedAction);
								boolean allow = allowedAgents == null || allowedAgents.contains(sender.getLocalName());
								String checkResult = allow ? "ALLOW" : "DENY";
								System.out.println("Agent "+sender.getLocalName()+" requesting action "+requestedAction+": "+checkResult);
								if (!allow) {
									// Not allowed!! Block the command and set an Exception as return-value
									// so that the sender will receive back a FAILURE
									cmd.setReturnValue(new JADESecurityException(requestedAction+" not allowed"));
									return false;
								}
							}
							catch (Exception e) {
								System.out.println("Error decoding incoming AMS REQUEST. "+e);
								
							}
						}
					}
				}
			}
			// Do not block the command
			return true;
		}
	}
}
