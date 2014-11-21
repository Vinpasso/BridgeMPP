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
public class Group {
    private ArrayList<Endpoint> endpoints;
    private String name;
    
    //Send message to all recipients in this group
    public void sendMessage(String message)
    {
        for(int i = 0; i < getEndpoints().size(); i++)
        {
            getEndpoints().get(i).sendMessage(message);
        }
    }
    
    //Send message without Loopback to sender
    public void sendMessageWithoutLoopback(String message, Endpoint sender)
    {
        for(int i = 0; i < getEndpoints().size(); i++)
        {
            //Check that Sender does not get the Message sent back to him
            if(!endpoints.get(i).equals(sender))
            {
                getEndpoints().get(i).sendMessage(message);
            }
        }
    }
    
    //Add a user/group to this groups recipients
    public void addEndpoint(Endpoint endpoint)
    {
        getEndpoints().add(endpoint);
    }
    
    //Remove a user/group from this groups recipients 
    public void removeEndpoint(Endpoint endpoint)
    {
        getEndpoints().remove(endpoint);
    }
    
    //Remove all endpoints in preperation for Group destruction
    public void removeAllEndpoints() {
        for(int i = 0; i < getEndpoints().size(); i++)
        {
            removeEndpoint(getEndpoints().get(i));
        }
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return name;
    }
    
    //Initialize the group endpoints
    public Group()
    {
        endpoints = new ArrayList<>();
        name = "NewGroup";
    }

    boolean hasEndpoint(Endpoint endpoint) {
        return getEndpoints().contains(endpoint);
    }

    @Override
    //Prints out its Members
    public String toString() {
        String members = "";
        for(int i = 0; i < getEndpoints().size(); i++)
        {
            members += "Member: " + getEndpoints().get(i).toString() + "\n";
        }
        return members;
    }

    /**
     * @return the endpoints
     */
    public ArrayList<Endpoint> getEndpoints() {
        return endpoints;
    }

}
