/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.service.BridgeService;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "CONSOLE_SERVICE")
@DiscriminatorValue(value = "CONSOLE_SERVICE")
public class ConsoleService extends BridgeService
{

	transient Scanner scanner;
	transient ConsoleReader reader;
	transient Thread consoleThread;

	@Override
	public void connect()
	{
		Log.log(Level.INFO, "Console Service is being loaded...");
		scanner = new Scanner(System.in);
		reader = new ConsoleReader();
		consoleThread = new Thread(reader, "Console Reader");
		consoleThread.start();
		Log.log(Level.INFO, "Console Service has been loaded...");
	}

	@Override
	public void disconnect()
	{
		Log.log(Level.WARNING, "Console service has been disconnected...");
		scanner.close();
	}

	@Override
	public void sendMessage(Message message, DeliveryGoal deliveryGoal)
	{
		System.out.println(message.getPlainTextMessageBody());
		deliveryGoal.setDelivered();
	}

	@Override
	public String getName()
	{
		return "Console";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	class ConsoleReader implements Runnable
	{

		@Override
		public void run()
		{
			Log.log(Level.FINE, "Console reader is running...");
			try
			{
				while (true)
				{
					if (System.in.available() > 0)
					{
						Endpoint origin = DataManager.getOrNewEndpointForIdentifier("Console", ConsoleService.this);
						User user = DataManager.getOrNewUserForIdentifier("Console", origin);
						Message message = new MessageBuilder(user, origin).addPlainTextBody(getName()).build();
						receiveMessage(message);
					} else
					{
						Thread.sleep(1000);
					}
				}
			} catch (IllegalStateException e)
			{
				Log.log(Level.WARNING, "System_IN was closed");
			} catch (InterruptedException e)
			{
				Log.log(Level.WARNING, "Shutting down Console Reader due to interrupt");
			} catch (IOException e)
			{
				Log.log(Level.WARNING, "System_IN was closed");
			}
			Log.log(Level.FINE, "Console reader has closed");
		}

	}

}
