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

import jade.core.UnreachableException;
import jade.util.Logger;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This activity implement the chat interface.
 * 
 * @author Michele Izzo - Telecomitalia
 */

public class ChatActivity extends Activity {
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	static final int PARTECIPANTS_REQUEST = 0;

	private MyReceiver myReceiver;
	private String[] partecipants;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myReceiver = new MyReceiver();
		partecipants = new String[] {};

		IntentFilter refreshChatFilter = new IntentFilter();
		refreshChatFilter.addAction("jade.demo.chat.REFRESH_CHAT");
		registerReceiver(myReceiver, refreshChatFilter);

		IntentFilter clearChatFilter = new IntentFilter();
		clearChatFilter.addAction("jade.demo.chat.CLEAR_CHAT");
		registerReceiver(myReceiver, clearChatFilter);

		IntentFilter refreshPartecipantsFilter = new IntentFilter();
		refreshPartecipantsFilter
				.addAction("jade.demo.chat.REFRESH_PARTECIPANTS");
		registerReceiver(myReceiver, refreshPartecipantsFilter);

		setContentView(R.layout.chat);

		Button button = (Button) findViewById(R.id.button_send);
		button.setOnClickListener(buttonSendListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.info("Destroy activity!");
	}

	private OnClickListener buttonSendListener = new OnClickListener() {
		public void onClick(View v) {
			final EditText messageField = (EditText) findViewById(R.id.edit_message);
			String message = messageField.getText().toString();
			if (message != null && !message.equals("")) {
				try {
					ChatGateway.getInstance().sendMessage(message);
					messageField.setText("");
				} catch (UnreachableException e) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ChatActivity.this);
					builder.setMessage(e.getMessage())
							.setCancelable(false)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_partecipants:
			Intent showPartecipants = new Intent(ChatActivity.this,
					PartecipantsActivity.class);
			showPartecipants.putExtra("partecipants", partecipants);
			startActivityForResult(showPartecipants, PARTECIPANTS_REQUEST);
			return true;
		case R.id.menu_clear:
			Intent broadcast = new Intent();
			broadcast.setAction("jade.demo.chat.CLEAR_CHAT");
			logger.info("Sending broadcast " + broadcast.getAction());
			sendBroadcast(broadcast);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PARTECIPANTS_REQUEST) {
			if (resultCode == RESULT_OK) {
				// TODO: A partecipant was picked. Send a private message.
			}
		}
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.info("Received intent " + action);
			if (action.equalsIgnoreCase("jade.demo.chat.REFRESH_CHAT")) {
				final TextView chatField = (TextView) findViewById(R.id.chatTextView);
				chatField.append(intent.getExtras().getString("sentence"));
				scrollDown();
			}
			if (action.equalsIgnoreCase("jade.demo.chat.CLEAR_CHAT")) {
				final TextView chatField = (TextView) findViewById(R.id.chatTextView);
				chatField.setText("");
			}
			if (action.equalsIgnoreCase("jade.demo.chat.REFRESH_PARTECIPANTS")) {
				partecipants = intent.getExtras()
						.getStringArray("partecipants");
			}
		}
	}

	private void scrollDown() {
		final ScrollView scroller = (ScrollView) findViewById(R.id.scroller);
		final TextView chatField = (TextView) findViewById(R.id.chatTextView);
		scroller.smoothScrollTo(0, chatField.getBottom());
	}
}