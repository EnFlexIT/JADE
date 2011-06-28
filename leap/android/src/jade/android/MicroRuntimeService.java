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
import jade.util.Logger;
import jade.util.leap.Properties;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
public class MicroRuntimeService extends Service {
	protected static final Logger logger = Logger
			.getMyLogger(RuntimeService.class.getName());

	private final IBinder binder = new MicroRuntimeServiceBinder(this);

	private String agentName;

	@Override
	public void onCreate() {
		logger.log(Logger.INFO, "JADE micro runtime service created");
	}

	@Override
	public void onDestroy() {
		logger.log(Logger.INFO, "JADE micro runtime service destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		logger.log(Logger.SEVERE,
				"JADE micro runtime service can only be used locally");

		throw new UnsupportedOperationException();
	}

	@Override
	public IBinder onBind(Intent intent) {
		logger.log(Logger.INFO, "JADE micro runtime service bound");

		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		logger.log(Logger.INFO, "JADE micro runtime service unbound");

		return false;
	}

	public void startAgentContainer(String host, int port,
			RuntimeCallback<Void> callback) {
		Properties properties = RuntimeHelper.createProfileProperties(host,
				port);

		startAgentContainer(properties, callback);
	}

	public void startAgentContainer(Properties properties,
			RuntimeCallback<Void> callback) {
		final Properties finalProperties = properties;

		final RuntimeCallback<Void> finalCallback = callback;

		RuntimeHelper.completeProfileProperties(properties);

		new Thread() {
			@Override
			public void run() {
				try {
					logger.log(Logger.INFO, "Creating micro agent container");

					MicroRuntime.startJADE(finalProperties, null);

					finalCallback.notifySuccess(logger, null);

					logger.log(Logger.INFO, "Agent container created");
				} catch (Throwable t) {
					logger.log(Logger.INFO,
							"Cannot create micro agent container with message: "
									+ t.getMessage());

					finalCallback.notifyFailure(logger, t);
				}
			}
		}.start();
	}

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
