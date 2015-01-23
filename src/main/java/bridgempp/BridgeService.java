/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.ArrayList;

import bridgempp.messageformat.MessageFormat;


/**
 *
 * @author Vinpasso
 */
public abstract class BridgeService {
	protected ArrayList<Endpoint> endpoints;
	
    //Initialize Service
    public abstract void connect(String args);
    //Deinitialize Service
    public abstract void disconnect();
    
    //Send message bridged from other Messages
    public abstract void sendMessage(Message message);
    
    //Get user-friendly name of this Service
    public abstract String getName();
    //Check whether this Service is persistent across restarts
    public abstract boolean isPersistent();
    
    public abstract void interpretCommand(Message message);
    
    //Add Endpoint from Save to list of Endpoints
    public abstract void addEndpoint(Endpoint endpoint);
    
    //Get the Supported Message Encodings by this Endpoint in order of descending priority
    public abstract MessageFormat[] getSupportedMessageFormats();
    
	public BridgeService()
	{
		endpoints = new ArrayList<>();
	}
	
	public ArrayList<Endpoint> listRegisteredEndpoints()
	{
		return endpoints;
	}
	
	public void broadcastMessage(Message message)
	{
		for(Endpoint endpoint : endpoints)
		{
			message.setTarget(endpoint);
			endpoint.sendMessage(message);
		}
	}
}
