package bridgempp.services.whatsapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.BridgeMPP;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;

class WhatsappMessageListener implements Runnable {

	private static final Pattern REGEX_MESSAGE = Pattern
			.compile("\\[([^\\/]*?)\\/([^\\(]*?)\\(([^()]*?)\\)\\]:\\[([^()]*?)]\\s*?(\\S+)");
	/**
	 * 
	 */
	final WhatsappService whatsappService;

	/**
	 * @param whatsappService
	 */
	WhatsappMessageListener(WhatsappService whatsappService) {
		this.whatsappService = whatsappService;
	}

	@Override
	public void run() {
		ShadowManager.log(Level.INFO, "Whatsapp Message Listener starting up");
		process();
		ShadowManager.log(Level.INFO, "Whatsapp Message Listener shutting down");
		ShadowManager.fatal("Whatsapp Message Listener offline");
	}

	public void process() {
		try {
			ShadowManager.log(Level.INFO, "Starting Yowsup Process...");
			String pythonCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "C:\\python27\\python.exe"
					: "python3";
			ProcessBuilder builder = new ProcessBuilder(pythonCommand, "-u", BridgeMPP.getPathLocation()
					+ "/yowsup/src/yowsup-cli", "demos", "-l " + this.whatsappService.phone + ":"
					+ this.whatsappService.password, "--yowsup");
			builder.directory(new File(BridgeMPP.getPathLocation() + "/yowsup/src/"));
			builder.environment().put("PYTHONPATH", BridgeMPP.getPathLocation() + "/yowsup/src/");
			this.whatsappService.yowsup = builder.start();
			new Thread(new WhatsappErrorListener(this), "Whatsapp Error Listener").start();
			this.whatsappService.yowsup.getOutputStream().write(
					("/login " + this.whatsappService.phone + " " + this.whatsappService.password + "\n\n\n")
							.getBytes());
			// LOGIN
			this.whatsappService.yowsup.getOutputStream().flush();
			// Make sure Login actually happens
			this.whatsappService.senderThread = new Thread(new WhatsappSender(this.whatsappService,
					this.whatsappService.yowsup.getOutputStream()), "Whatsapp Sender");
			this.whatsappService.senderThread.start();
			this.whatsappService.bufferedReader = new BufferedReader(new InputStreamReader(
					this.whatsappService.yowsup.getInputStream(), "UTF-8"));
			ShadowManager.log(Level.INFO, "Started Yowsup Process");
			while (true) {
				String buffer = "";
				do {
					buffer += this.whatsappService.bufferedReader.readLine() + "\n";
				} while (this.whatsappService.bufferedReader.ready());
				if (buffer.trim().equals("null")) {
					break;
				}
				ShadowManager.log(Level.INFO, "YOWSUP Buffer: " + buffer);
				Matcher matcher = REGEX_MESSAGE.matcher(buffer);
				while (matcher.find()) {
					String author = matcher.group(1) + "@s.whatsapp.net";
					String group = matcher.group(2);
					String message = new String(Base64.getDecoder().decode(matcher.group(5)), "UTF-8");
					Endpoint endpoint;
					if (this.whatsappService.endpoints.containsKey(group)) {
						endpoint = this.whatsappService.endpoints.get(group);
						endpoint.setExtra(author);
					} else {
						endpoint = new Endpoint(this.whatsappService, group);
						endpoint.setExtra(author);
						this.whatsappService.endpoints.put(group, endpoint);
					}
					Message parsedMessage = new Message(endpoint, message, MessageFormat.PLAIN_TEXT);
					CommandInterpreter.processMessage(parsedMessage);
				}
			}
		} catch (UnsupportedOperationException | IOException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
		this.whatsappService.senderThread.interrupt();
		this.whatsappService.yowsup.destroyForcibly();
		ShadowManager.log(Level.INFO, "Stopped Yowsup Process");
	}
}