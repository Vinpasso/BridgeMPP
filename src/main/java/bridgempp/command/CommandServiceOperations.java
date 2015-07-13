package bridgempp.command;

import java.util.List;

import bridgempp.BridgeService;
import bridgempp.PermissionsManager.Permission;
import bridgempp.ServiceManager;
import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.command.wrapper.RequiredPermission;
import bridgempp.services.BridgeChat;
import bridgempp.services.ConsoleService;
import bridgempp.services.MailService;
import bridgempp.services.SkypeService;
import bridgempp.services.facebook.FacebookService;
import bridgempp.services.socket.SocketService;
import bridgempp.services.whatsapp.WhatsappService;
import bridgempp.services.xmpp.XMPPService;

public class CommandServiceOperations
{
	@CommandName("!listservices: List running services")
	@CommandTrigger("!listservices")
	@HelpTopic("Lists all the running Services on the Server")
	@RequiredPermission(Permission.LIST_SERVICES)
	public static String listServices()
	{
		String listing = "Running services on BridgeMPP:\n";
		List<BridgeService> services = ServiceManager.listServices();
		for(BridgeService service : services)
		{
			listing += service.toString() + "\n";
		}
		return listing.trim() + "\nListed "+ services.size() + " services";
	}
	
	@CommandName("!unloadservice: Remove running service")
	@CommandTrigger("!unloadservice")
	@HelpTopic("Shuts down a running service and disconnects it and it's users with specified Service ID")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String removeService(int serviceID)
	{
		ServiceManager.unloadService(serviceID);
		return "Removed service."; 
	}
	
	@CommandName("!loadconsoleservice: Load a Console Service")
	@CommandTrigger("!loadconsoleservice")
	@HelpTopic("Creates a Console Service. Requires no arguments.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadConsoleService()
	{
		ConsoleService service = new ConsoleService();
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}

	@CommandName("!loadwhatsappservice: Load a Whatsapp Service")
	@CommandTrigger("!loadwhatsappservice")
	@HelpTopic("Creates a Whatsapp Service. Requires phone and Base64 password.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadWhatsappService(String phone, String password)
	{
		WhatsappService service = new WhatsappService();
		service.configure(phone, password);
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}
	
	@CommandName("!loadxmppservice: Load a XMPP Service")
	@CommandTrigger("!loadxmppservice")
	@HelpTopic("Creates a XMPP Service. Requires host, port, domain, oldStyleSSL (boolean), Username, Password, Status message.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadXMPPService(String host, int port, String domain, boolean oldStyleSSL, String username, String password, String status)
	{
		XMPPService service = new XMPPService();
		service.configure(host, port, domain, oldStyleSSL, username, password, status);
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}
	
	@CommandName("!loadsocketservice: Load a Socket Service")
	@CommandTrigger("!loadsocketservice")
	@HelpTopic("Creates a Socket Service. Requires listen address and port.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadSocketService(String listenAddress, int port)
	{
		SocketService service = new SocketService();
		service.configure(listenAddress, port);
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}
	
	@CommandName("!loadmailservice: Load a Mail Service")
	@CommandTrigger("!loadmailservice")
	@HelpTopic("Creates a Mail Service. Requires IMAP Host, IMAP Port, Username, Password, SMTP Host, SMTP Port.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadMailService(String imapHost, int imapPort, String username, String password, String smtpHost, int smtpPort)
	{
		MailService service = new MailService();
		service.configure(imapHost, imapPort, username, password, smtpHost, smtpPort);
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}
	
	@CommandName("!loadskypeservice: Load a Skype Service")
	@CommandTrigger("!loadskypeservice")
	@HelpTopic("Creates a Skype Service. Requires no parameters.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadSkypeService()
	{
		SkypeService service = new SkypeService();
		service.configure();
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}

	@CommandName("!loadbridgechatservice: Load a BridgeChat Service")
	@CommandTrigger("!loadbridgechatservice")
	@HelpTopic("Creates a BridgeChat Service. Requires hostname and port number.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadBridgeChatService(String host, int port)
	{
		BridgeChat service = new BridgeChat();
		service.configure(host, port);
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}
	
	@CommandName("!loadfacebookservice: Load a Facebook Service")
	@CommandTrigger("!loadfacebookservice")
	@HelpTopic("Creates a Facebook Service. Requires APP_ID, API_KEY, ACCESS_TOKEN.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadFacebookService(String appid, String apikey, String accesstoken)
	{
		FacebookService service = new FacebookService();
		service.configure(appid, apikey, accesstoken);
		ServiceManager.loadService(service);
		return "Loaded service: " + service.toString(); 
	}
}
