package jade.core;

import java.util.Enumeration;
import java.util.Vector;

import jade.imtp.leap.JICP.JICPProtocol;
import jade.lang.acl.ACLMessage;
import jade.security.JADESecurityException;
import jade.util.leap.Properties;

class BackEndWrapper implements BackEnd {
	private static final String CONN_MGR_CLASS_DEFAULT = "jade.imtp.leap.JICP.BIFEDispatcher";
	
	private FrontEndContainer myContainer;
	private Properties properties;
	private FEConnectionManager connectionManager;
	private BackEnd backEnd;
	
	BackEndWrapper(FrontEndContainer fec, Properties pp) throws Exception {
		myContainer = fec;
		properties = pp;
		
		// Load the FEConnectionManager
		String connMgrClass = properties.getProperty(MicroRuntime.CONN_MGR_CLASS_KEY);
		if (connMgrClass == null) {
			connMgrClass = CONN_MGR_CLASS_DEFAULT;
		}	
		connectionManager = (FEConnectionManager) Class.forName(connMgrClass).newInstance();
		
		if (properties.getProperty(MicroRuntime.PLATFORM_KEY) != null) {
			// Platform-name specified --> Start detached
			// Adjust container-name property: msisdn key takes precedence over container-name key 
			String containerName = properties.getProperty(JICPProtocol.MSISDN_KEY);
			if (containerName == null) {
				containerName = properties.getProperty(MicroRuntime.CONTAINER_NAME_KEY);
				if (containerName == null) {
					containerName = JICPProtocol.DUMMY_ID;
				}
			}
			properties.put(MicroRuntime.CONTAINER_NAME_KEY, containerName);
			// Adjust bootstrap agents as if they were validated by the platform
			String agents = properties.getProperty(MicroRuntime.AGENTS_KEY);
			try {
				Vector specs = Specifier.parseSpecifierList(agents);
				Enumeration e = specs.elements();
				while (e.hasMoreElements()) {
					Specifier sp = (Specifier) e.nextElement();
					sp.setClassName(sp.getName());
					sp.setArgs(null);
				}
				properties.put(MicroRuntime.AGENTS_KEY, Specifier.encodeSpecifierList(specs));
			}
			catch (Exception e) {
				// This should never happen since agent specifiers have already been successfully parsed once!
				e.printStackTrace();
			}
			// Set the container name as the mediator-ID --> This will force the BE to resynch as soon as we will attach to the platform
			properties.put(JICPProtocol.MEDIATOR_ID_KEY, properties.getProperty(MicroRuntime.CONTAINER_NAME_KEY));

			myContainer.initInfo(properties);
		}
		else {
			// Platform-name not specified --> Attach immediately
			attach();
		}
		
	}
	
	public String bornAgent(String name) throws IMTPException, JADESecurityException {
		if (backEnd != null) {
			return backEnd.bornAgent(name);
		}
		else {
			return name;
		}
	}

	public void deadAgent(String name) throws IMTPException {
		if (backEnd != null) {
			backEnd.deadAgent(name);
		}
	}

	public void suspendedAgent(String name) throws NotFoundException, IMTPException {
		if (backEnd != null) {
			backEnd.suspendedAgent(name);
		}
	}

	public void resumedAgent(String name) throws NotFoundException, IMTPException {
		if (backEnd != null) {
			backEnd.resumedAgent(name);
		}
	}

	public void messageOut(ACLMessage msg, String sender) throws NotFoundException, IMTPException {
		if (backEnd == null) {
			// Force actual BE creation
			attach();
		}
		backEnd.messageOut(msg, sender);
	}

	public Object serviceInvokation(String actor, String serviceName, String methodName, Object[] methodParams) throws NotFoundException, ServiceException, IMTPException {
		if (backEnd == null) {
			// Force actual BE creation
			attach();
		}
		return backEnd.serviceInvokation(actor, serviceName, methodName, methodParams);
	}

	void detach() {
		if (backEnd != null) {
			connectionManager.shutdown();
			backEnd = null;
		}
	}
	
	private void attach() throws IMTPException {
		backEnd = connectionManager.getBackEnd(myContainer, properties);
		myContainer.initInfo(properties);
	}
}
