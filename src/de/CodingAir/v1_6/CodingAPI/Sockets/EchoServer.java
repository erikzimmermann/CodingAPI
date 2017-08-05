package de.CodingAir.v1_6.CodingAPI.Sockets;


import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public abstract class EchoServer {
	private ServerSocket serverSocket;
	private List<SocketMessenger> messengers = new ArrayList<>();
	
	private int port;
	
	public EchoServer(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean start(Plugin plugin) {
		if(!available(port)) return false;
		
		try {
			serverSocket = new ServerSocket(port);
			
			BungeeCord.getInstance().getScheduler().runAsync(plugin, new Runnable() {
				@Override
				public void run() {
					while(true) {
						try {
							Socket clientSocket = serverSocket.accept();
							
							messengers.add(onConnect(clientSocket));
						} catch(IOException e) {
							
						}
					}
				}
			});
			
			BungeeCord.getInstance().getScheduler().schedule(plugin, new Runnable() {
				int times = 0;
				
				@Override
				public void run() {
					if(times == 100) {
						times = 0;
						//messengers.forEach(messenger -> messenger.send("KEEP_ALIVE"));
					} else times++;
					
					messengers.forEach(messenger -> messenger.check());
				}
			}, 50, 50, TimeUnit.MILLISECONDS);
			
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void sendToAll(String message, Socket... exceptions) {
		List<Socket> sockets = new ArrayList<>();
		
		for(Socket socket : exceptions) {
			sockets.add(socket);
		}
		
		this.messengers.forEach(messenger -> {
			if(!sockets.contains(messenger.getSocket()) && messenger.isAlive()) messenger.send(message);
		});
	}
	
	public abstract SocketMessenger onConnect(Socket socket);
	
	public List<SocketMessenger> getMessengers() {
		return messengers;
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	
	public boolean available(int port) {
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}
		
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress("localhost", port), 5);
			socket.close();
			return false;
		} catch (Exception ex) {
			return true;
		}
	}
}
