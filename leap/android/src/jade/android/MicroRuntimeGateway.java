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
	public enum ConnectionState {
		IN_PROGRESS, CONNECTED, DISCONNECTED
	};

	private static final String AGENT_NAME = "Control";
	
	private static Logger logger = Logger.getMyLogger(MicroRuntimeGateway.class.getName());

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;

	private ServiceConnection serviceConnection;

	private Context context;

	private Properties profile;
	
	private String agentName;
	
	private String agentClassName;

	private Object[] agentArguments;

	private List<GatewayListener> listeners;

	private GatewayListener[] listenersArray;
	
	private ConnectionState state;

	public MicroRuntimeGateway(Context context, Properties profile) {
		this.state = ConnectionState.IN_PROGRESS;
		
		this.context = context;
		
		this.profile = profile;
		
		this.listeners = new ArrayList<GatewayListener>();
		
		this.listenersArray = new GatewayListener[0];
		
		this.agentClassName = GatewayAgent.class.getName();
		
		this.agentName = AGENT_NAME + "-" + Integer.toHexString((int)System.currentTimeMillis());
		
		this.agentArguments = new Object[0];
	}

	public void checkJADE() {
	}

	public void restartJADE() {
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
		Event e = null;
		
		synchronized (this) {
			checkJADE();

			e = new Event(-1, command);

			AgentController agentController = MicroRuntime.getAgent(agentName);
			
			try {
				logger.log(Logger.INFO, "Requesting execution of command "+command);
			
				agentController.putO2AObject(e, AgentController.ASYNC);
			} catch (StaleProxyException exc) {
				exc.printStackTrace();

				restartJADE();

				agentController.putO2AObject(e, AgentController.ASYNC);
			}
		}

		e.waitUntilProcessed(timeout);
	}

	public final void execute(Object command, final RuntimeCallback<Void> callback) {
		execute(command, 0, callback);
	}
	
	public final void execute(Object command, long timeout, final RuntimeCallback<Void> callback) {
		final Event e = new Event(-1, command);

		final long finalTimeout = timeout;
		
		final Object finalCommand = command;
		
		new Thread() {
			@Override
			public void run() {
				try {
					synchronized (MicroRuntimeGateway.this) {
						checkJADE();

						AgentController agentController = MicroRuntime.getAgent(agentName);
						
						try {
							logger.log(Logger.INFO, "Requesting execution of command " + finalCommand);
						
							agentController.putO2AObject(e, AgentController.ASYNC);
						} catch (StaleProxyException exc) {
							exc.printStackTrace();

							restartJADE();

							agentController.putO2AObject(e, AgentController.ASYNC);
						}
					}

					e.waitUntilProcessed(finalTimeout);

					callback.onSuccess(null);
				} catch (Throwable throwable) {
					callback.onFailure(throwable);
				}
			}
		}.start();
	}

	public void connect() {
		logger.log(Level.INFO, "Creating service");

		bindMicroService();
	}

	public void disconnect() {
			if (microRuntimeServiceBinder == null)
				return;

			microRuntimeServiceBinder
					.stopAgentContainer(new RuntimeUICallback<Void>() {
						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.INFO, "Cannot stop micro container");

							throwable.printStackTrace();

							unbindService();

							handleGatewayDisconnected();
						}

						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Micro agent container stopped");

							unbindService();
							
							handleGatewayDisconnected();
						}
					});
	}

	private void bindMicroService() {
		serviceConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;

				logger.log(Level.INFO, "Service connected");

				startMicroContainer();
			};

			public void onServiceDisconnected(ComponentName className) {
				microRuntimeServiceBinder = null;

				logger.log(Level.INFO, "Service disconnected");
				
				handleGatewayDisconnected();
			}
		};

		logger.log(Level.INFO, "Binding service");

		context.bindService(new Intent(context,
				MicroRuntimeService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
	}

	private void unbindService() {
		if (serviceConnection != null)
			context.unbindService(serviceConnection);
	}

	private void startMicroContainer() {
		logger.log(Level.INFO, "Creating micro container");

		completeProfile(profile);

		microRuntimeServiceBinder.startAgentContainer(profile,
				new RuntimeUICallback<Void>() {
					@Override
					public void onSuccess(Void thisIsNull) {
						logger.log(Level.INFO, "Container started");

						startMicroAgent();
					}

					@Override
					public void onFailure(Throwable throwable) {
						logger.log(Level.INFO, "Cannot start container");

						throwable.printStackTrace();
						
						handleGatewayDisconnected();
					}
				});
	}

	private void startMicroAgent() {
		logger.log(Level.INFO, "Starting agent");

		microRuntimeServiceBinder.startAgent(agentName,
				agentClassName, agentArguments,
				new RuntimeUICallback<Void>() {
					@Override
					public void onSuccess(Void thisIsNull) {
						logger.log(Level.INFO, "Agent started");
						
						handleGatewayConnected();
					}

					@Override
					public void onFailure(Throwable throwable) {
						logger.log(Level.INFO, "Cannot start agent");

						throwable.printStackTrace();
						
						handleGatewayDisconnected();
					}
				});
	}

	private void completeProfile(Properties properties) {
		properties.setProperty(Profile.MAIN, Boolean.FALSE.toString());

		properties.setProperty(Profile.JVM, Profile.ANDROID);

		if (AndroidHelper.isEmulator())
			// Emulator: this is needed to work with emulated devices
			properties.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
		else
			properties.setProperty(Profile.LOCAL_HOST,
					AndroidHelper.getLocalIPAddress());

		// Emulator: this is not really needed on a real device
		properties.setProperty(Profile.LOCAL_PORT, "2000");
	}
	
	public void handleGatewayConnected() {
		if(state == ConnectionState.CONNECTED)
			return;
		
		state = ConnectionState.CONNECTED;
		
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
		if(state == ConnectionState.DISCONNECTED)
			return;
		
		state = ConnectionState.DISCONNECTED;
		
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
}
