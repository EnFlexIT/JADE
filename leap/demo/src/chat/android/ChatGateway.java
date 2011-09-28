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

package chat.android;

//#MIDP_EXCLUDE_FILE
//#J2SE_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE

import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.core.UnreachableException;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import chat.client.ChatClientAgent;

/**
 * This gateway implements the communication between the Android Application and
 * the Jade platform.
 * 
 * @author Michele Izzo - Telecomitalia
 */

public class ChatGateway {
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	private static ChatGateway theGateway;

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;

	private ServiceConnection serviceConnection;

	private Context context;

	private Properties profile;
	private Properties originalProfile;

	private String nickname;

	public static final ChatGateway getInstance() {
		if (theGateway == null) {
			theGateway = new ChatGateway();
		}
		return theGateway;
	}

	public void init(Context context, Properties profile) {
		this.context = context;
		this.originalProfile = profile;
		initProfile();
	}

	private ChatGateway() {
	}

	public void startChatAgent(final String nickname,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		this.nickname = nickname;
		if (microRuntimeServiceBinder == null) {
			serviceConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName className,
						IBinder service) {
					microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
					logger.info("Gateway successfully bound to MicroRuntimeService");
					startContainer(nickname, agentStartupCallback);
				};

				public void onServiceDisconnected(ComponentName className) {
					microRuntimeServiceBinder = null;
					logger.info("Gateway unbound from MicroRuntimeService");
				}
			};
			logger.info("Binding Gateway to MicroRuntimeService...");
			context.bindService(new Intent(context, MicroRuntimeService.class),
					serviceConnection, Context.BIND_AUTO_CREATE);
		} else {
			logger.info("MicroRumtimeGateway already binded to service");
			startContainer(nickname, agentStartupCallback);
		}
	}

	public void sendMessage(final String message) throws UnreachableException {
		try {
			MicroRuntime.getAgent(nickname).putO2AObject(message,
					AgentController.ASYNC);
		} catch (Exception e) {
			e.printStackTrace();
			throw new UnreachableException("Error sending message!");
		}
	}

	private void startContainer(final String nickname,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.info("Successfully start of the container...");
							startAgent(nickname, agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.severe("Failed to start the container...");
						}
					});
		} else {
			startAgent(nickname, agentStartupCallback);
		}
	}

	private void startAgent(final String nickname,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(nickname,
				ChatClientAgent.class.getName(), new Object[] { context },
				new RuntimeCallback<Void>() {
					@Override
					public void onSuccess(Void thisIsNull) {
						logger.info("Successfully start of the "
								+ ChatClientAgent.class.getName() + "...");
						try {
							agentStartupCallback.onSuccess(MicroRuntime
									.getAgent(nickname));
						} catch (ControllerException e) {
							// Should never happen
							agentStartupCallback.onFailure(e);
						}
					}

					@Override
					public void onFailure(Throwable throwable) {
						logger.severe("Failed to start the "
								+ ChatClientAgent.class.getName() + "...");
						agentStartupCallback.onFailure(throwable);
					}
				});
	}

	private void initProfile() {
		// we need to clone the initialization properties to be sure that we
		// start from a clean situation, also when the JADE Runtime
		// is started more than one time.
		profile = (Properties) originalProfile.clone();
		profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		profile.setProperty(Profile.JVM, Profile.ANDROID);

		if (AndroidHelper.isEmulator()) {
			// Emulator: this is needed to work with emulated devices
			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
		} else {
			profile.setProperty(Profile.LOCAL_HOST,
					AndroidHelper.getLocalIPAddress());
		}

		// Emulator: this is not really needed on a real device
		profile.setProperty(Profile.LOCAL_PORT, "2000");
	}
}
