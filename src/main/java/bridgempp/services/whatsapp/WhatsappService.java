/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.whatsapp;

import bridgempp.*;
import bridgempp.message.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;
import bridgempp.service.ServiceFilter;
import bridgempp.service.filter.RateLimiter;

import java.io.*;
import java.util.Base64;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "WHATSAPP_SERVICE_CONFIGURATION")
@DiscriminatorValue("WHATSAPP_SERVICE")
@ServiceFilter(RateLimiter.class)
public class WhatsappService extends BridgeService {
	
	transient Process yowsup;
	transient BufferedReader bufferedReader;
	// private PrintStream printStream;
	transient LinkedBlockingQueue<String> senderQueue;
	transient Thread senderThread;
	
	@Column(name = "Phone_Number", nullable = false, length = 50)
	String phone;
	
	@Column(name = "Password", nullable = false, length = 50) 
	String password;

	transient private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT };

	@Override
	public void connect() {
		ShadowManager.log(Level.INFO, "Starting Whatsapp Service...");
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
					+ message.getDestination().getIdentifier().substring(0, message.getDestination().getIdentifier().indexOf("@"))
					+ " \""
					+ Base64.getEncoder().encodeToString(
							message.toSimpleString(getSupportedMessageFormats())
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
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}

	public void configure(String phone, String password)
	{
		this.phone = phone;
		this.password = password;
	}
}
