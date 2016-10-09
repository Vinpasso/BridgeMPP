package bridgempp.servicecomponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.GroupManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.data.MessageNodeIO;
import bridgempp.data.User;
import bridgempp.message.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

/**
 * Regex Message Parser
 * 
 * Required Groups:
 * 	origin: Endpoint identifier
 *  user: User identifier
 *  message: Message identifier
 * Optional Groups:
 * 	group: Target group
 */
public class StringToMessageRegexNode extends MessageNodeIO<String, Message> {

	private Pattern pattern;
	private BridgeService service;
	private MessageFormat messageFormat;
	
	@Override
	protected void process(String input)
	{
		Matcher matcher = pattern.matcher(input);
		while(matcher.find())
		{
			Endpoint origin = DataManager.getOrNewEndpointForIdentifier(matcher.group("origin"), service);
			User sender = DataManager.getOrNewUserForIdentifier(matcher.group("user"), origin);
			String messageBody = matcher.group("message");
			Group group = null;
			try
			{
				group = GroupManager.findGroup(matcher.group("group"));
			} catch (IllegalArgumentException e)
			{}
			processResult(new Message(sender, origin, null, group, messageBody, messageFormat));
		}
	}

	public StringToMessageRegexNode(Pattern pattern, BridgeService service, MessageFormat messageFormat)
	{
		super();
		this.pattern = pattern;
		this.service = service;
		this.messageFormat = messageFormat;
	}

}
