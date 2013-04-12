package me.shanked.nicatronTg.minercon;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;

import com.google.gson.Gson;

public class StorageMaintainer {

	private String fileName;
	private final Activity activity;

	public StorageMaintainer(Activity activity, String fileName) {
		this.fileName = fileName;
		this.activity = activity;
	}

	public boolean storeNewServer(Server s) throws IOException,
			NullPointerException {
		ServerStore servers = null;

		try {
			servers = readServers();
		} catch (FileNotFoundException e) {
			servers = new ServerStore();
		}

		for (Server srv : servers.getServers()) {
			if (srv.getName().equalsIgnoreCase(s.getName())) {
				return false;
			}
		}
		
		servers.getServers().add(s);
		writeServers(servers);
		return true;
	}

	private void writeServers(ServerStore s) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		String jsonServerStore = new Gson().toJson(s);
		System.out.println("Writing JSON: " + jsonServerStore);
		try {
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			osw.write(jsonServerStore);
			osw.flush();
			osw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			fos.close();
		}
	}

	public ServerStore readServers() throws FileNotFoundException {
		FileInputStream fis = null;
		ServerStore servers = null;
		fis = activity.openFileInput(fileName);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String contents = "";
		String readIn;
		try {
			readIn = br.readLine();
			while (readIn != null) {
				contents = contents + readIn;
				readIn = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Reading JSON: " + contents);
		servers = new Gson().fromJson(contents, ServerStore.class);
		return servers;
	}
	
	public boolean clearStorage() {
		return activity.deleteFile(fileName);
	}

}
