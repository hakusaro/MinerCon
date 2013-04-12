package me.shanked.nicatronTg.minercon;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StorageMaintainer sm = new StorageMaintainer(this, "servers.json");
		ServerStore serverStore = null;
		try {
			serverStore = sm.readServers();
		} catch (FileNotFoundException e) {
			String[] testItems = { "No servers defined...",
					"Add a server to continue" };

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testItems);

			ListView lv = (ListView) findViewById(R.id.serverListView);

			lv.setAdapter(adapter);
			return;
		}

		String servers[] = new String[serverStore.getServers().size()];
		
		int iter = 0;
		
		for (Server s : serverStore.getServers()) {
			servers[iter] = s.getName();
			iter++;
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, servers);
		
		ListView lv = (ListView) findViewById(R.id.serverListView);
		lv.setAdapter(adapter);
		
		final ListView llv = lv;
		final Activity activity = this;
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				String choice = (String) llv.getItemAtPosition(position);
				Intent i = new Intent(activity, ServerManageActivity.class);
				i.putExtra("serverName", choice);
				startActivity(i);
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onAddButtonClick(MenuItem m) {
		Toast t = Toast.makeText(this, m.getTitle() + (" (click event)"), Toast.LENGTH_SHORT);
		t.show();

		Intent i = new Intent(this, AddServerActivity.class);
		startActivity(i);
	}

	public void onDeleteStorageClick(MenuItem m) {
		Toast t = Toast.makeText(this, "Opening a storage maintainer and deleting the server store. Hold on. Will reboot this activity immediately.", Toast.LENGTH_SHORT);
		t.show();

		StorageMaintainer sm = new StorageMaintainer(this, "servers.json");
		sm.clearStorage();
		Intent i = new Intent(this, MainActivity.class);
		startActivity(i);
	}
	
	

}
