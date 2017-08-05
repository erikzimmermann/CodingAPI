package de.CodingAir.v1_6.CodingAPI.Sockets;

import net.md_5.bungee.api.connection.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class SocketMessenger {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private boolean alive = true;
	private Server server;
	
	public SocketMessenger(Socket socket, BufferedReader in, PrintWriter out) {
		this.socket = socket;
		this.in = in;
		this.out = out;
	}
	
	public SocketMessenger() {
	
	}
	
	public void check() {
		if(in == null) {
			onFail();
			return;
		}
		
		try {
			String input;
			if(in.ready() && (input = in.readLine()) != null) {
				onReceive(input);
			}
		} catch(IOException e) {
			onFail();
		}
	}
	
	public abstract void onReceive(String message);
	public abstract void onSend(String message);
	public void onFail() {
		try {
			alive = false;
			if(this.socket == null || this.socket.isClosed()) return;
			
			this.socket.setKeepAlive(false);
			this.socket.close();
		} catch(IOException e) {
		}
	}
	
	public void send(String message) {
		if(out == null) {
			onFail();
			return;
		}
		
		onSend(message);
		out.println(message);
	}
	
	public BufferedReader getIn() {
		return in;
	}
	
	public void setIn(BufferedReader in) {
		this.in = in;
	}
	
	public PrintWriter getOut() {
		return out;
	}
	
	public void setOut(PrintWriter out) {
		this.out = out;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public Server getServer() {
		return server;
	}
	
	public void setServer(Server server) {
		this.server = server;
	}
}
