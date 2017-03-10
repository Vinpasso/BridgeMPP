package bridgempp.services.whatsapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.BridgeMPP;
import bridgempp.ServiceManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.data.processing.Schedule;
import bridgempp.log.Log;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;

class WhatsappMessageListener implements Runnable {

	private static final Pattern REGEX_MESSAGE = Pattern
			.compile("\\[([\\d]*?)\\/([^\\(]*?)\\(([^()]*?)\\)\\]:\\[([^()]*?)]\\s*?(\\S+)");
	private static final Pattern MESSAGE_SENT_CONFIRMATION = Pattern.compile("message sent");
	private static final long READ_TIMEOUT = 300000l;

	/**
	 * 
	 */
	final WhatsappService whatsappService;
	private long lastRead;

	/**
	 * @param whatsappService
	 */
	WhatsappMessageListener(WhatsappService whatsappService) {
		this.whatsappService = whatsappService;
	}

	@Override
	public void run() {
		Log.log(Level.INFO, "Whatsapp Message Listener starting up");
		Future<?> keepAlive = Schedule.scheduleRepeatWithPeriod(() -> checkLastSuccessfulRead(), READ_TIMEOUT, READ_TIMEOUT, TimeUnit.MILLISECONDS);
		process();
		keepAlive.cancel(false);
		Log.log(Level.INFO, "Whatsapp Message Listener shutting down");
		ServiceManager.onServiceError(whatsappService, "WhatsApp Message Listener offline", null);
	}

	public void process() {
		try {
			Log.log(Level.INFO, "Starting Yowsup Process...");
			String pythonCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "C:\\python27\\python.exe"
					: "python3";
			String[] launchCommand = {pythonCommand, "-u", BridgeMPP.getPathLocation()
					+ "/yowsup/src/yowsup-cli", "demos", "-l", this.whatsappService.phone + ":"
					+ this.whatsappService.password, "--yowsup"};
			Log.log(Level.INFO, "Yowsup launch command: " + Arrays.stream(launchCommand).reduce("", (a, v) -> a + " " + v));
			ProcessBuilder builder = new ProcessBuilder(launchCommand);
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
			whatsappService.printStream = new PrintStream(whatsappService.yowsup.getOutputStream(), true);
			this.whatsappService.bufferedReader = new BufferedReader(new InputStreamReader(
					this.whatsappService.yowsup.getInputStream(), "UTF-8"));
			Log.log(Level.INFO, "Started Yowsup Process");
			while (true) {
				String buffer = "";
				do {
					buffer += this.whatsappService.bufferedReader.readLine() + "\n";
				} while (this.whatsappService.bufferedReader.ready());
				if (buffer.trim().equals("null")) {
					break;
				}
				Log.log(Level.INFO, "YOWSUP Buffer: " + buffer);
				lastRead = System.currentTimeMillis();
				Matcher matcher = REGEX_MESSAGE.matcher(buffer);
				while (matcher.find()) {
					String author = matcher.group(1) + "@s.whatsapp.net";
					String group = matcher.group(2);
					String message = new String(Base64.getDecoder().decode(matcher.group(5)), "UTF-8");
					Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(group, whatsappService);
					User user = DataManager.getOrNewUserForIdentifier(author, endpoint);
					Message parsedMessage = new MessageBuilder(user, endpoint).addPlainTextBody(message).build();
					whatsappService.receiveMessage(parsedMessage);
				}
				if(MESSAGE_SENT_CONFIRMATION.matcher(buffer).find())
				{
					whatsappService.messageConfirmed = true;
					whatsappService.notify();
				}
			}
		} catch (UnsupportedOperationException | IOException ex) {
			ServiceManager.onServiceError(whatsappService, "WhatsApp Message Listener error", ex);
		}
		if(whatsappService.yowsup != null)
		{
			this.whatsappService.yowsup.destroyForcibly();
		}
		Log.log(Level.INFO, "Stopped Yowsup Process");
	}
	
	private void checkLastSuccessfulRead()
	{
		long differenceToLastRead = System.currentTimeMillis() - lastRead;
		if(differenceToLastRead > READ_TIMEOUT)
		{
			Log.log(Level.WARNING, "Last successful Yowsup read surpassed timeout of " + READ_TIMEOUT + "ms (Last read was " + (differenceToLastRead /1000) + "s ago)." );
			
		} else
		{
			Log.info("Yowsup read timeout OK. Last read was " + (differenceToLastRead / 1000) + "s ago.");
		}
	}
}