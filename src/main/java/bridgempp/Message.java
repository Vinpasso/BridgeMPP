/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.messageformat.MessageFormat;

/**
 *
 * @author Vincent Bode
 */
public class Message {

    private String message;
    private MessageFormat messageFormat;
    private Endpoint sender;
    private Endpoint target;
    private Group group;

    public Message() {
        this(null, null, null, "", MessageFormat.PLAIN_TEXT);
    }

    public Message(String message) {
        this(null, null, null, message, MessageFormat.PLAIN_TEXT);
    }

    @Deprecated
    public Message(Endpoint sender, String message) {
        this(sender, null, null, message, MessageFormat.PLAIN_TEXT);
    }
    
    public Message(Endpoint sender, String message, MessageFormat messageFormat)
    {
    	this(sender, null, null, message, messageFormat);
    }

    public Message(Endpoint sender, Endpoint target, Group group, String message, MessageFormat messageFormat) {
        this.sender = sender;
        this.target = target;
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
    
    public String getMessage(MessageFormat[] formats)
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

    public String toSimpleString(MessageFormat[] messageFormats) {
        return ((getSender() != null)?getSender().toString():"Unknown") + ": " + getMessage(messageFormats);
    }

    public String toComplexString(MessageFormat[] messageFormats) {
    	String messageFormat = chooseMessageFormat(messageFormats).getName() + ": ";
        String group = (getGroup() != null)?(getGroup().getName() + ": "):"Direct Message: ";
        String sender = (getSender() != null)?getSender().toString():"Unknown";
        String target = (getTarget() != null)?(getTarget().toString() + ": "):("Unknown: ");
        return messageFormat + group + sender + " --> " + target + getMessage(messageFormats);
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
        return toComplexString(new MessageFormat[] {MessageFormat.PLAIN_TEXT});
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
