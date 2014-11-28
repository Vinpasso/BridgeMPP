/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;


/**
 *
 * @author Vinpasso
 */
public interface BridgeService {
    
    //Initialize Service
    public void connect(String args);
    //Deinitialize Service
    public void disconnect();
    
    //Send message bridged from other Messages
    public void sendMessage(Message message);
    
    //Get user-friendly name of this Service
    public String getName();
    //Check whether this Service is persistent across restarts
    public boolean isPersistent();
    
    //Add Endpoint from Save to list of Endpoints
    public void addEndpoint(Endpoint endpoint);
}
