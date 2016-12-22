package bridgempp.services.whatsapp;

import java.util.Scanner;
import java.util.logging.Level;

import bridgempp.log.Log;

final class WhatsappErrorListener implements Runnable {
	/**
	 * 
	 */
	private final WhatsappMessageListener whatsappMessageListener;

	/**
	 * @param whatsappMessageListener
	 */
	WhatsappErrorListener(WhatsappMessageListener whatsappMessageListener) {
		this.whatsappMessageListener = whatsappMessageListener;
	}

	@Override
	public void run() {
		Log.log(Level.INFO, "Starting Yowsup Error Listener...");

		Scanner error = new Scanner(this.whatsappMessageListener.whatsappService.yowsup.getErrorStream());
		while (error.hasNext()) {
			System.err.println(error.nextLine());
		}
		error.close();
		Log.log(Level.INFO, "Stopping Yowsup Error Listener");
	}
}