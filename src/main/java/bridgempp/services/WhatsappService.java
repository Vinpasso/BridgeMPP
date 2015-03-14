/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.*;
import bridgempp.command.CommandInterpreter;
import bridgempp.messageformat.MessageFormat;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Vinpasso
 */
public class WhatsappService implements BridgeService {

	private HashMap<String, Endpoint> endpoints;

	private Process yowsup;
	private BufferedReader bufferedReader;
	// private PrintStream printStream;
	private LinkedBlockingQueue<String> senderQueue;
	private Thread senderThread;
	private String phone;
	private String password;

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT };

	@Override
	public void connect(String argString) {
		ShadowManager.log(Level.INFO, "Starting Whatsapp Service...");
		String[] args = argString.split("; ");
		if (args.length != 2) {
			throw new UnsupportedOperationException("Incorrect Parameters for Whatsapp Service: " + argString);
		}
		phone = args[0];
		password = args[1];
		endpoints = new HashMap<>();
		senderQueue = new LinkedBlockingQueue<>();
		new Thread(new WhatsappMessageListener(), "Whatsapp Message Listener").start();
		ShadowManager.log(Level.INFO, "Service Whatsapp started");
	}

	@Override
	public void disconnect() {
		senderThread.interrupt();
		yowsup.destroy();
	}

	@Override
	public void sendMessage(Message message) {
		try {
			senderQueue.add("/message send " + message.getTarget().getTarget().substring(0, message.getTarget().getTarget().indexOf("@")) + " \"" +  Base64.getEncoder().encodeToString((message.getSender().toString(true) + ": " + message.getMessage(supportedMessageFormats)).getBytes("UTF-8")) + "\"");
		} catch (UnsupportedEncodingException e) {
			Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, "Base64 Encode: No such UTF-8", e);
		}
	}

	@Override
	public String getName() {
		return "Whatsapp";
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public void addEndpoint(Endpoint endpoint) {
		endpoints.put(endpoint.getTarget(), endpoint);
	}

	@Override
	public void interpretCommand(bridgempp.Message message) {
		message.getSender().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}

	private class WhatsappSender implements Runnable {

		private PrintStream realOutputStream;
		private long lastMessage = 0;

		public WhatsappSender(OutputStream realOutputStream) {
			try {
				this.realOutputStream = new PrintStream(realOutputStream, true, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try {

				while (true) {
					String line = senderQueue.take();
					if (line == null) {
						return;
					}
					if(System.currentTimeMillis() - lastMessage < 10000) {
						Thread.sleep(10000);
					}
					realOutputStream.println(line);
					lastMessage = System.currentTimeMillis();
				}
			} catch (InterruptedException ex) {
				Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	class WhatsappMessageListener implements Runnable {

		@Override
		public void run() {
			while (true) {
				ShadowManager.log(Level.INFO, "Whatsapp Message Listener starting up");
				process();
				ShadowManager.log(Level.INFO, "Whatsapp Message Listener shutting down");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException ex) {
					Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		public void process() {
			try {
				ShadowManager.log(Level.INFO, "Starting Yowsup Process...");
				String pythonCommand = System.getProperty("os.name").toLowerCase().contains("win") ? "C:\\python27\\python.exe"
						: "python3";
				ProcessBuilder builder = new ProcessBuilder(pythonCommand, "-u", BridgeMPP.getPathLocation()
						+ "/yowsup/src/yowsup-cli", "demos", "-l " + phone + ":" + password, "--yowsup");
				builder.directory(new File(BridgeMPP.getPathLocation() + "/yowsup/src/"));
				builder.environment().put("PYTHONPATH", BridgeMPP.getPathLocation() + "/yowsup/src/");
				yowsup = builder.start();
				new Thread(new Runnable() {

					@Override
					public void run() {
						ShadowManager.log(Level.INFO, "Starting Yowsup Error Listener...");

						Scanner error = new Scanner(yowsup.getErrorStream());
						while (error.hasNext()) {
							System.err.println(error.nextLine());
						}
						error.close();
						ShadowManager.log(Level.INFO, "Stopping Yowsup Error Listener");
					}
				}, "Whatsapp Error Listener").start();
				yowsup.getOutputStream().write(("/login " + phone + " " + password + "\n").getBytes()); //LOGIN
				senderThread = new Thread(new WhatsappSender(yowsup.getOutputStream()), "Whatsapp Sender");
				senderThread.start();
				bufferedReader = new BufferedReader(new InputStreamReader(yowsup.getInputStream(), "UTF-8"));
				ShadowManager.log(Level.INFO, "Started Yowsup Process");
				while (true) {
					String buffer = "";
					do
					{
						buffer += bufferedReader.readLine() + "\n";
					} while(bufferedReader.ready());
					if(buffer.trim().equals("null"))
					{
						break;
					}
					Logger.getLogger(WhatsappService.class.getName()).log(Level.INFO, "YOWSUP Buffer: " + buffer);
					Matcher matcher = Pattern.compile("\\[([^\\[]*?)\\(([^()]*?)\\)\\]:\\[([^()]*?)]\\s*?(\\S+)").matcher(buffer);
					while(matcher.find())
					{
						String author = matcher.group(3);
						String group = matcher.group(1);
						String message = new String(Base64.getDecoder().decode(matcher.group(4)), "UTF-8");
						Endpoint endpoint;
						if(endpoints.containsKey(group))
						{
							endpoint = endpoints.get(group);
							endpoint.setExtra(author);
						}
						else
						{
							endpoint = new Endpoint(WhatsappService.this, group);
							endpoint.setExtra(author);
							endpoints.put(group, endpoint);
						}
						Message parsedMessage = new Message(endpoint, message, MessageFormat.PLAIN_TEXT);
						CommandInterpreter.processMessage(parsedMessage);
					}
				}
			} catch (UnsupportedOperationException | IOException ex) {
				Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
			}
			senderThread.interrupt();
			yowsup.destroyForcibly();
			ShadowManager.log(Level.INFO, "Stopped Yowsup Process");
		}
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
}
