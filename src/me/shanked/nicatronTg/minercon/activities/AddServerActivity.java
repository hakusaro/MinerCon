package me.shanked.nicatronTg.minercon.activities;

import java.io.IOException;

import me.shanked.nicatronTg.minercon.R;
import me.shanked.nicatronTg.minercon.Server;
import me.shanked.nicatronTg.minercon.StorageMaintainer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class AddServerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_server);
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_server, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onAddButtonClick(MenuItem m) {
		EditText _serverName = (EditText) findViewById(R.id.editTextServerName);
		EditText _serverHost = (EditText) findViewById(R.id.editTextServerHost);
		EditText _serverPort = (EditText) findViewById(R.id.editTextServerPort);
		EditText _serverPassword = (EditText) findViewById(R.id.editTextServerPassword);

		String serverName = _serverName.getText().toString();
		String serverHost = _serverHost.getText().toString();
		String s_serverPort = _serverPort.getText().toString();
		String serverPassword = _serverPassword.getText().toString();

		if (serverName.trim().length() == 0) {
			Toast.makeText(this, R.string.error_no_server_name, Toast.LENGTH_LONG).show();
			return;
		}

		if (serverHost.trim().length() == 0) {
			Toast.makeText(this, R.string.error_no_server_host, Toast.LENGTH_LONG).show();
			return;
		}

		if (s_serverPort.trim().length() == 0) {
			Toast.makeText(this, R.string.error_no_server_port, Toast.LENGTH_LONG).show();
			return;
		}

		if (serverPassword.trim().length() == 0) {
			Toast.makeText(this, R.string.error_no_server_password, Toast.LENGTH_LONG).show();
			return;
		}

		int serverPort;

		try {
			serverPort = Integer.parseInt(s_serverPort);
		} catch (NumberFormatException e) {
			Toast.makeText(this, R.string.error_invalid_server_port, Toast.LENGTH_LONG).show();
			return;
		}
		
		try {
			StorageMaintainer sm = new StorageMaintainer(this, "servers.json");
			if (sm.storeNewServer(new Server(serverName, serverHost, serverPort, serverPassword))) {
				Toast.makeText(this, R.string.server_added, Toast.LENGTH_SHORT).show();
				Intent i = new Intent(this, ServerList.class);
				startActivity(i);
			} else {
				Toast.makeText(this, R.string.error_duplicate_server_name, Toast.LENGTH_SHORT).show();
			}
		} catch (NullPointerException e) {
			Toast.makeText(this, R.string.error_npe_reinstall, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, R.string.error_io_reinstall, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		
	}
	
	public void onCancelButtonClick(MenuItem m) {
		Intent i = new Intent(this, ServerList.class);
		startActivity(i);
	}

}
