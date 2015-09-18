package bridgempp.services.leagueoflegends;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.data.Endpoint;
import bridgempp.services.xmpp.XMPPService;

@Entity(name = "LEAGUE_OF_LEGENDS_SERVICE")
@DiscriminatorValue(value = "LEAGUE_OF_LEGENDS_SERVICE")
public class LeagueOfLegendsService extends XMPPService
{

	public void connect()
	{
		super.connect();
		connection.getRoster().addRosterListener(new LeagueOfLegendsStatusListener(this));
	}
	
	public void importFromEndpoint(Endpoint endpoint)
	{
		//Don't attempt to recover chats
	}
	
	public String getName()
	{
		return "League_of_Legends";
	}
		
}
