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

import jade.util.Logger;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This activity implement the partecipants interface.
 * 
 * @author Michele Izzo - Telecom Italia
 */

public class PartecipantsActivity extends ListActivity {
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	private MyReceiver myReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String[] partecipants = new String[] { };
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			partecipants = extras.getStringArray("partecipants");
		}

		myReceiver = new MyReceiver();

		IntentFilter refreshPartecipantsFilter = new IntentFilter();
		refreshPartecipantsFilter
				.addAction("jade.demo.chat.REFRESH_PARTECIPANTS");
		registerReceiver(myReceiver, refreshPartecipantsFilter);

		setContentView(R.layout.partecipants);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.partecipant,
				partecipants));

		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(listViewtListener);
	}

	private OnItemClickListener listViewtListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO: A partecipant was picked. Send a private message.
			finish();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.info("Destroy activity!");
	}

	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.info("Received intent " + action);
			if (action.equalsIgnoreCase("jade.demo.chat.REFRESH_PARTECIPANTS")) {
				String[] names = intent.getStringArrayExtra("partecipants");
				setListAdapter(new ArrayAdapter<String>(
						PartecipantsActivity.this, R.layout.partecipant, names));
			}
		}
	}

}
