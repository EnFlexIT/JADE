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

import java.util.LinkedList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.TimerDispatcher;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
public class RuntimeService extends Service {
	protected static final Logger logger = Logger.getJADELogger(RuntimeService.class.getName());

	private AgentContainerHandler handler = null;
	
	private final List<RuntimeServiceListener> listeners = new LinkedList<RuntimeServiceListener>();
	private RuntimeServiceListener[] listenersArray = new RuntimeServiceListener[0];

	private final IBinder binder = new RuntimeServiceBinder(this);

	@Override
	public void onCreate() {
		logger.log(Logger.INFO, "JADE runtime service created");
		
		// Create an ad-hoc TimerDispatcher that does not reply on Object.wait(ms) since this
		// is based on a clock that is paused when the terminal enters deep sleep (CPU off, display dark...)
		TimerDispatcher td = new AndroidTimerDispatcher(this);
		TimerDispatcher.setTimerDispatcher(td);
	}

	@Override
	public void onDestroy() {
		synchronized (listeners) {
			listeners.clear();
			listenersArray = new RuntimeServiceListener[0];
		}

		logger.log(Logger.INFO, "JADE runtime service destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.log(Logger.SEVERE,
				"JADE runtime service can only be used locally");

		throw new UnsupportedOperationException();
	}

	@Override
	public IBinder onBind(Intent intent) {
		logger.log(Logger.INFO, "JADE runtime service bound");

		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		logger.log(Logger.INFO, "JADE runtime service unbound");

		return false;
	}

	public void startAgent(AgentHandler agentHandler, RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Starting agent");

					final AgentController finalAgentController = finalAgentHandler.getAgentController();
					finalAgentController.start();
					finalCallback.notifySuccess(logger, null);
					notifyAgentStarted(finalAgentHandler);

					logger.log(Logger.INFO, "Agent started");
				} 
				catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot start agent with message: " + t.getMessage());
					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void killAgent(AgentHandler agentHandler, RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Killing agent");

					final AgentController finalAgentController = finalAgentHandler.getAgentController();
					finalAgentController.kill();
					finalCallback.notifySuccess(logger, null);
					notifyAgentKilled(finalAgentHandler);

					logger.log(Logger.INFO, "Agent killed");
				} 
				catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot kill agent with message: " + t.getMessage());
					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void suspendAgent(AgentHandler agentHandler, RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;
		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Suspending agent");

					final AgentController finalAgentController = finalAgentHandler.getAgentController();
					finalAgentController.suspend();
					finalCallback.notifySuccess(logger, null);
					notifyAgentSuspended(finalAgentHandler);

					logger.log(Logger.INFO, "Agent suspended");
				} 
				catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot suspend agent with message: " + t.getMessage());
					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void activateAgent(AgentHandler agentHandler, RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;
		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Activating agent");

					final AgentController finalAgentController = finalAgentHandler.getAgentController();
					finalAgentController.activate();
					finalCallback.notifySuccess(logger, null);
					notifyAgentActivated(finalAgentHandler);

					logger.log(Logger.INFO, "Agent activated");
				} 
				catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot activate agent with message: " + t.getMessage());
					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void createNewAgent(AgentContainerHandler agentContainerHandler, String nickname, String className, Object[] args, RuntimeCallback<AgentHandler> callback) {
		final RuntimeCallback<AgentHandler> finalCallback = callback;
		final AgentContainerHandler finalAgentContainerHandler = agentContainerHandler;
		final String finalNickname = nickname;
		final String finalClassName = className;
		final Object[] finalArgs = args;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Creating new agent");

					AgentContainer agentContainer = finalAgentContainerHandler.getAgentContainer();
					AgentController agentController = agentContainer.createNewAgent(finalNickname, finalClassName, finalArgs);
					AgentHandler agentHandler = new AgentHandler(finalAgentContainerHandler, agentController);
					finalCallback.notifySuccess(logger, agentHandler);
					notifyNewAgentCreated(agentHandler);

					logger.log(Logger.INFO, "New agent created");
				} 
				catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot create new agent with message: " + t.getMessage());
					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void killAgentContainer(AgentContainerHandler agentContainerHandler, RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;
		final AgentContainerHandler finalAgentContainerHandler = agentContainerHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Killing agent container");

					AgentContainer agentContainer = finalAgentContainerHandler.getAgentContainer();
					agentContainer.kill();
					finalCallback.notifySuccess(logger, null);
					notifyAgentContainerDestroyed(finalAgentContainerHandler);

					logger.log(Logger.INFO, "Agent container killed");
				} 
				catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot kill agent container with message: " + t.getMessage());
					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void createMainAgentContainer(RuntimeCallback<AgentContainerHandler> callback) {
		createAgentContainer(RuntimeHelper.createMainProfile(), callback);
	}

