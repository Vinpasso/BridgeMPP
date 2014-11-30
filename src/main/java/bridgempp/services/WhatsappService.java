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
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Vinpasso
 */
public class WhatsappService implements BridgeService {

	private ArrayList<Endpoint> endpoints;

	private Process yowsup;
	private BufferedReader bufferedReader;
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
		senderQueue.add("<message \"type=\"" + (message.getMessageFormat().equals(MessageFormat.PLAIN_TEXT)?"text":"media") + 
				"to=\"" + message.getTarget().getTarget() + 
				"\"body=\"" + message.toSimpleString(getSupportedMessageFormats()) +
				"/>");
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
				bufferedReader = new BufferedReader(new InputStreamReader(yowsup.getInputStream(), "UTF-8"));
				ShadowManager.log(Level.INFO, "Started Yowsup Process");
				while (true) {
					String buffer = "";
					do
					{
						buffer += bufferedReader.readLine() + "\n";
					} while(bufferedReader.ready());
					Matcher matcher = Pattern.compile("(?<=<message>).+?(?=<\\/message>)", Pattern.DOTALL).matcher(buffer);
					while(matcher.find())
					{
						String message = matcher.group();
						Message parsedMessage = parseMessage(message);
						if(parsedMessage == null)
						{
							throw new UnsupportedOperationException("Non parseable Message: " + message);
						}
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

		private Message parseMessage(String message) {
			Matcher matcher = Pattern.compile(".").matcher(message);
			Message constructedMessage = new Message();
			while(matcher.find())
			{
				String value = matcher.group().substring(matcher.group().indexOf(": ") + 2);
				switch(matcher.group().substring(0, matcher.group().indexOf(": ")))
				{
				case "Message":
					break;
				case "ID":
					break;
				case "From":
					for(int i = 0; i < endpoints.size(); i++)
					{
						if(endpoints.get(i).getTarget().equals(matcher.group().substring(matcher.group().indexOf(": " + 2))))
						{
							constructedMessage.setSender(endpoints.get(i));
						}
					}
					if(constructedMessage.getTarget() == null)
					{
						Endpoint endpoint = new Endpoint(WhatsappService.this, matcher.group().substring(matcher.group().indexOf(": ") + 2));
						endpoints.add(endpoint);
						constructedMessage.setSender(endpoint);
					}
					break;
				case "Type":
					if(value.equals("text"))
					{
						constructedMessage.setMessageFormat(MessageFormat.PLAIN_TEXT);
					}
					else if(value.equals("media"))
					{
						constructedMessage.setMessageFormat(MessageFormat.HTML);
					}
					break;
				case "Participant":
					constructedMessage.getSender().setExtra(value);
					break;
				case "Body":
					constructedMessage.setMessage(value);
					break;
				case "URL":
					constructedMessage.setMessage("<img style='' alt='WhatsApp Image " + value + "' src=" + value + "/>");
					break;
				}

			}
			return constructedMessage;
		}
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
}
