/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import bridgempp.BridgeService;
import bridgempp.CommandInterpreter;
import bridgempp.Endpoint;
import bridgempp.ShadowManager;
import com.skype.Chat;
import com.skype.ChatMessage;
import com.skype.ChatMessageListener;
import com.skype.Skype;
import com.skype.SkypeException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vinpasso
 */
public class SkypeService implements BridgeService {

    private ArrayList<Endpoint> endpoints;

    @Override
    public void connect(String args) {
        try {
            ShadowManager.log(Level.INFO, "Starting Skype Service...");
            Skype.setDaemon(false);
            Skype.addChatMessageListener(new SkypeChatListener());
            ShadowManager.log(Level.INFO, "Starting Skype Service...");
        } catch (SkypeException ex) {
            Logger.getLogger(SkypeService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void returnToSender(String target, String response) {
        sendMessage(target, response);
    }

    @Override
    public void sendMessage(String target, String response) {
        try {
            Chat[] chats = Skype.getAllChats();
            for (int i = 0; i < chats.length; i++) {
                if (chats[i].getId().equals(target)) {
                    chats[i].send(response);
                    return;
                }
            }
        } catch (SkypeException ex) {
            Logger.getLogger(SkypeService.class.getName()).log(Level.SEVERE, null, ex);
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
    public void addEndpoint(Endpoint endpoint) {
        endpoints.add(endpoint);
    }

    private class SkypeChatListener implements ChatMessageListener {

        public SkypeChatListener() {
            endpoints = new ArrayList<>();
        }

        @Override
        public void chatMessageReceived(ChatMessage receivedChatMessage) throws SkypeException {
            String chatID = receivedChatMessage.getChat().getId();
            String message = receivedChatMessage.getContent();
            String sender = receivedChatMessage.getSenderDisplayName();
            for (int i = 0; i < endpoints.size(); i++) {
                if (endpoints.get(i).getTarget().equals(chatID)) {
                    endpoints.get(i).setExtra(sender);
                    CommandInterpreter.processMessage(message, endpoints.get(i));
                    return;
                }
            }
            Endpoint endpoint = new Endpoint(SkypeService.this, chatID);
            endpoint.setExtra(sender);
            endpoints.add(endpoint);
            CommandInterpreter.processMessage(message, endpoint);
        }

        @Override
        public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException {
        }
    }
}
