/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatMessageListener;
import com.skype.Skype;
import com.skype.SkypeException;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "SKYPE_SERVICE")
@DiscriminatorValue(value = "SKYPE_SERVICE")
public class SkypeService extends BridgeService
{

	transient private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT };

	@Override
	public void connect()
	{
		try
		{
			ShadowManager.log(Level.INFO, "Starting Skype Service...");
			Skype.setDaemon(false);
			Skype.addChatMessageListener(new SkypeChatListener());
			ShadowManager.log(Level.INFO, "Starting Skype Service...");
		} catch (SkypeException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void disconnect()
	{
	}

	@Override
	public void sendMessage(Message message)
	{
		try
		{
			Chat[] chats = Skype.getAllChats();
			for (int i = 0; i < chats.length; i++)
			{
				if (chats[i].getId().equals(message.getDestination().getIdentifier()))
				{
					chats[i].send(message.toSimpleString(getSupportedMessageFormats()));
					return;
				}
			}
		} catch (SkypeException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String getName()
	{
		return "Skype";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	private class SkypeChatListener implements ChatMessageListener
	{

		public SkypeChatListener()
		{
		}

		@Override
		public void chatMessageReceived(ChatMessage receivedChatMessage) throws SkypeException
		{
			String chatID = receivedChatMessage.getChat().getId();
			String message = receivedChatMessage.getContent();
			String sender = receivedChatMessage.getSenderDisplayName();
			Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(chatID, SkypeService.this);
			User user = DataManager.getOrNewUserForIdentifier(sender, endpoint);
			Message bMessage = new Message(user, endpoint, message, getSupportedMessageFormats()[0]);
			receiveMessage(bMessage);
		}

		@Override
		public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException
		{
		}
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats;
	}

	public void configure()
	{		
	}
}
