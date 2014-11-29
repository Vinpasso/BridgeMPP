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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vinpasso
 */
public class WhatsappService implements BridgeService {

	private ArrayList<Endpoint> endpoints;

	private Process yowsup;
	private Scanner scanner;
	// private PrintStream printStream;
	private LinkedBlockingQueue<String> senderQueue;
	private Thread senderThread;

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.HTML,
			MessageFormat.PLAIN_TEXT };

	@Override
	public void connect(String argString) {
		ShadowManager.log(Level.INFO, "Starting Whatsapp Service...");
		String[] args = argString.split("; ");
		if (args.length != 3) {
			throw new UnsupportedOperationException("Incorrect Parameters for Whatsapp Service: " + argString);
		}
		String yowsupConfig = "cc=" + args[0] + "\nphone=" + args[1] + "\npassword=" + args[2];
		try {
			File configFile = new File(BridgeMPP.getPathLocation() + "/yowsup/src/credentials.config");
			if (!configFile.exists()) {
				configFile.createNewFile();
			}
			PrintWriter configFileWriter = new PrintWriter(configFile);
			configFileWriter.println(yowsupConfig);
			configFileWriter.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
		}
		endpoints = new ArrayList<>();
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
			senderQueue.add(message.getTarget().getTarget());
			senderQueue.add(Base64.getEncoder().encodeToString(
					message.toSimpleString(getSupportedMessageFormats()).getBytes("UTF-8")));
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
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
		endpoints.add(endpoint);
	}
	
	@Override
	public void interpretCommand(bridgempp.Message message) {
		message.getSender().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}

	private class WhatsappSender implements Runnable {

		private PrintStream realOutputStream;
		private boolean isBody = false;
		private long lastMessage = 0;

		public WhatsappSender(OutputStream realOutputStream) {
			this.realOutputStream = new PrintStream(realOutputStream, true);
		}

		@Override
		public void run() {
			try {

				while (true) {
					String line = senderQueue.take();
					if (line == null) {
						return;
					}
					realOutputStream.println(line);

					if (isBody) {
						lastMessage = System.currentTimeMillis();
						isBody = false;
					} else {
						isBody = true;
					}
					if (isBody && System.currentTimeMillis() - lastMessage < 10000) {
						Thread.sleep(System.currentTimeMillis() - lastMessage);
					}
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
						: "python";
				ProcessBuilder builder = new ProcessBuilder(pythonCommand, "-u", BridgeMPP.getPathLocation()
						+ "/yowsup/src/yowsup-cli", "-c", BridgeMPP.getPathLocation()
						+ "/yowsup/src/credentials.config", "-i", "4915110865845", "-k", "-a");
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
				senderThread = new Thread(new WhatsappSender(yowsup.getOutputStream()), "Whatsapp Sender");
				senderThread.start();
				scanner = new Scanner(yowsup.getInputStream());
				ShadowManager.log(Level.INFO, "Started Yowsup Process");
				while (true) {
					try {
						String line = scanner.nextLine();
						if (!line.contains(" [") || !line.contains("]:")) {
							ShadowManager.log(Level.INFO, "Restarting Yowsup process due to: " + line);
							break;
						}
						String sender = line.substring(0, line.indexOf(" ["));
						String extra = "";
						if (sender.endsWith("@g.us")) {
							extra = sender.substring(0, sender.indexOf("@s.whatsapp.net")) + "@s.whatsapp.net";
							sender = sender.substring(extra.length() + 1);
						}
						String message = new String(Base64.getDecoder().decode(
								line.substring(line.indexOf("]:") + 2).trim()), "UTF-8");
						boolean found = false;
						for (int i = 0; i < endpoints.size(); i++) {
							if (endpoints.get(i).getTarget().equals(sender)) {
								endpoints.get(i).setExtra(extra);
								Message bMessage = new Message(endpoints.get(i), message, MessageFormat.PLAIN_TEXT);
								CommandInterpreter.processMessage(bMessage);
								found = true;
								break;
							}
						}
						if (!found) {
							Endpoint endpoint = new Endpoint(WhatsappService.this, sender);
							endpoint.setExtra(extra);
							endpoints.add(endpoint);
							Message bMessage = new Message(endpoint, message, MessageFormat.PLAIN_TEXT);
							CommandInterpreter.processMessage(bMessage);
						}
					} catch (UnsupportedEncodingException ex) {
						Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				senderThread.interrupt();
				yowsup.destroyForcibly();
			} catch (IOException ex) {
				Logger.getLogger(WhatsappService.class.getName()).log(Level.SEVERE, null, ex);
			}
			ShadowManager.log(Level.INFO, "Stopped Yowsup Process");
		}

	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
}
