/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.whatsapp;

import bridgempp.*;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.jivesoftware.smackx.pubsub.ConfigureForm;

/**
 *
 * @author Vinpasso
 */
public class WhatsappService extends BridgeService {
	
	Process yowsup;
	BufferedReader bufferedReader;
	// private PrintStream printStream;
	LinkedBlockingQueue<String> senderQueue;
	Thread senderThread;
	String phone;
	String password;

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT };

	@Override
	public void connect() {
		ShadowManager.log(Level.INFO, "Starting Whatsapp Service...");
		senderQueue = new LinkedBlockingQueue<>();
		new Thread(new WhatsappMessageListener(this), "Whatsapp Message Listener").start();
		ShadowManager.log(Level.INFO, "Service Whatsapp started");
	}
	
	void configure(String phone, String password)
	{
		this.phone = phone;
		this.password = password;
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
					+ message.getDestination().getIdentifier().substring(0, message.getDestination().getIdentifier().indexOf("@"))
					+ " \""
					+ Base64.getEncoder().encodeToString(
							(message.getSender().toString() + ": " + message.getMessage(supportedMessageFormats))
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
	public void interpretCommand(bridgempp.Message message) {
		message.getOrigin().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
}
