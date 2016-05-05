/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.persistence.Version;

import bridgempp.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;
import bridgempp.util.StringOperations;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "ENDPOINT")
public class Endpoint {
    @Id
    @Column(name = "IDENTIFIER", nullable = false, length = 50)
    private String identifier;
	
	@ManyToOne(optional=false)
	@JoinColumn(name = "BRIDGE_SERVICE_IDENTIFIER", referencedColumnName = "SERVICE_IDENTIFIER")
	private BridgeService service;
    
    @ManyToMany
    @JoinTable(name = "ENDPOINT_USERS", joinColumns = @JoinColumn(name = "ENDPOINT_IDENTIFIER", referencedColumnName = "IDENTIFIER"), inverseJoinColumns = @JoinColumn(name = "USER_IDENTIFIER", referencedColumnName = "IDENTIFIER"))
    private Collection<User> users;
    
    @Column(name = "PERMISSIONS", nullable = false)
    private int permissions;
    
    @ManyToMany(mappedBy = "endpoints")
    private Collection<Group> groups;
    
	@Version
    @Column(name = "VERSION", nullable = false)
    private long version;
    
    /**
     * JPA Constructor
     */
    protected Endpoint()
    {
    	users = new ArrayList<>();
    	groups = new ArrayList<>();
    }
    
    //Create a new Endpoint
    Endpoint(BridgeService bridgeService, String identifier) {
    	this();
        this.service = bridgeService;
        this.identifier = identifier;
        permissions = 0;
    }

    //Send this endpoint a Message (convenience)
    public void sendMessage(Message message) {
        message.setDestination(this);
        if(!service.isEnabled())
        {
        	return;
        }
        service.processMessage(message);
    }

    public void sendOperatorMessage(String message) {
    	//TODO: This message needs a Sender
        sendMessage(new Message(null, this, this, null, "BridgeMPP: " + message, MessageFormat.PLAIN_TEXT));
    }

    //Get this endpoints Carrier Service
    public BridgeService getService() {
        return service;
    }

    //Get this endpoints Carrier Identifier
    public String getIdentifier() {
        return identifier;
    }
    
	public String getPartOneIdentifier()
	{
		return StringOperations.getPartOneIdentifier(identifier);
	}

    /**
     * @return the permissions
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    // OR with newPermissions
    public void addPermissions(int newPermissions) {
        permissions = permissions | newPermissions;
    }

    //AND negated removepermission
    public void removePermissions(int removePermissions) {
        permissions = permissions & ~removePermissions;
    }
    
    public String toString()
    {
    	return getIdentifier() + " " + getService().getName();
    }

	public Collection<User> getUsers()
	{
		return Collections.unmodifiableCollection(users);
	}

	/**
	 * BI-DIRECTIONAL
	 * @param user
	 */
	public void putUser(User user)
	{
		if(!users.contains(user))
		{
			users.add(user);
			user.addEndpointNonBidirectional(this);
		}
	}
	
	/**
	 * BI-DIRECTIONAL
	 * @param user
	 */
	public void removeUser(User user)
	{
		users.remove(user);
		user.removeEndpointNonBidirectional(this);
	}
	
    public Collection<Group> getGroups() {
		return groups;
	}

	public void unsubscribeAllGroups() {
		while(!groups.isEmpty())
		{
			groups.iterator().next().removeEndpoint(this);
		}
	}

	@PreRemove
	public void delete()
	{
		unsubscribeAllGroups();
	}

	/**
	 * Non BI-DIRECTIONAL
	 * @param group
	 */
	protected void addGroupNonBidirectional(Group group) {
		groups.add(group);
	}

	/**
	 * Non BI-DIRECTIONAL
	 * @param group
	 */
	protected void removeGroupNonBidirectional(Group group) {
		groups.remove(group);
	}
}
