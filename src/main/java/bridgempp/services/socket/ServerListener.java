package bridgempp.services.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;

class ServerListener implements Runnable {
	/**
	 * 
	 */
	private final SocketService socketService;

	/**
	 * @param socketService
	 */
	ServerListener(SocketService socketService) {
		this.socketService = socketService;
	}

	private long lastKeepAlive = 0;

	@Override
	public void run() {
		ShadowManager.log(Level.INFO, "Starting TCP Server Socket Listener");

		try {
			socketService.serverSocket = new ServerSocket(socketService.listenPort, 10,
					InetAddress.getByName(socketService.listenAddress));
			socketService.serverSocket.setSoTimeout(5000);
			while (!socketService.pendingShutdown && !socketService.serverSocket.isClosed()) {
				try {
					Socket socket = socketService.serverSocket.accept();
					String identifier = socket.getInetAddress().toString() + ":" + socket.getPort();

					Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(identifier, socketService);
					User user = DataManager.getOrNewUserForIdentifier(identifier, endpoint);
					SocketClient socketClient = new SocketClient(socketService, socket, user, endpoint);
					socketClient.randomIdentifier = identifier;
					new Thread(socketClient, "Socket TCP Connection").start();
				} catch (SocketTimeoutException e) {
				}
				if (System.currentTimeMillis() > lastKeepAlive + 60000) {
					sendKeepAliveMessages();
				}
			}
		} catch (IOException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	private void sendKeepAliveMessages() {
		socketService.sendKeepAliveMessages();
		lastKeepAlive = System.currentTimeMillis();
	}

}