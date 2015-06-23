package bridgempp.services.whatsapp;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import bridgempp.ShadowManager;

class WhatsappSender implements Runnable {

	/**
	 * 
	 */
	private final WhatsappService whatsappService;
	private PrintStream realOutputStream;
	private long lastMessage = 0;

	public WhatsappSender(WhatsappService whatsappService, OutputStream realOutputStream) {
		this.whatsappService = whatsappService;
		try {
			this.realOutputStream = new PrintStream(realOutputStream, true,
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {

			while (true) {
				String line = this.whatsappService.senderQueue.take();
				if (line == null) {
					return;
				}
				if (System.currentTimeMillis() - lastMessage < 10000) {
					Thread.sleep(10000);
				}
				realOutputStream.println(line);
				lastMessage = System.currentTimeMillis();
			}
		} catch (InterruptedException ex) {
			ShadowManager.log(Level.WARNING, "Whatsapp Message Sender interrupted. Shutting down Whatsapp Message Sender");
		}
	}
}