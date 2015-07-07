package bridgempp.services.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;

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
					int randomIdentifier;
					do {
						randomIdentifier = new Random().nextInt(Integer.MAX_VALUE);
					} while (socketService.connectedSockets.containsKey(randomIdentifier));

					Socket socket = socketService.serverSocket.accept();
					SocketClient socketClient = new SocketClient(socketService, socket, new Endpoint(
							socketService, randomIdentifier + ""));
					socketClient.randomIdentifier = randomIdentifier;
					socketService.connectedSockets.put(randomIdentifier, socketClient);
					new Thread(socketClient, "Socket TCP Connection").start();
				} catch (SocketTimeoutException e) {
				}
				if (System.currentTimeMillis() > lastKeepAlive + 60000) {
					sendKeepAliveMessages();
				}
				removePendingDeletions();
			}
		} catch (IOException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	private void sendKeepAliveMessages() {
		for (SocketClient client : socketService.connectedSockets.values()) {
			socketService.sendMessage(new Message(client.endpoint, client.endpoint, null, "",
					MessageFormat.PLAIN_TEXT));
		}
		lastKeepAlive = System.currentTimeMillis();
	}

	private void removePendingDeletions() {
		while (!socketService.pendingDeletion.isEmpty()) {
			Integer index = socketService.pendingDeletion.removeFirst();
			socketService.connectedSockets.remove(index);
		}
	}

}