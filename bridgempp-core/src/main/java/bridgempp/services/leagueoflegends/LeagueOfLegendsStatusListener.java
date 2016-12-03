package bridgempp.services.leagueoflegends;

import java.util.Collection;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Presence;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.services.xmpp.XMPPStatusListener;

public class LeagueOfLegendsStatusListener extends XMPPStatusListener
{

	private String acceptedStatus;
	
	public LeagueOfLegendsStatusListener(LeagueOfLegendsService service, String acceptedStatus)
	{
		super(service);
		this.acceptedStatus = acceptedStatus;
	}
	
	@Override
	public void entriesAdded(Collection<String> addresses)
	{
		super.entriesAdded(addresses);
		try
		{
			service.sendPresenceUpdate();
		} catch (NotConnectedException e)
		{
			ShadowManager.log(Level.WARNING, "XMPP not connected.", e);
			service.disconnect();
		}
	}

	@Override
	public void presenceChanged(Presence presence)
	{
		Endpoint endpoint = service.getXMPPPresenceEndpoint();
		User user = DataManager.getOrNewUserForIdentifier(presence.getFrom(), endpoint);
		String presenceNotification = presence.getStatus();
		if(presenceNotification == null)
		{
			return;
		}
		Matcher matcher = Pattern.compile("<gameStatus>(.*?)<\\/gameStatus>").matcher(presenceNotification);
		String message = "";
		if(matcher.find())
		{
			if(!acceptedStatus.contains(matcher.group(1)))
			{
				ShadowManager.log(Level.INFO, "Ignored Status update: " + matcher.group(1) + " from " + user.toString());
				return;
			}
			switch(matcher.group(1))
			{
				case "outOfGame":
					message = "Browsing Main Menu";
					break;
				case "hostingNormalGame":
					message = "Hosting a Normal Game";
					break;
				case "hostingCoopVsAIGame":
					message = "Hosting a Co-op vs. AI Game";
					break;
				case "hostingRankedGame":
					message = "Hosting a Ranked Game";
					break;
				case "inQueue":
					message = "Is now in Queue";
					break;
				case "championSelect":
					message = "Is selecting their Champion";
					break;
				case "inGame":
					message = "Is now in Game";
					break;
				default:
					message = "Unknown Status: " + matcher.group(1);
			}
			Message bridgeMessage = new MessageBuilder(user, endpoint).addPlainTextBody(message).build();
			service.receiveMessage(bridgeMessage);
		}
		else
		{
			ShadowManager.log(Level.WARNING, "Received presence update without Game Status tag: " + presence.toString());
		}
	}
}
