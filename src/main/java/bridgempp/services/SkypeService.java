/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
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
	public void sendMessage(Message message, Endpoint destination)
	{
		try
		{
			Chat[] chats = Skype.getAllChats();
			for (int i = 0; i < chats.length; i++)
			{
				if (chats[i].getId().equals(destination.getIdentifier()))
				{
					chats[i].send(message.getPlainTextMessageBody());
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
			Message bMessage = new MessageBuilder(user, endpoint).addPlainTextBody(message).build();
			receiveMessage(bMessage);
		}

		@Override
		public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException
		{
		}
	}

	public void configure()
	{		
	}
}
