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
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
public class RuntimeService extends Service {
	protected static final Logger logger = Logger
			.getMyLogger(RuntimeService.class.getName());

	private final List<RuntimeServiceListener> listeners = new LinkedList<RuntimeServiceListener>();

	private final IBinder binder = new RuntimeServiceBinder(this);

	private final ListenerNotifier listenerNotifier = new ListenerNotifier();

	@Override
	public void onCreate() {
		logger.log(Logger.INFO, "JADE runtime service created");
	}

	@Override
	public void onDestroy() {
		synchronized (listeners) {
			listeners.clear();
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

	public void addListener(RuntimeServiceListener listener) {
		if (listener != null)
			synchronized (listeners) {
				listeners.add(listener);
			}
	}

	public void removeListener(RuntimeServiceListener listener) {
		if (listener != null)
			synchronized (listeners) {
				listeners.remove(listener);
			}
	}

	public void startAgent(AgentHandler agentHandler,
			RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Starting agent");

					final AgentController finalAgentController = finalAgentHandler
							.getAgentController();

					finalAgentController.start();

					finalCallback.notifySuccess(logger, null);

					notifyAgentStarted(finalAgentHandler);

					logger.log(Logger.INFO, "Agent started");
				} catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot start agent with message: "
							+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void killAgent(AgentHandler agentHandler,
			RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Killing agent");

					final AgentController finalAgentController = finalAgentHandler
							.getAgentController();

					finalAgentController.kill();

					finalCallback.notifySuccess(logger, null);

					notifyAgentKilled(finalAgentHandler);

					logger.log(Logger.INFO, "Agent killed");
				} catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot kill agent with message: "
							+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void suspendAgent(AgentHandler agentHandler,
			RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Suspending agent");

					final AgentController finalAgentController = finalAgentHandler
							.getAgentController();

					finalAgentController.suspend();

					finalCallback.notifySuccess(logger, null);

					notifyAgentSuspended(finalAgentHandler);

					logger.log(Logger.INFO, "Agent suspended");
				} catch (Throwable t) {
					logger.log(
							Logger.INFO,
							"Cannot suspend agent with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void activateAgent(AgentHandler agentHandler,
			RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final AgentHandler finalAgentHandler = agentHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Activating agent");

					final AgentController finalAgentController = finalAgentHandler
							.getAgentController();

					finalAgentController.activate();

					finalCallback.notifySuccess(logger, null);

					notifyAgentActivated(finalAgentHandler);

					logger.log(Logger.INFO, "Agent activated");
				} catch (Throwable t) {
					logger.log(
							Logger.INFO,
							"Cannot activate agent with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void createNewAgent(AgentContainerHandler agentContainerHandler,
			String nickname, String className, Object[] args,
			RuntimeCallback<AgentHandler> callback) {
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

					AgentContainer agentContainer = finalAgentContainerHandler
							.getAgentContainer();

					AgentController agentController = agentContainer
							.createNewAgent(finalNickname, finalClassName,
									finalArgs);

					AgentHandler agentHandler = new AgentHandler(
							finalAgentContainerHandler, agentController);

					finalCallback.notifySuccess(logger, agentHandler);

					notifyNewAgentCreated(agentHandler);

					logger.log(Logger.INFO, "New agent created");
				} catch (Throwable t) {
					logger.log(
							Logger.INFO,
							"Cannot create new agent with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void killAgentContainer(AgentContainerHandler agentContainerHandler,
			RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final AgentContainerHandler finalAgentContainerHandler = agentContainerHandler;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Killing agent container");

					AgentContainer agentContainer = finalAgentContainerHandler
							.getAgentContainer();

					agentContainer.kill();

					finalCallback.notifySuccess(logger, null);

					notifyAgentContainerDestroyed(finalAgentContainerHandler);

					logger.log(Logger.INFO, "Agent container killed");
				} catch (Throwable t) {
					logger.log(
							Logger.INFO,
							"Cannot kill agent container with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void createMainAgentContainer(
			RuntimeCallback<AgentContainerHandler> callback) {
		final Profile finalProfile = RuntimeHelper.createMainProfile();

		final RuntimeCallback<AgentContainerHandler> finalCallback = callback;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Creating main agent container");

					AgentContainer agentContainer = Runtime.instance()
							.createMainContainer(finalProfile);

					AgentContainerHandler agentContainerHandler = new AgentContainerHandler(
							RuntimeService.this, agentContainer);

					finalCallback.notifySuccess(logger, agentContainerHandler);

					notifyAgentContainerCreated(agentContainerHandler);

					logger.log(Logger.INFO, "Main agent container created");
				} catch (Throwable t) {
					logger.log(Logger.INFO,
							"Cannot create main agent container with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void createAgentContainer(String host, int port,
			RuntimeCallback<AgentContainerHandler> callback) {
		Properties properties = RuntimeHelper.createProfileProperties(host,
				port);

		final Profile finalProfile = new ProfileImpl(properties);

		final RuntimeCallback<AgentContainerHandler> finalCallback = callback;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Creating agent container");

					AgentContainer agentContainer = Runtime.instance()
							.createAgentContainer(finalProfile);

					AgentContainerHandler agentContainerHandler = new AgentContainerHandler(
							RuntimeService.this, agentContainer);

					finalCallback.notifySuccess(logger, agentContainerHandler);

					notifyAgentContainerCreated(agentContainerHandler);

					logger.log(Logger.INFO, "Agent container created");
				} catch (Throwable t) {
					logger.log(
							Logger.INFO,
							"Cannot create agent container with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	public void createAgentContainer(Profile profile,
			RuntimeCallback<AgentContainerHandler> callback) {
		final Profile finalProfile = profile;

		RuntimeHelper.completeProfile(profile);

		final RuntimeCallback<AgentContainerHandler> finalCallback = callback;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Creating agent container");

					AgentContainer agentContainer = Runtime.instance()
							.createAgentContainer(finalProfile);

					AgentContainerHandler agentContainerHandler = new AgentContainerHandler(
							RuntimeService.this, agentContainer);

					finalCallback.notifySuccess(logger, agentContainerHandler);

					notifyAgentContainerCreated(agentContainerHandler);

					logger.log(Logger.INFO, "Agent container created");
				} catch (Throwable t) {
					logger.log(
							Logger.INFO,
							"Cannot create agent container with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	private void notifyAgentContainerCreated(
			AgentContainerHandler agentContainerHandler) {
		final AgentContainerHandler finalContainerHandler = agentContainerHandler;

		listenerNotifier.notify(new Notifier() {
			public void notifyListener(RuntimeServiceListener listener) {
				listener.onAgentContainerCreated(finalContainerHandler);
			}
		});
	}

	private void notifyAgentContainerDestroyed(
			AgentContainerHandler agentContainerHandler) {
		final AgentContainerHandler finalContainerHandler = agentContainerHandler;

		listenerNotifier.notify(new Notifier() {
			public void notifyListener(RuntimeServiceListener listener) {
				listener.onAgentContainerDestroyed(finalContainerHandler);
			}
		});
	}

	private void notifyNewAgentCreated(AgentHandler agentHandler) {
		final AgentHandler finalAgentHandler = agentHandler;

		listenerNotifier.notify(new Notifier() {
			public void notifyListener(RuntimeServiceListener listener) {
				listener.onAgentCreated(finalAgentHandler);
			}
		});
	}

	private void notifyAgentStarted(AgentHandler agentHandler) {
		final AgentHandler finalAgentHandler = agentHandler;

		listenerNotifier.notify(new Notifier() {
			public void notifyListener(RuntimeServiceListener listener) {
				listener.onAgentStarted(finalAgentHandler);
			}
		});
	}

	private void notifyAgentKilled(AgentHandler agentHandler) {
		final AgentHandler finalAgentHandler = agentHandler;

		listenerNotifier.notify(new Notifier() {
			public void notifyListener(RuntimeServiceListener listener) {
				listener.onAgentKilled(finalAgentHandler);
			}
		});
	}

	private void notifyAgentSuspended(AgentHandler agentHandler) {
		final AgentHandler finalAgentHandler = agentHandler;

		listenerNotifier.notify(new Notifier() {
			public void notifyListener(RuntimeServiceListener listener) {
				listener.onAgentSuspended(finalAgentHandler);
			}
		});
	}

	private void notifyAgentActivated(AgentHandler agentHandler) {
		final AgentHandler finalAgentHandler = agentHandler;

		listenerNotifier.notify(new Notifier() {
			public void notifyListener(RuntimeServiceListener listener) {
				listener.onAgentActivated(finalAgentHandler);
			}
		});
	}

	private interface Notifier {
		public void notifyListener(RuntimeServiceListener listener);
	}

	private class ListenerNotifier {
		public void notify(Notifier notifier) {
			int n = listeners.size();

			RuntimeServiceListener[] array = new RuntimeServiceListener[n];

			synchronized (listeners) {
				array = listeners.toArray(array);
			}

			for (int i = 0; i < n; i++) {
				RuntimeServiceListener listener = null;

				try {
					listener = array[i];

					notifier.notifyListener(listener);
				} catch (Throwable t) {
					removeListener(listener);
				}
			}
		}
	}
}
