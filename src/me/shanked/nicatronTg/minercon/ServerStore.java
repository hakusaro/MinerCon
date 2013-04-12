package me.shanked.nicatronTg.minercon;

import java.util.ArrayList;

public class ServerStore {

	public ArrayList<Server> getServers() {
		return servers;
	}

	public void setServers(ArrayList<Server> servers) {
		this.servers = servers;
	}

	private ArrayList<Server> servers = new ArrayList<Server>();
	
	public ServerStore(ArrayList<Server> servers) {
		this.servers = servers;
	}
	
	public ServerStore() {
		
	}

}
