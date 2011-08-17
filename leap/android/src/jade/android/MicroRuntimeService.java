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

import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Implements the managing of a JADE platform within the <code>Service</code>
 * component of the Android system achitecture.
 * 
 * @author Federico Bergenti - Universita' di Parma
 */
public class MicroRuntimeService extends Service {
	protected static final Logger logger = Logger
			.getMyLogger(RuntimeService.class.getName());

	private final IBinder binder = new MicroRuntimeServiceBinder(this);

	private String agentName;

	/**
	 * Called by the system when the service is first created. Do not call this
	 * method directly.
	 */
	@Override
	public void onCreate() {
		logger.log(Logger.INFO, "JADE micro runtime service created");
	}

	/**
	 * Called by the system to notify a Service that it is no longer used and is
	 * being removed.
	 */
	@Override
	public void onDestroy() {
		logger.log(Logger.INFO, "JADE micro runtime service destroyed");
		if(MicroRuntime.isRunning())
			MicroRuntime.stopJADE();
	}

	/**
	 * Called by the system every time a client explicitly starts the service by
	 * calling <code>startService(Intent)</code>. <b>Unsupported.</b>
	 * 
	 * @param intent
	 *            The Intent supplied to <code>startService(Intent)</code>, as
	 *            given.
	 * @param flags
	 *            Additional data about this start request.
	 * @param startId
	 *            A unique integer representing this specific request to start.
	 * @return The return value indicates what semantics the system should use
	 *         for the service's current started state.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.log(Logger.SEVERE,
				"JADE micro runtime service can only be used locally");

		throw new UnsupportedOperationException();
		//return Service.START_STICKY;
		
	}

	/**
	 * Return the communication channel to the service.
	 * 
	 * @param intent
	 *            The Intent that was used to bind to this service, as given to
	 *            <code>Context.bindService</code>. <b>Unsupported.</b>
	 * @return Return an <code>IBinder</code> through which clients can call on
	 *         to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		logger.log(Logger.INFO, "JADE micro runtime service bound");

		return binder;
	}

	/**
	 * Called when all clients have disconnected from a particular interface
	 * published by the service. The default implementation does nothing and
	 * returns false.
	 * 
	 * @param intent
	 *            The Intent that was used to bind to this service, as given to
	 *            <code>Context.bindService</code>. <b>Unsupported.</b>
	 * @return Return always false
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		logger.log(Logger.INFO, "JADE micro runtime service unbound");

		return false;
	}

	/**
	 * Creates a new agent container in the current JVM, connected to a main
	 * platform.
	 * 
	 * @param host
	 *            hostname of the server that running the main container
	 * @param port
	 *            port on which the main container is listening
	 * @param callback
	 *            a <code>RuntimeCallback<Void></code> object that manages the
	 *            outcome of the operation
	 */
	public void startAgentContainer(String host, int port,
			RuntimeCallback<Void> callback) {
		Properties properties = RuntimeHelper.createProfileProperties(host,
				port);

		startAgentContainer(properties, callback);
	}

	/**
	 * Start up the JADE runtim creating a new micro agent container in the
	 * current JVM, according to the parameters provided
	 * 
	 * @param properties
	 *            A property bag, containing name-value pairs used to configure
	 *            the container during boot
	 * @param callback
	 *            a <code>RuntimeCallback<Void></code> object that manages the
	 *            outcome of the operation
	 */
	public void startAgentContainer(Properties properties, RuntimeCallback<Void> callback) {
		final Properties finalProperties = properties;

		final RuntimeCallback<Void> finalCallback = callback;

		RuntimeHelper.completeProfileProperties(properties);

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Creating micro agent container");

					MicroRuntime.startJADE(finalProperties, null);
					if (MicroRuntime.isRunning()) {
						logger.log(Logger.INFO, "Agent container created");
						finalCallback.notifySuccess(logger, null);
					} else {
						throw new Exception(
								"Cannot connect to the platform at "
										+ finalProperties
												.getProperty(Profile.MAIN_HOST)
										+ ":"
										+ finalProperties
												.getProperty(Profile.MAIN_PORT));
					}
				} catch (Throwable t) {
					logger.log(Logger.INFO,
							"Cannot create micro agent container with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	/**
	 * Shut down the JADE runtime. This method stops the JADE Front End
	 * container currently running in this JVM, if one such container exists.
	 * 
	 * @param callback
	 *            a <code>RuntimeCallback<Void></code> object that manages the
	 *            outcome of the operation
	 */
	public void stopAgentContainer(RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Stopping micro agent container");

					MicroRuntime.stopJADE();

					finalCallback.notifySuccess(logger, null);

					logger.log(Logger.INFO, "Agent container stopped");
				} catch (Throwable t) {
					logger.log(Logger.INFO,
							"Cannot stop micro agent container with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	/**
	 * Start a new agent. This method starts a new agent within the active Front
	 * End container.
	 * 
	 * @param nickname
	 *            The local name (i.e. without the platform ID) of the agent to
	 *            create
	 * @param className
	 *            The fully qualified name of the class implementing the agent
	 *            to start
	 * @param args
	 *            The creation arguments for the agent.
	 * @param callback
	 *            a <code>RuntimeCallback<Void></code> object that manages the
	 *            outcome of the operation
	 */
	public void startAgent(String nickname, String className, Object[] args,
			RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		final String finalNickname = nickname;

		final String finalClassName = className;

		final Object[] finalArgs = args;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Starting agent");

					agentName = finalNickname;

					MicroRuntime.startAgent(finalNickname, finalClassName,
							finalArgs);

					finalCallback.notifySuccess(logger, null);

					logger.log(Logger.INFO, "Agent started");
				} catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot start agent with message: "
							+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

	/**
	 * Kill an agent. This method terminates an agent running within the active
	 * Front End container.
	 * 
	 * @param callback
	 *            a <code>RuntimeCallback<Void></code> object that manages the
	 *            outcome of the operation
	 */
	public void killAgent(RuntimeCallback<Void> callback) {
		final RuntimeCallback<Void> finalCallback = callback;

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Killing agent");

					MicroRuntime.killAgent(agentName);

					agentName = null;

					finalCallback.notifySuccess(logger, null);

					logger.log(Logger.INFO, "Agent killed");
				} catch (Throwable t) {
					logger.log(Logger.INFO, "Cannot kill agent with message: "
							+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}
}
