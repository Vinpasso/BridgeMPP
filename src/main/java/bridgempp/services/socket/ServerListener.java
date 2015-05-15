package bridgempp.services.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import bridgempp.Endpoint;
import bridgempp.Message;
import bridgempp.ShadowManager;
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
			this.socketService.serverSocket = new ServerSocket(this.socketService.listenPort, 10, InetAddress.getByName(this.socketService.listenAddress));
			this.socketService.serverSocket.setSoTimeout(5000);
			while (!this.socketService.serverSocket.isClosed()) {
				try {
					int randomIdentifier;
					do {
						randomIdentifier = new Random().nextInt(Integer.MAX_VALUE);
					} while (this.socketService.connectedSockets.containsKey(randomIdentifier));

					Socket socket = this.socketService.serverSocket.accept();
					SocketClient socketClient = new SocketClient(this.socketService, socket, new Endpoint(this.socketService,
							randomIdentifier + ""));
					socketClient.randomIdentifier = randomIdentifier;
					this.socketService.connectedSockets.put(randomIdentifier, socketClient);
					new Thread(socketClient, "Socket TCP Connection").start();
				} catch (SocketTimeoutException e) {
				}
				if (System.currentTimeMillis() > lastKeepAlive + 60000) {
					sendKeepAliveMessages();
				}
				removePendingDeletions();
			}
		} catch (IOException ex) {
			Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void sendKeepAliveMessages() {
		for (SocketClient client : this.socketService.connectedSockets.values()) {
			this.socketService.sendMessage(new Message(client.endpoint, client.endpoint, null, "", MessageFormat.PLAIN_TEXT));
		}
		lastKeepAlive = System.currentTimeMillis();
	}

	private void removePendingDeletions() {
		while (!this.socketService.pendingDeletion.isEmpty()) {
			Integer index = this.socketService.pendingDeletion.removeFirst();
			this.socketService.connectedSockets.remove(index);
		}
	}

}