package me.shanked.nicatronTg.minercon.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import me.shanked.nicatronTg.minercon.R;
import me.shanked.nicatronTg.minercon.Server;
import me.shanked.nicatronTg.minercon.StorageMaintainer;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
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

	private static Server server;

	static ArrayList<AsyncTask<?, ?, ?>> runningAsyncTasks = null;
	static RCon rcon = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		runningAsyncTasks = new ArrayList<AsyncTask<?, ?, ?>>();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_manage_server);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setListNavigationCallbacks(new ArrayAdapter<String>(actionBar.getThemedContext(), android.R.layout.simple_list_item_1, android.R.id.text1, new String[] {
				getString(R.string.manage_server_players),
				getString(R.string.manage_server_console), }), this);

		String serverName = getIntent().getExtras().getString("serverName");

		StorageMaintainer sm = new StorageMaintainer(this, "servers.json");
		try {
			for (Server s : sm.readServers().getServers()) {
				if (s.getName().equals(serverName)) {
					server = s;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
			getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
		}

		if (position == 1) {
			Fragment fragment = new ServerConsoleFragment();
			getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
		}

		return true;
	}

	public static void destroyAllRunningTasks() {
		for (AsyncTask<?, ?, ?> asyncTask : runningAsyncTasks) {
			asyncTask.cancel(true);
		}
	}

	public static class ServerConsoleFragment extends Fragment {
		public ServerConsoleFragment() {

		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// TODO Auto-generated method stub
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setHasOptionsMenu(true);
			View root = inflater.inflate(R.layout.fragment_server_console, container, false);

			EditText editText = (EditText) root.findViewById(R.id.console_command);
			editText.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_SEND) {
						String consoleCommand = v.getText().toString().trim();
						if (consoleCommand.length() != 0) {
							new ConsoleCommand().execute(consoleCommand);
							TextView tv = (TextView) getActivity().findViewById(R.id.server_console);
							String text = tv.getText().toString();
							tv.setText(text + "\n" + "> " + v.getText().toString().trim());
							
						}
					}
					return true;
				}
			});

			return root;
		}

		@Override
		public void onDestroy() {
			try {
				rcon.close();
				destroyAllRunningTasks();
			} catch (Exception e) {
                e.printStackTrace();
			}
			super.onDestroy();
		}

		@Override
		public void onDetach() {
			try {
				rcon.close();
				destroyAllRunningTasks();
			} catch (Exception e) {
                e.printStackTrace();
			}
			super.onDestroy();
		}
		
		class ConsoleCommand extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {
				String consoleCommandResponse = "";
				try {
					if (rcon == null || rcon.isShutdown()) {
						rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					}
					consoleCommandResponse = rcon.send(params[0]);
				} catch (Exception e) {
                    e.printStackTrace();
				}
				return consoleCommandResponse;
			}

			@Override
			protected void onPostExecute(String result) {
				TextView tv = (TextView) getActivity().findViewById(R.id.server_console);
				String text = tv.getText().toString();
				tv.setText(text + "\n" + result);
				final ScrollView sv = (ScrollView) getActivity().findViewById(R.id.scrollView1);
				sv.post(new Runnable() {
					
					@Override
					public void run() {
						sv.fullScroll(View.FOCUS_DOWN);
					}
				});
                EditText et = (EditText) getActivity().findViewById(R.id.console_command);
				et.setText("");
				super.onPostExecute(result);
			}

		}

	}

	public static class CurrentPlayersFragment extends Fragment {

		public CurrentPlayersFragment() {

		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			inflater.inflate(R.menu.current_players, menu);
			super.onCreateOptionsMenu(menu, inflater);
		}

		@Override
		public void onPrepareOptionsMenu(Menu menu) {
			super.onPrepareOptionsMenu(menu);
			MenuItem item = menu.findItem(R.id.refresh_player_list);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem m) {
					destroyAllRunningTasks();
					runningAsyncTasks.add(new GetPlayerList().execute(server));
					getActivity().setProgressBarIndeterminateVisibility(true);
					return true;
				}
			});
		}

		@Override
		public void onDestroy() {
			try {
				rcon.close();
				destroyAllRunningTasks();
			} catch (Exception e) {
                e.printStackTrace();
			}
			super.onDestroy();
		}

		@Override
		public void onDetach() {
			try {
				rcon.close();
				destroyAllRunningTasks();
			} catch (Exception e) {
                e.printStackTrace();
			}
			super.onDestroy();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			setHasOptionsMenu(true);
			View root = inflater.inflate(R.layout.fragment_current_players, container, false);
			registerForContextMenu(root.findViewById(R.id.player_list_fragment));
			ListView lv = (ListView) root.findViewById(R.id.player_list_fragment);
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapterView, View v,
						int position, long id) {
					v.showContextMenu();
				}
			});

			return root;
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			final String playerName = ((TextView) info.targetView).getText().toString();
			LayoutInflater factory;
            LayoutInflater.from(this.getActivity());
            switch (item.getItemId()) {
			case R.id.op:
				new GivePlayerOperator().execute(playerName);
				Toast.makeText(getActivity(), playerName + " is now an operator.", Toast.LENGTH_SHORT).show();
				break;
			case R.id.deop:
				new RevokePlayerOperator().execute(playerName);
				Toast.makeText(getActivity(), playerName + " is no longer an operator.", Toast.LENGTH_SHORT).show();
				break;
			case R.id.kick:
				factory = LayoutInflater.from(this.getActivity());
				final View kickReasonTextEntry = factory.inflate(R.layout.dialog_kick_reason, null);
				AlertDialog.Builder kickAlert = new AlertDialog.Builder(this.getActivity());
				kickAlert.setTitle("Kick Details");
				kickAlert.setMessage("Enter Kick Reason:");
				kickAlert.setView(kickReasonTextEntry);
				kickAlert.setPositiveButton("Kick Player", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText eKickReason = (EditText) kickReasonTextEntry.findViewById(R.id.kick_reason);
						String kickReason = "Kicked by an operator.";

						if (eKickReason.getText().toString().trim().length() != 0) {
							kickReason = eKickReason.getText().toString();
						}

						new KickPlayer().execute(playerName, kickReason);
						Toast.makeText(getActivity(), playerName + " was kicked.", Toast.LENGTH_LONG).show();
					}
				});
				kickAlert.setNegativeButton("Cancel", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getActivity(), "Kick aborted!", Toast.LENGTH_LONG).show();
					}
				});
				kickAlert.show();

				break;
			case R.id.ban:
				factory = LayoutInflater.from(this.getActivity());
				final View banReasonTextEntry = factory.inflate(R.layout.dialog_ban_reason, null);
				AlertDialog.Builder banAlert = new AlertDialog.Builder(this.getActivity());
				banAlert.setTitle("Ban Details");
				banAlert.setMessage("Enter Ban Reason:");
				banAlert.setView(banReasonTextEntry);
				banAlert.setPositiveButton("Ban", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText eBanReason = (EditText) banReasonTextEntry.findViewById(R.id.ban_reason);
						String banReason = "Banned by operator.";
						if (eBanReason.getText().toString().trim().length() != 0) {
							banReason = eBanReason.getText().toString().trim();
						}
						new BanPlayer().execute(playerName, banReason);
						Toast.makeText(getActivity(), playerName + " was banned.", Toast.LENGTH_LONG).show();
					}
				});

				banAlert.setNegativeButton("Cancel", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getActivity(), "Ban aborted!", Toast.LENGTH_SHORT).show();
					}
				});

				banAlert.show();

				break;
			case R.id.clear_inventory:
				new ClearInventory().execute(playerName);
				Toast.makeText(getActivity(), playerName + " had their inventory cleared.", Toast.LENGTH_SHORT).show();
				break;
			case R.id.give_item:
				Toast.makeText(getActivity(), "Not implemented yet!!1!", Toast.LENGTH_SHORT).show();
				break;
			}

			return super.onContextItemSelected(item);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater menuInflater = getActivity().getMenuInflater();
			menuInflater.inflate(R.menu.player_actions, menu);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			runningAsyncTasks.add(new GetPlayerList().execute(server));
			getActivity().setProgressBarIndeterminateVisibility(true);

			super.onActivityCreated(savedInstanceState);
		}

		class ClearInventory extends AsyncTask<String, Void, Void> {

			@Override
			protected Void doInBackground(String... params) {
				try {
					if (rcon == null || rcon.isShutdown()) {
						rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					}
					rcon.clearInventory(params[0]);
				} catch (Exception e) {
                    e.printStackTrace();
				}
				return null;
			}

		}

		class BanPlayer extends AsyncTask<String, Void, Void> {

			@Override
			protected Void doInBackground(String... params) {
				try {
					if (rcon == null || rcon.isShutdown()) {
						rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					}
					rcon.banWithReason(params[0], params[1]);
				} catch (Exception e) {
                    e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				new GetPlayerList().execute(server);
			}

		}

		class KickPlayer extends AsyncTask<String, Void, Void> {

			@Override
			protected Void doInBackground(String... params) {
				try {
					if (rcon == null || rcon.isShutdown()) {
						rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					}
					rcon.kickWithReason(params[0], params[1]);
				} catch (Exception e) {
                    e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				new GetPlayerList().execute(server);
			}

		}

		class RevokePlayerOperator extends AsyncTask<String, Void, Void> {

			@Override
			protected Void doInBackground(String... params) {
				try {
					if (rcon == null || rcon.isShutdown()) {
						rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					}
					rcon.deOp(params[0]);
				} catch (Exception e) {
                    e.printStackTrace();
				}
				return null;
			}

		}

		class GivePlayerOperator extends AsyncTask<String, Void, Void> {

			@Override
			protected Void doInBackground(String... arg0) {
				try {
					if (rcon == null || rcon.isShutdown()) {
						rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					}
					rcon.op(arg0[0]);
				} catch (Exception e) {
                    e.printStackTrace();
				}
				return null;
			}

		}

		class GetPlayerList extends AsyncTask<Server, Void, String[]> {

			@Override
			protected String[] doInBackground(Server... params) {
				String players[];
				Server server = params[0];
				try {
					if (rcon == null || rcon.isShutdown()) {
						rcon = new RCon(server.getHost(), server.getPort(), server.getPassword().toCharArray());
					}
					players = rcon.list();
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
					Toast.makeText(CurrentPlayersFragment.this.getActivity(), R.string.error_unable_to_authenticate, Toast.LENGTH_LONG).show();
					Intent i = new Intent(CurrentPlayersFragment.this.getActivity(), ServerList.class);
					startActivity(i);
					return;
				}

				if (result.length == 0) {
					ListView lv = (ListView) CurrentPlayersFragment.this.getActivity().findViewById(R.id.player_list_fragment);
					lv.setVisibility(View.INVISIBLE);

					TextView tv = (TextView) CurrentPlayersFragment.this.getActivity().findViewById(R.id.no_players_online);
					tv.setVisibility(View.VISIBLE);
					CurrentPlayersFragment.this.getActivity().setProgressBarIndeterminateVisibility(false);
					return;
				}

				for (int i = 0; i < result.length; i++) {
					result[i] = result[i].replaceAll("[^\\x20-\\x7e]f", "");
				}

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, result);
				ListView lv = (ListView) CurrentPlayersFragment.this.getActivity().findViewById(R.id.player_list_fragment);
				lv.setAdapter(adapter);
				lv.setVisibility(View.VISIBLE);
				TextView tv = (TextView) CurrentPlayersFragment.this.getActivity().findViewById(R.id.no_players_online);
				tv.setVisibility(View.INVISIBLE);
				CurrentPlayersFragment.this.getActivity().setProgressBarIndeterminateVisibility(false);
			}
		}
	}

}
