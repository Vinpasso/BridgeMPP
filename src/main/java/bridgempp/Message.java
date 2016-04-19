/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

/**
 *
 * @author Vincent Bode
 */
public class Message {

    private String message;
    private MessageFormat messageFormat;
    private User sender;
    private Endpoint origin;
    private Endpoint destination;
    private Group group;

    public Message() {
        this(null, null, null, null, "", MessageFormat.PLAIN_TEXT);
    }
    
    public Message(User sender, Endpoint origin, String message, MessageFormat messageFormat)
    {
    	this(sender, origin, null, null, message, messageFormat);
    }

    public Message(User sender, Endpoint origin, Endpoint target, Group group, String message, MessageFormat messageFormat) {
    	this.sender = sender;
        this.origin = origin;
        this.destination = target;
        this.group = group;
        this.message = message;
        this.messageFormat = messageFormat;
    }

    /**
     * @return the message
     */
    public String getPlainTextMessage() {
        return getMessage(new MessageFormat[] {MessageFormat.PLAIN_TEXT});
    }
    
    public String getMessage(MessageFormat... formats)
    {
    	for(MessageFormat format : formats)
    	{
    		if(messageFormat.canConvertToFormat(format))
    		{
    			return messageFormat.convertToFormat(message, format);
    		}
    	}
    	return message;
    }
    
    public MessageFormat chooseMessageFormat(MessageFormat[] formats)
    {
    	for(MessageFormat format : formats)
    	{
    		if(messageFormat.canConvertToFormat(format))
    		{
    			return format;
    		}
    	}
    	return MessageFormat.PLAIN_TEXT;
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
	public User getSender()
	{
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(User sender)
	{
		this.sender = sender;
	}

	/**
     * @return the Origin
     */
    public Endpoint getOrigin() {
        return origin;
    }

    /**
     * @param sender the Origin to set
     */
    public void setOrigin(Endpoint sender) {
        this.origin = sender;
    }

    /**
     * @return the target
     */
    public Endpoint getDestination() {
        return destination;
    }

    /**
     * @param destination the Destination to set
     */
    public void setDestination(Endpoint destination) {
        this.destination = destination;
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

    /**
     * Return the End-User friendly Description of this Message
     * This attempts to format the Message in the following way:
     * <sender>: Message
     * If the sender is unavailable the origin is used instead
     * If the origin is unavailable unknown will be used
     * @param messageFormats The Messages Format to return the message in
     * @return The formatted Message
     */
    public String toSimpleString(MessageFormat... messageFormats) {
        String messageSender = (getSender() != null)?getSender().toString():null;
        String messageOrigin = (getOrigin() != null)?getOrigin().toString():"Unknown";
		return ((messageSender!=null)?messageSender:messageOrigin) + ": " + getMessage(messageFormats);
    }

    /**
     * This returns a more detailed description of the Message in the following format
     * <Message Format>: <Group>: <Sender> (<Origin>) --> <Destination>: <Message Format>"
     * This is the default format in the toString Method (Plain-Text)
     * @param messageFormats The requested format to return the message in
     * @return The formatted Message
     */
    public String toComplexString(MessageFormat... messageFormats) {
    	String messageFormat = chooseMessageFormat(messageFormats).getName() + ": ";
        String group = (getGroup() != null)?(getGroup().getName() + ": "):"Direct Message: ";
        String sender = (getSender() != null)?getSender().toString():"Unknown";
        String origin = (getOrigin() != null)?getOrigin().toString():"Unknown";
        String target = (getDestination() != null)?(getDestination().toString() + ": "):("Unknown: ");
        return messageFormat + group + sender + " (" + origin + ") --> " + target + getMessage(messageFormats);
    }
    
    public static Message parseMessage(String complexString)
    {
    	Message message = new Message();
    	String[] messageSplit = complexString.split("\\s*(?::|-->)\\s+", 5);
    	if(messageSplit.length == 5)
    	{
        	message.setMessageFormat(MessageFormat.parseMessageFormat(messageSplit[0]));
    		message.setGroup(GroupManager.findGroup(messageSplit[1]));
    		//Endpoint Sender
    		//Endpoint Recipient
    		message.setMessage(messageSplit[4]);
    	}
    	else
    	{
    		message.setMessage(complexString);
    	}
    	return message;
    }

    @Override
    public String toString() {
        return toComplexString(MessageFormat.PLAIN_TEXT);
    }

	/**
	 * @return the messageFormat
	 */
	public MessageFormat getMessageFormat() {
		return messageFormat;
	}

	/**
	 * @param messageFormat the messageFormat to set
	 */
	public void setMessageFormat(MessageFormat messageFormat) {
		this.messageFormat = messageFormat;
	}

	public String getMessageRaw() {
		return message;
	}
}
