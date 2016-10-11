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
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.persistence.Version;

import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.service.BridgeService;
import bridgempp.util.StringOperations;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "ENDPOINT")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ENDPOINT_TYPE", discriminatorType=DiscriminatorType.STRING, length = 50)
@DiscriminatorValue("Endpoint")
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
    public void sendMessage(Message message, DeliveryGoal deliveryGoal) {
        if(!service.isEnabled())
        {
        	return;
        }
        service.processMessage(message, deliveryGoal);
    }

    public void sendOperatorMessage(String message) {
    	//TODO: This message needs a Sender
        new MessageBuilder(null, this).addPlainTextBody("BridgeMPP: " + message).addMessageDestination(this).build().send();
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
