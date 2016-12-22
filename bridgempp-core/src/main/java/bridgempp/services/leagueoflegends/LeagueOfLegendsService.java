package bridgempp.services.leagueoflegends;


import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jivesoftware.smack.roster.Roster;

import bridgempp.data.Endpoint;
import bridgempp.services.xmpp.XMPPService;

@Entity(name = "LEAGUE_OF_LEGENDS_SERVICE")
@DiscriminatorValue(value = "LEAGUE_OF_LEGENDS_SERVICE")
public class LeagueOfLegendsService extends XMPPService
{

	@Column(name = "Status_Filter", length = 255, nullable = false)
	private String statusFilter;
	
	public void connect() throws Exception
	{
		super.connect();
		Roster.getInstanceFor(connection).addRosterListener(new LeagueOfLegendsStatusListener(this, statusFilter));
	}
	
	public void importFromEndpoint(Endpoint endpoint)
	{
		//Don't attempt to recover chats
	}
	
	public void configure(String host, int port, String domain, boolean oldStyleSSL, String username, String password, String statusMessage, String statusFilter)
	{
		super.configure(host, port, domain, oldStyleSSL, username, password, statusMessage);
		this.statusFilter = statusFilter;
	}
	
	public String getName()
	{
		return "League_of_Legends";
	}
		
}
