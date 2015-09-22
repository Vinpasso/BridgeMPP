package bridgempp.services.teamspeak;

import bridgempp.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

public class TeamspeakService extends BridgeService
{

	private transient ServerQueryManager serverQueryManager;
	private String serverAddress;
	private int serverPort;
	private String queryPassword;
	private String queryUsername;
	private int virtualServerID;
	
	
	@Override
	public void connect()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(Message message)
	{
		serverQueryManager.sendMessage(message);
	}

	@Override
	public String getName()
	{
		return "Teamspeak";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return MessageFormat.PLAIN_TEXT_ONLY;
	}

	protected String getServerAddress()
	{
		return serverAddress;
	}

	protected int getServerPort()
	{
		return serverPort;
	}

	public String getQueryUsername()
	{
		return queryUsername;
	}
	
	public String getQueryPassword()
	{
		return queryPassword;
	}

	public int getVirtualServerID()
	{
		return virtualServerID;
	}
	
	
}
