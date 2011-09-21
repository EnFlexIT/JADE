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

import jade.core.Profile;
import jade.util.leap.Properties;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * This activity implement the settings interface.
 * 
 * @author Michele Izzo - Telecom Italia
 */

public class SettingsActivity extends Activity {
	Properties properties;
	EditText hostField;
	EditText portField;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		properties = ChatApplication.getProperties();

		setContentView(R.layout.settings);

		hostField = (EditText) findViewById(R.id.edit_host);
		hostField.setText(properties.getProperty(Profile.MAIN_HOST));

		portField = (EditText) findViewById(R.id.edit_port);
		portField.setText(properties.getProperty(Profile.MAIN_PORT));

		Button button = (Button) findViewById(R.id.button_use);
		button.setOnClickListener(buttonUseListener);
	}

	private OnClickListener buttonUseListener = new OnClickListener() {
		public void onClick(View v) {
			properties.setProperty(Profile.MAIN_HOST, hostField.getText()
					.toString());
			properties.setProperty(Profile.MAIN_PORT, portField.getText()
					.toString());
			ChatGateway.getInstance().init(getApplicationContext(), properties);
			finish();
		}
	};
}
