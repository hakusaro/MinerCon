package me.shanked.nicatronTg.minercon;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.rconclient.rcon.AuthenticationException;
import com.google.rconclient.rcon.RCon;

public class ManageServer extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	private static String serverName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		setContentView(R.layout.activity_manage_server);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setListNavigationCallbacks(new ArrayAdapter<String>(actionBar.getThemedContext(), android.R.layout.simple_list_item_1, android.R.id.text1, new String[] {
				getString(R.string.manage_server_players),
				getString(R.string.manage_server_properties),
				getString(R.string.manage_server_console), }), this);

		serverName = (String) getIntent().getExtras().getString("serverName");
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.manage_server, menu);
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

	@Override
	public boolean onNavigationItemSelected(int position, long id) {

		if (position == 0) {
			Fragment fragment = new CurrentPlayersFragment();
			Bundle args = new Bundle();
			args.putString(CurrentPlayersFragment.serverName, serverName);
			fragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
		}

		return true;
	}

	public static class CurrentPlayersFragment extends Fragment {
		static String serverName = "";

		public CurrentPlayersFragment() {

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_current_players, container, false);
			serverName = getArguments().getString(serverName);
			return root;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			Activity activity = getActivity();
			StorageMaintainer sm = new StorageMaintainer(activity, "servers.json");
			Server server = null;
			try {
				for (Server s : sm.readServers().getServers()) {
					if (s.getName().equals(serverName)) {
						server = s;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			new GetPlayerList().execute(server);
			
			getActivity().setProgressBarIndeterminateVisibility(true);
			
			super.onActivityCreated(savedInstanceState);
		}
		
		class GetPlayerList extends AsyncTask<Server, Void, String[]> {

			@Override
			protected String[] doInBackground(Server... params) {
				String players[] = null;
				Server server = params[0];
				try {
					RCon rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					players = rcon.list();
					rcon.close();
				} catch (IOException e) {
					return null;
				} catch (AuthenticationException e) {
					return null;
				}
				return players;
			}

			@Override
			protected void onPostExecute(String[] result) {
				
				if (result == null) {
					Toast.makeText(CurrentPlayersFragment.this.getActivity(), "Unable to authenticate with server. Please verify that a connection is available and the server's connection information is correct.", Toast.LENGTH_LONG).show();
					Intent i = new Intent(CurrentPlayersFragment.this.getActivity(), MainActivity.class);
					startActivity(i);
					return;
				}
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, result);
				ListView lv = (ListView) CurrentPlayersFragment.this.getActivity().findViewById(R.id.player_list_fragment);
				lv.setAdapter(adapter);
				CurrentPlayersFragment.this.getActivity().setProgressBarIndeterminateVisibility(false);
			}
		}

	}

}
