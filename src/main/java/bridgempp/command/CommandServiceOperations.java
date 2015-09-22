package bridgempp.command;

import java.util.List;
import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.ServiceManager;
import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.command.wrapper.RequiredPermission;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;
import bridgempp.services.BridgeChat;
import bridgempp.services.ConsoleService;
import bridgempp.services.MailService;
import bridgempp.services.SkypeService;
import bridgempp.services.facebook.FacebookService;
import bridgempp.services.leagueoflegends.LeagueOfLegendsService;
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
	
	@CommandName("!loadleagueoflegendsservice: Load a League Of Legends Service")
	@CommandTrigger("!loadleagueoflegendsservice")
	@HelpTopic("Creates a League Of Legends Service. Requires host, port, domain, oldStyleSSL (boolean), Username, Password, Status message, Status Filter.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String loadLeagueOfLegendsService(String host, int port, String domain, boolean oldStyleSSL, String username, String password, String status, String filter)
	{
		LeagueOfLegendsService service = new LeagueOfLegendsService();
		service.configure(host, port, domain, oldStyleSSL, username, password, status, filter);
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
	
	@CommandName("!injectendpoint: Load a endpoint for given Service")
	@CommandTrigger("!injectendpoint")
	@HelpTopic("Load a new endpoint for a given Service. Requires SERVICE_ID and ENDPOINT_ID.")
	@RequiredPermission(Permission.INJECT_ENDPOINT)
	public static String injectEndpoint(int serviceID, String endpointID)
	{
		BridgeService service = ServiceManager.getServiceByServiceIdentifier(serviceID);
		if(service == null)
		{
			return "Service " + serviceID + "not found. Try obtaining a Service ID with !listservices";
		}
		ShadowManager.log(Level.WARNING, "Injecting Endpoint: " + service.toString() + " new Endpoint: " + endpointID);
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(endpointID, service);
		return "Injected endpoint: " + endpoint.toString(); 
	}
	
	@CommandName("!remotesendmessage: Send a message from a specified endpoint")
	@CommandTrigger("!remotesendmessage")
	@HelpTopic("Remotely send a message as if it were comming from the specified endpoint. Requires SERVICE_ID and ENDPOINT_ID and USER_ID and Message.")
	@RequiredPermission(Permission.REMOTE_SEND_MESSAGE)
	public static String remoteSendMessage(int serviceID, String endpointID, String userID, String message)
	{
		BridgeService service = ServiceManager.getServiceByServiceIdentifier(serviceID);
		if(service == null)
		{
			return "Service " + serviceID + "not found. Try obtaining a Service ID with !listservices";
		}
		ShadowManager.log(Level.WARNING, "Remote sending Message from: " + service.toString() + " endpoint: " + endpointID + " message: " + message);
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(endpointID, service);
		User user = DataManager.getOrNewUserForIdentifier(userID, endpoint);
		CommandInterpreter.processMessage(new Message(user, endpoint, message, MessageFormat.PLAIN_TEXT));
		return "Remotely sent message: " + message; 
	}
	
	@CommandName("!reloadservice: Reload a Service")
	@CommandTrigger("!reloadservice")
	@HelpTopic("Reload a Service by disconnecting and reconnecting it. Requires SERVICE_ID.")
	@RequiredPermission(Permission.ADD_REMOVE_SERVICE)
	public static String reloadService(int serviceID)
	{
		BridgeService service = ServiceManager.getServiceByServiceIdentifier(serviceID);
		if(service == null)
		{
			return "Service " + serviceID + "not found. Try obtaining a Service ID with !listservices";
		}
		ShadowManager.log(Level.WARNING, "Reloading Service: " + service.toString());
		service.disconnect();
		service.connect();
		return "Reloaded Service: " + service.toString(); 
	}
}
