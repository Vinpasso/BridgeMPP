/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.*;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;


import java.util.ArrayList;
import java.util.logging.Level;

import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatMessageListener;
import com.skype.Skype;
import com.skype.SkypeException;

/**
 *
 * @author Vinpasso
 */
public class SkypeService implements BridgeService {

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT };

	@Override
	public void connect(String args) {
		try {
			ShadowManager.log(Level.INFO, "Starting Skype Service...");
			Skype.setDaemon(false);
			Skype.addChatMessageListener(new SkypeChatListener());
			ShadowManager.log(Level.INFO, "Starting Skype Service...");
		} catch (SkypeException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void disconnect() {
	}

	@Override
	public void sendMessage(Message message) {
		try {
			Chat[] chats = Skype.getAllChats();
			for (int i = 0; i < chats.length; i++) {
				if (chats[i].getId().equals(message.getDestination().getIdentifier())) {
					chats[i].send(message.toSimpleString(getSupportedMessageFormats()));
					return;
				}
			}
		} catch (SkypeException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public String getName() {
		return "Skype";
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public void interpretCommand(Message message) {
		message.getOrigin().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}

	private class SkypeChatListener implements ChatMessageListener {

		public SkypeChatListener() {
		}

		@Override
		public void chatMessageReceived(ChatMessage receivedChatMessage) throws SkypeException {
			String chatID = receivedChatMessage.getChat().getId();
			String message = receivedChatMessage.getContent();
			String sender = receivedChatMessage.getSenderDisplayName();
			Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(chatID, SkypeService.this);
			User user = DataManager.getOrNewUserForIdentifier(sender, SkypeService.this, endpoint);
			Message bMessage = new Message(user, endpoint, message, getSupportedMessageFormats()[0]);
			CommandInterpreter.processMessage(bMessage);
		}

		@Override
		public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException {
		}
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
}
