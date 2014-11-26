/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

/**
 *
 * @author Vincent Bode
 */
public class Message {

    private String message;
    private Endpoint sender;
    private Endpoint target;
    private Group group;

    public Message() {
        this(null, null, null, "");
    }

    public Message(String message) {
        this(null, null, null, message);
    }

    public Message(Endpoint sender, String message) {
        this(sender, null, null, message);
    }

    public Message(Endpoint sender, Endpoint target, Group group, String message) {
        this.sender = sender;
        this.target = target;
        this.group = group;
        this.message = message;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the sender
     */
    public Endpoint getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(Endpoint sender) {
        this.sender = sender;
    }

    /**
     * @return the target
     */
    public Endpoint getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(Endpoint target) {
        this.target = target;
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    public String toSimpleString() {
        return ((getSender() != null)?getSender().toString():"Unknown") + ": " + getMessage();
    }

    public String toComplexString() {
        return (getGroup() != null)?(getGroup().getName() + ": "):"Direct Message: " + ((getSender() != null)?getSender().toString():"Unknown") + " --> " + ((getTarget() != null)?(getTarget().toString() + ": "):("Unknown")) + getMessage();
    }

    @Override
    public String toString() {
        return toComplexString();
    }
}
