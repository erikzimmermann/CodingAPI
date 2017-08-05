package de.CodingAir.v1_6.CodingAPI.Sockets;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class EchoClient {
	private Socket echoSocket;
	
	private String host;
	private int port;
	private SocketMessenger messenger;
	private boolean failed = false;
	
	public EchoClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void connect(Plugin plugin, SocketMessenger messenger) {
		try {
			echoSocket = new Socket(host, port);
			
			PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			
			messenger.setOut(out);
			messenger.setIn(in);
			messenger.setSocket(echoSocket);
			
			this.messenger = messenger;
			
			Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> messenger.check(), 1, 1);
		} catch(ConnectException e) {
			onFail(e);
			setFailed(true);
		} catch(UnknownHostException e) {
			onFail(e);
			setFailed(true);
		} catch(IOException e) {
			onFail(e);
			setFailed(true);
		}
	}
	
	public abstract void onFail(Exception ex);
	
	public SocketMessenger getMessenger() {
		return messenger;
	}
	
	public Socket getEchoSocket() {
		return echoSocket;
	}
	
	public boolean isFailed() {
		return failed;
	}
	
	public void setFailed(boolean failed) {
		this.failed = failed;
	}
}
