/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import org.apache.commons.configuration.XMLConfiguration;

import bridgempp.messageformat.MessageFormat;

/**
 *
 * @author Vinpasso
 */
public class Endpoint {

    private BridgeService bridgeService;
    private String target;
    private String extra;
    private int permissions;

    //Create a new Endpoint
    public Endpoint(BridgeService bridgeService, String target) {
        this.bridgeService = bridgeService;
        this.target = target;
        extra = "";
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

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getExtra() {
        return extra;
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

    //To String
    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean humanreadable) {
        if (humanreadable) {
            String humanReadable = EndpointTranslator.getHumanReadableEndpoint(this);
            if (humanReadable.length() != 0) {
                return humanReadable;
            }
        }
        return getExtra() + ((getExtra().length() == 0) ? "" : "@") + getTarget() + "@" + getService().getName();
    }

    //Has Alias
    public boolean hasAlias() {
        return !EndpointTranslator.getHumanReadableEndpoint(this).isEmpty();
    }

    //Parameter node is the endpoint. With appended "."
    public static Endpoint readEndpoint(XMLConfiguration configuration, String node) {
        BridgeService service = ServiceManager.getService(configuration.getString(node + "service"));
        String target = configuration.getString(node + "target");
        String extra = configuration.getString(node + "extra");
        int permissions = configuration.getInt(node + "permissions");
        Endpoint endpoint = new Endpoint(service, target);
        endpoint.setExtra(extra);
        endpoint.setPermissions(permissions);
        return endpoint;
    }

    //Always creates a new Endpoint in List. Parameter node is the list in which the endpoint is created with appended "."
    public static void writeEndpoint(Endpoint endpoint, XMLConfiguration configuration, String node) {
        if (!endpoint.bridgeService.isPersistent()) {
            return;
        }
        configuration.addProperty(node + "endpoint(-1).service", endpoint.getService().getName());
        configuration.addProperty(node + "endpoint.target", endpoint.getTarget());
        configuration.addProperty(node + "endpoint.extra", endpoint.getExtra());
        configuration.addProperty(node + "endpoint.permissions", endpoint.getPermissions());
    }

    String getIdentifer() {
        if (extra.isEmpty()) {
            return getTarget();
        }
        return getExtra();
    }

}
