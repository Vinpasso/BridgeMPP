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

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
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

	transient private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT };

	@Override
	public void connect()
	{
		ShadowManager.log(Level.INFO, "Console Service is being loaded...");
		scanner = new Scanner(System.in);
		reader = new ConsoleReader();
		consoleThread = new Thread(reader, "Console Reader");
		consoleThread.start();
		ShadowManager.log(Level.INFO, "Console Service has been loaded...");
	}

	@Override
	public void disconnect()
	{
		ShadowManager.log(Level.WARNING, "Console service has been disconnected...");
		scanner.close();
	}

	@Override
	public void sendMessage(Message message)
	{
		System.out.println(message.toComplexString(getSupportedMessageFormats()));
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
			ShadowManager.log(Level.FINE, "Console reader is running...");
			try
			{
				while (true)
				{
					if (System.in.available() > 0)
					{
						Endpoint origin = DataManager.getOrNewEndpointForIdentifier("Console", ConsoleService.this);
						User user = DataManager.getOrNewUserForIdentifier("Console", origin);
						Message message = new Message(user, origin, scanner.nextLine(), getSupportedMessageFormats()[0]);
						receiveMessage(message);
					} else
					{
						Thread.sleep(1000);
					}
				}
			} catch (IllegalStateException e)
			{
				ShadowManager.log(Level.WARNING, "System_IN was closed");
			} catch (InterruptedException e)
			{
				ShadowManager.log(Level.WARNING, "Shutting down Console Reader due to interrupt");
			} catch (IOException e)
			{
				ShadowManager.log(Level.WARNING, "System_IN was closed");
			}
			ShadowManager.log(Level.FINE, "Console reader has closed");
		}

	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats;
	}

}
