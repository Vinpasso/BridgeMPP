/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import bridgempp.BridgeService;
import bridgempp.Message;
import bridgempp.messageformat.MessageFormat;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "ENDPOINT")
public class Endpoint {
    @Id
    @Column(name = "IDENTIFIER", nullable = false, length = 50)
    private String identifier;
	
	@Column(name = "SERVICE", nullable = false, length = 50)
	private String serviceName;
	//What to do with this?
    private BridgeService bridgeService;
    
    @ManyToMany
    @JoinTable(name = "ENDPOINT_USERS", joinColumns = @JoinColumn(name = "ENDPOINT_ID", referencedColumnName = "ENDPOINT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID"))
    private Collection<User> users;
    
    @Column(name = "PERMISSIONS", nullable = false)
    private int permissions;
    
    @ManyToMany(mappedBy = "endpoints")
    private Collection<Group> groups;
    
    //For constructing by Persistence
    Endpoint()
    {
    	
    }
    
    //Create a new Endpoint
    Endpoint(BridgeService bridgeService, String identifier) {
        this.bridgeService = bridgeService;
        this.identifier = identifier;
        permissions = 0;
    }

    //Send this endpoint a Message (convenience)
    public void sendMessage(Message message) {
        message.setDestination(this);
        bridgeService.sendMessage(message);
    }

    public void sendOperatorMessage(String message) {
    	//TODO: This message needs a Sender
        sendMessage(new Message(null, this, this, null, "BridgeMPP: " + message, MessageFormat.PLAIN_TEXT));
    }

    //Get this endpoints Carrier Service
    public BridgeService getService() {
        return bridgeService;
    }

    //Get this endpoints Carrier Identifier
    public String getIdentifier() {
        return identifier;
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

	public void putUser(User user)
	{
		if(!users.contains(user))
		{
			users.add(user);
		}
	}

}
