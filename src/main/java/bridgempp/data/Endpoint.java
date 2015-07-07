/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.data;

import java.util.Collection;

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
	@Column(name = "ENDPOINT_ID", nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long endpointID;
	
	@Column(name = "SERVICE", nullable = false, length = 50)
	private String serviceName;
	//What to do with this?
    private BridgeService bridgeService;

    @Column(name = "TARGET", nullable = false, length = 50)
    private String target;
    
    @ManyToMany
    @JoinTable(name = "ENDPOINT_USERS", joinColumns = @JoinColumn(name = "ENDPOINT_ID", referencedColumnName = "ENDPOINT_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID"))
    private Collection<User> users;
    
    @Column(name = "PERMISSIONS", nullable = false)
    private int permissions;
    
    @ManyToMany(mappedBy = "endpoints")
    private Collection<Group> groups;
    
    //For constructing by Persistence
    public Endpoint()
    {
    	
    }
    
    //Create a new Endpoint
    public Endpoint(BridgeService bridgeService, String target) {
        this.bridgeService = bridgeService;
        this.target = target;
        permissions = 0;
    }

    //Send this endpoint a Message (convenience)
    public void sendMessage(Message message) {
        message.setTarget(this);
        bridgeService.sendMessage(message);
    }

    public void sendOperatorMessage(String message) {
        sendMessage(new Message(this, this, null, "BridgeMPP: " + message, MessageFormat.PLAIN_TEXT));
    }

    //Get this endpoints Carrier Service
    public BridgeService getService() {
        return bridgeService;
    }

    //Get this endpoints Carrier Identifier
    public String getTarget() {
        return target;
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

}
