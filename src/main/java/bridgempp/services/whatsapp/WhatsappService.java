/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.whatsapp;

import bridgempp.*;
import bridgempp.data.Endpoint;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.text.Base64PlainTextFormat;
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
	transient LinkedBlockingQueue<String> senderQueue;
	transient Thread senderThread;
	transient PrintStream printStream;
	private transient long lastMessageTimestamp = 0l;
	transient volatile boolean messageConfirmed = false;

	
	@Column(name = "Phone_Number", nullable = false, length = 50)
	String phone;
	
	@Column(name = "Password", nullable = false, length = 50) 
	String password;

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
	public synchronized void sendMessage(Message message, DeliveryGoal deliveryGoal) {
		Endpoint endpoint = deliveryGoal.getTarget();
		if(System.currentTimeMillis() - 10000 < lastMessageTimestamp)
		{
			try
			{
				Thread.sleep(Math.max(0, System.currentTimeMillis() - 10000 - lastMessageTimestamp));
			} catch (InterruptedException e)
			{
				ShadowManager.log(Level.WARNING, "Whatsapp send message interrupted");
				return;
			}
		}
		printStream.println("/message send "
					+ endpoint.getIdentifier().substring(0, endpoint.getIdentifier().indexOf("@"))
					+ " \""
					+ message.getMessageBody(Base64PlainTextFormat.class) + "\"");
		lastMessageTimestamp = System.currentTimeMillis();
		try
		{
			printStream.wait(10000);
		} catch (InterruptedException e)
		{
			ShadowManager.log(Level.WARNING, "Whatsapp send message confirmation interrupted");
		}
		if(messageConfirmed)
		{
			deliveryGoal.setDelivered();
			messageConfirmed = false;
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

	public void configure(String phone, String password)
	{
		this.phone = phone;
		this.password = password;
	}
}
