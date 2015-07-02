/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.whatsapp;

import bridgempp.*;
import bridgempp.messageformat.MessageFormat;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class WhatsappService implements BridgeService {

	HashMap<String, Endpoint> endpoints;

	Process yowsup;
	BufferedReader bufferedReader;
	// private PrintStream printStream;
	LinkedBlockingQueue<String> senderQueue;
	Thread senderThread;
	String phone;
	String password;

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
		new Thread(new WhatsappMessageListener(this), "Whatsapp Message Listener").start();
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
			senderQueue.add("/message send "
					+ message.getTarget().getTarget().substring(0, message.getTarget().getTarget().indexOf("@"))
					+ " \""
					+ Base64.getEncoder().encodeToString(
							(message.getSender().toString(true) + ": " + message.getMessage(supportedMessageFormats))
									.getBytes("UTF-8")) + "\"");
		} catch (UnsupportedEncodingException e) {
			ShadowManager.log(Level.SEVERE, "Base64 Encode: No such UTF-8", e);
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

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
}
