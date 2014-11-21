/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.ArrayList;

/**
 *
 * @author Vinpasso
 */
public interface BridgeService {
    
    //Initialize Service
    public void connect(String args);
    //Deinitialize Service
    public void disconnect();
    
    //When delivery Fails notify User
    public void returnToSender(String target, String response);
    
    //Send message bridged from other Messages
    public void sendMessage(String target, String response);
    
    //Get user-friendly name of this endpoint
    public String getName();
    
    //Add Endpoint from Save to list of Endpoints
    public void addEndpoint(Endpoint endpoint);
}
