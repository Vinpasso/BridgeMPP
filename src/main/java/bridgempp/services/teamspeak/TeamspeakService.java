package bridgempp.services.teamspeak;

import java.util.logging.Level;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;

import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

public class TeamspeakService extends BridgeService
{
	private String host;
	
	private int port;
	
	private String username;
	
	private String password;
	
	private int virtualServerID;

	private transient TS3Config teamspeakConfig;
	private transient TS3Query teamspeak;
	private transient TS3Api teamspeakAPI;	
	
	@Override
	public void connect() throws Exception
	{
		teamspeakConfig = new TS3Config();
		teamspeakConfig.setHost(host);
		teamspeakConfig.setQueryPort(port);
		teamspeakConfig.setDebugLevel(Level.ALL);
		
		teamspeak = new TS3Query(teamspeakConfig);
		teamspeak.connect();
		
		teamspeakAPI = teamspeak.getApi();
		teamspeakAPI.login(username, password);
		
		teamspeakAPI.selectVirtualServerById(virtualServerID);
		teamspeakAPI.setNickname("BridgeMPP");
		
		teamspeakAPI.registerEvent(TS3EventType.TEXT_CHANNEL, teamspeakAPI.getChannelByNameExact("tumspam", true).getId());
		
		teamspeakAPI.addTS3Listeners(new TS3EventAdapter() {
			public void onTextMessage(TextMessageEvent e)
			{
				Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(e.get("channel"), TeamspeakService.this);
				User user = DataManager.getOrNewUserForIdentifier(e.getInvokerUniqueId(), endpoint);
				Message message = new Message(user, endpoint, e.getMessage(), MessageFormat.PLAIN_TEXT);
				receiveMessage(message);
			}
		});
	}

	@Override
	public void disconnect() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void sendMessage(Message message)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPersistent()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