	public void createAgentContainer(String host, int port, RuntimeCallback<AgentContainerHandler> callback) {
		createAgentContainer(RuntimeHelper.createContainerProfile(host, port), callback);
	}

	public void createAgentContainer(final Profile profile, final RuntimeCallback<AgentContainerHandler> callback) {
		// FIXME: We should properly handle synchronizations between two or more 
		// container creations requests occurring ion parallel
		RuntimeHelper.completeProfile(profile);
		new Thread() {
			@Override
			public void run() {
				try {
					if (handler != null) {
						throw new IllegalStateException("JADE Runtime already active");
					}
					logger.log(Logger.INFO, "Creating agent container");
					ContainerController cc = null;
					if (profile.isMain()) {
						// Main container
						cc = Runtime.instance().createMainContainer(profile);
					}
					else {
						// Peripheral container
						cc = Runtime.instance().createAgentContainer(profile);
					}
					if (cc != null) {
						// Container startup OK
						handler = new AgentContainerHandler(RuntimeService.this, cc);
						Runtime.instance().invokeOnTermination(new Runnable() {
							public void run() {
								handler = null;
								logger.log(Logger.INFO, "JADE runtime terminated");
							}
						});
	
						callback.notifySuccess(logger, handler);
						notifyAgentContainerCreated(handler);
	
						logger.log(Logger.INFO, "Agent container created");
					}
					else {
						// Container startup failure
						throw new Exception("JADE Startup failed");
					}
				} 
				catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot create agent container: " + t.getMessage());
					callback.notifyFailure(logger, t);
				}
			}
		}.start();
	}
	
	public AgentContainerHandler getContainerHandler() {
		return handler;
	}

	
	/////////////////////////////////////////////
	// Listener management section
	/////////////////////////////////////////////
	public void addListener(RuntimeServiceListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				listeners.add(listener);
				listenersArray = listeners.toArray(new RuntimeServiceListener[0]);
			}
		}
	}

	public void removeListener(RuntimeServiceListener listener) {
		if (listener != null) {
			synchronized (listeners) {
				if (listeners.remove(listener)) {
					listenersArray = listeners.toArray(new RuntimeServiceListener[0]);
				}
			}
		}
	}

	
	private void notifyAgentContainerCreated(AgentContainerHandler agentContainerHandler) {
		for (RuntimeServiceListener l : listenersArray) {
			l.onAgentContainerCreated(agentContainerHandler);
		}
	}

	private void notifyAgentContainerDestroyed(AgentContainerHandler agentContainerHandler) {
		for (RuntimeServiceListener l : listenersArray) {
			l.onAgentContainerDestroyed(agentContainerHandler);
		}
	}

	private void notifyNewAgentCreated(AgentHandler agentHandler) {
		for (RuntimeServiceListener l : listenersArray) {
			l.onAgentCreated(agentHandler);
		}
	}

	private void notifyAgentStarted(AgentHandler agentHandler) {
		for (RuntimeServiceListener l : listenersArray) {
			l.onAgentStarted(agentHandler);
		}
	}

	private void notifyAgentKilled(AgentHandler agentHandler) {
		for (RuntimeServiceListener l : listenersArray) {
			l.onAgentKilled(agentHandler);
		}
	}

	private void notifyAgentSuspended(AgentHandler agentHandler) {
		for (RuntimeServiceListener l : listenersArray) {
			l.onAgentSuspended(agentHandler);
		}
	}

	private void notifyAgentActivated(AgentHandler agentHandler) {
		for (RuntimeServiceListener l : listenersArray) {
			l.onAgentActivated(agentHandler);
		}
	}

}
