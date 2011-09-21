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

import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.wrapper.AgentController;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This activity implement the main interface.
 * 
 * @author Michele Izzo - Telecom Italia
 */

public class MainActivity extends Activity {
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	static final int CHAT_REQUEST = 0;
	static final int SETTINGS_REQUEST = 1;

	private MyReceiver myReceiver;
	private MyHandler myHandler;

	private TextView infoTextView;

	private String nickname;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myReceiver = new MyReceiver();

		IntentFilter killFilter = new IntentFilter();
		killFilter.addAction("jade.demo.chat.KILL");
		registerReceiver(myReceiver, killFilter);

		IntentFilter showChatFilter = new IntentFilter();
		showChatFilter.addAction("jade.demo.chat.SHOW_CHAT");
		registerReceiver(myReceiver, showChatFilter);

		myHandler = new MyHandler();

		setContentView(R.layout.main);

		Button button = (Button) findViewById(R.id.button_chat);
		button.setOnClickListener(buttonChatListener);

		infoTextView = (TextView) findViewById(R.id.infoTextView);
		infoTextView.setText("");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.info("Destroy activity!");
	}

	private static boolean checkName(String name) {
		if (name == null || name.trim().equals("")) {
			return false;
		}
		// FIXME: should also check that name is composed
		// of letters and digits only
		return true;
	}

	private OnClickListener buttonChatListener = new OnClickListener() {
		public void onClick(View v) {
			final EditText nameField = (EditText) findViewById(R.id.edit_nickname);
			nickname = nameField.getText().toString();
			if (!checkName(nickname)) {
				logger.info("Invalid nickname!");
				myHandler.postError(getString(R.string.msg_nickname_not_valid));
			} else {
				try {
					infoTextView.setText(getString(R.string.msg_connecting_to)
							+ " "
							+ ChatApplication.getProperties().getProperty(
									Profile.MAIN_HOST)
							+ ":"
							+ ChatApplication.getProperties().getProperty(
									Profile.MAIN_PORT) + "...");
					ChatGateway gateway = ChatGateway.getInstance();
					gateway.init(getApplicationContext(),
							ChatApplication.getProperties());
					gateway.startChatAgent(nickname, agentStartupCallback);
				} catch (Exception ex) {
					// TODO: unexpected exception
					logger.severe("Unexpected exception creating chat agent!");
					infoTextView.setText(getString(R.string.msg_unexpected));
				}
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent showSettings = new Intent(MainActivity.this,
					SettingsActivity.class);
			MainActivity.this.startActivityForResult(showSettings,
					SETTINGS_REQUEST);
			return true;
		case R.id.menu_exit:
			// ChatGateway.getInstance().detach();
			finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHAT_REQUEST) {
			if (resultCode == RESULT_CANCELED) {
				// The chat activity was closed.
				infoTextView.setText("");
				// finish();
			}
		}
	}

	private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
		@Override
		public void onSuccess(AgentController agent) {
		}

		@Override
		public void onFailure(Throwable throwable) {
			logger.info("Nickname already in use!");
			myHandler.postError(getString(R.string.msg_nickname_in_use));
		}
	};

	public void ShowDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.info("Received intent " + action);
			if (action.equalsIgnoreCase("jade.demo.chat.KILL")) {
				// ChatGateway.getInstance().detach();
				finish();
			}
			if (action.equalsIgnoreCase("jade.demo.chat.SHOW_CHAT")) {
				Intent showChat = new Intent(MainActivity.this,
						ChatActivity.class);
				MainActivity.this
						.startActivityForResult(showChat, CHAT_REQUEST);
			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle.containsKey("error")) {
				infoTextView.setText("");
				String message = bundle.getString("error");
				ShowDialog(message);
			}
		}

		public void postError(String error) {
			Message msg = obtainMessage();
			Bundle b = new Bundle();
			b.putString("error", error);
			msg.setData(b);
			sendMessage(msg);
		}
	}

}
