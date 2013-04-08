package me.shanked.nicatronTg.minercon;

import java.io.IOException;

import com.google.rconclient.rcon.AuthenticationException;
import com.google.rconclient.rcon.RCon;

public class RconTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RCon rcon = new RCon("localhost", 25575, "testing123".toCharArray());
			
			String[] players = rcon.list();
			
			for (String s : players) {
				System.out.println("Player: " + s);
			}
			
			rcon.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
