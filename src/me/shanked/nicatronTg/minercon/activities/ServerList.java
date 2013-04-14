package me.shanked.nicatronTg.minercon.activities;

import java.io.FileNotFoundException;

import me.shanked.nicatronTg.minercon.R;
import me.shanked.nicatronTg.minercon.Server;
import me.shanked.nicatronTg.minercon.ServerStore;
import me.shanked.nicatronTg.minercon.StorageMaintainer;
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

public class ServerList extends Activity {

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		StorageMaintainer sm = new StorageMaintainer(this, "servers.json");
		ServerStore serverStore = null;
		
		try {
			serverStore = sm.readServers();
		} catch (FileNotFoundException e) {
			String[] testItems = { "No servers defined.",
					"Press the add button (top right) to add one." };

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
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				String choice = (String) llv.getItemAtPosition(position);
				Intent i = new Intent(activity, ManageServer.class);
				i.putExtra("serverName", choice);
				startActivity(i);
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onAddButtonClick(MenuItem m) {
		Intent i = new Intent(this, AddServerActivity.class);
		startActivity(i);
	}

	public void onDeleteStorageClick(MenuItem m) {
		StorageMaintainer sm = new StorageMaintainer(this, "servers.json");
		sm.clearStorage();
		Intent i = new Intent(this, ServerList.class);
		startActivity(i);
	}

}
