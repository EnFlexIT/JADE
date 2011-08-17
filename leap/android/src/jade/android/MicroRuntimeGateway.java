/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.android;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Event;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import jade.wrapper.gateway.GatewayAgent;
import jade.wrapper.gateway.GatewayListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
public class MicroRuntimeGateway {
	private static Logger logger = Logger.getMyLogger(MicroRuntimeGateway.class.getName());
	
	private static MicroRuntimeGateway theGateway;

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;

	private ServiceConnection serviceConnection;

	private Context context;

	private Properties profile;
	private Properties originalProfile;
	
	private String agentName;

	private List<GatewayListener> listeners;

	private GatewayListener[] listenersArray;
	
	private Thread gatewayAgentThread;

	public static final MicroRuntimeGateway getInstance(){
		if(theGateway == null){
			theGateway = new MicroRuntimeGateway();
		}
		return theGateway;
	}
	
	public void init(Context context, Properties profile){
		this.context = context;
		this.originalProfile = profile;
	}
	
	private MicroRuntimeGateway() {
		this.listeners = new ArrayList<GatewayListener>();
		this.listenersArray = new GatewayListener[0];
	}

	public void addListener(GatewayListener l) {
		listeners.add(l);
		listenersArray = listeners.toArray(new GatewayListener[0]);
	}

	public void removeListener(GatewayListener l) {
		if (listeners.remove(l))
			listenersArray = listeners.toArray(new GatewayListener[0]);
	}

	public final void execute(Object command) throws StaleProxyException, ControllerException, InterruptedException {
		execute(command, 0);
	}

	public final void execute(Object command, long timeout) throws StaleProxyException, ControllerException, InterruptedException {
		if(Thread.currentThread() == gatewayAgentThread)
			throw new IllegalStateException("Synchronous execute cannot be invoked by the agent thread");
		final Event ev = new Event(-1, this);
		execute(command, new RuntimeCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				ev.notifyProcessed(null);
			}

			@Override
			public void onFailure(Throwable throwable) {
				ev.notifyProcessed(throwable);
			}
		});
		Throwable t = (Throwable) ev.waitUntilProcessed(timeout);
		if(t!=null){
			if (t instanceof ControllerException) {
				throw (ControllerException) t;
			}
			else {
				throw new ControllerException(t);
			}
		}
	}

	public final void execute(final Object command, final RuntimeCallback<Void> cmdCallback) {
		if (microRuntimeServiceBinder == null) {
			logger.info("MicroRumtimeGateway binding to service");
			// We are not bound to the MicroRuntimeService yet
			bind(new RuntimeCallback<AgentController>() {

				@Override
				public void onSuccess(AgentController agent) {
					// The Gateway bound to the MicroRuntimeService and the GatewayAgent successfully started
					submit(agent, command, cmdCallback);
				}

				@Override
				public void onFailure(Throwable throwable) {
					cmdCallback.onFailure(throwable);
				}
			});	
		}
		else {
			logger.info("MicroRumtimeGateway already binded to service");
			submit(command, cmdCallback);
		}
	}
	
	private final void submit(final Object command, final RuntimeCallback<Void> cmdCallback) {
		try {
			submit(MicroRuntime.getAgent(agentName), command, cmdCallback);
		}
		catch (ControllerException ce) {
			logger.info("activating agent...");
			// The agent is not active --> Create it and retry
			activateAgent(new RuntimeCallback<AgentController>() {

				@Override
				public void onSuccess(AgentController agent) {
					submit(agent, command, cmdCallback);
				}

				@Override
				public void onFailure(Throwable throwable) {
					cmdCallback.onFailure(throwable);
				}
			});
		}
	}

	private final void submit(AgentController agent, Object command, RuntimeCallback<Void> callback) {
		AsynchCommandInfo info = new AsynchCommandInfo(command, callback);
		try {
			logger.info("passing command to agent...");
			agent.putO2AObject(info, AgentController.ASYNC);
		} catch (StaleProxyException e) {
			// should never happen
			logger.info("error passing command to agent...");
			callback.onFailure(e);
		}
	}

	private void bind(final RuntimeCallback<AgentController> agentStartupCallback) {
		serviceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
				logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
				activateAgent(agentStartupCallback);
			};

			public void onServiceDisconnected(ComponentName className) {
				microRuntimeServiceBinder = null;
				logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
			}
		};

		logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");

		context.bindService(new Intent(context, MicroRuntimeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	private void activateAgent(final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			// The container is down --> Start it first
			initProfile();
	
			microRuntimeServiceBinder.startAgentContainer(profile, new RuntimeCallback<Void>() {
				@Override
				public void onSuccess(Void thisIsNull) {
					logger.log(Level.INFO, "Gateway Container successfully started");
					activateAgent(agentStartupCallback);
				}
	
				@Override
				public void onFailure(Throwable throwable) {
					logger.log(Level.WARNING, "Gateway Container startup error.", throwable);
					agentStartupCallback.onFailure(throwable);
				}
			});
		}
		else {
			microRuntimeServiceBinder.startAgent("Control-%C", AndroidGatewayAgent.class.getName(), new Object[]{new ListenerImpl()}, new RuntimeCallback<Void>() {
				@Override
				public void onSuccess(Void thisIsNull) {
					logger.log(Level.INFO, "Gateway Agent successfully started");
					try {
						agentName = "Control-"+ profile.getProperty(MicroRuntime.CONTAINER_NAME_KEY);
						agentStartupCallback.onSuccess(MicroRuntime.getAgent(agentName));
					} catch (ControllerException e) {
						// should never happen
						agentStartupCallback.onFailure(e);
					}
				}

				@Override
				public void onFailure(Throwable throwable) {
					logger.log(Level.WARNING, "Gateway Agent startup error.", throwable);
					agentStartupCallback.onFailure(throwable);
				}
			});
		}
	}

	private void initProfile() {
		//we need to clone the initialization properties to be sure that we start from a clean situation, also when the JADE Runtime
		//is started more than one time.
		profile = (Properties) originalProfile.clone();
		profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		profile.setProperty(Profile.JVM, Profile.ANDROID);

		if (AndroidHelper.isEmulator()) {
			// Emulator: this is needed to work with emulated devices
			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
		}
		else {
			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.getLocalIPAddress());
		}

		// Emulator: this is not really needed on a real device
		profile.setProperty(Profile.LOCAL_PORT, "2000");
		
	}

	
	/**
	 * Inner class ListenerImpl
	 */
	private class ListenerImpl implements GatewayListener {
		public void handleGatewayConnected() {
			gatewayAgentThread = Thread.currentThread();
			Thread t = new Thread() {
				public void run() {
					for (GatewayListener listener : listenersArray) {
						try {
							listener.handleGatewayConnected();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			t.start();
		}
	
		public void handleGatewayDisconnected() {
			Thread t = new Thread() {
				public void run() {
					for (GatewayListener listener : listenersArray) {
						try {
							listener.handleGatewayDisconnected();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
			t.start();
		}
	} // END of inner class ListenerImpl
}
