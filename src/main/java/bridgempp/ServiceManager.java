/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.services.ConsoleService;
import bridgempp.services.MailService;
import bridgempp.services.SkypeService;
import bridgempp.services.WhatsappService;
import bridgempp.services.socket.SocketService;
import bridgempp.services.xmpp.XMPPService;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class ServiceManager {

	private static ArrayList<BridgeService> services;

	// Load Services from Config
	public static void loadAllServices() {
		ShadowManager.log(Level.INFO, "Loading all Services...");

		services = new ArrayList<>();
		int serviceDefinitionCount = ConfigurationManager.serviceConfiguration.getRootNode().getChild(0)
				.getChildrenCount();
		for (int i = 0; i < serviceDefinitionCount; i++) {
			String type = ConfigurationManager.serviceConfiguration.getString("services.service(" + i + ").type");
			String options = ConfigurationManager.serviceConfiguration.getString("services.service(" + i + ").options");
			BridgeService service;
			switch (type.toLowerCase()) {
			case "consoleservice":
				service = new ConsoleService();
				break;
			case "socketservice":
				service = new SocketService();
				break;
			case "mailservice":
				service = new MailService();
				break;
			case "xmppservice":
				service = new XMPPService();
				break;
			case "skypeservice":
				service = new SkypeService();
				break;
			case "whatsappservice":
				service = new WhatsappService();
				break;
			default:
				throw new UnsupportedOperationException("Incorrect Servicetype in Service Declaration: " + type);
			}
			services.add(service);
			service.connect(options);
		}
		ShadowManager.log(Level.INFO, "All Services loaded");

	}

	public static void unloadAllServices() {
		for (int i = 0; i < services.size(); i++) {
			try {
				services.get(i).disconnect();
			} catch (Exception e) {
				ShadowManager.log(Level.INFO, "Unloading of Service " + services.get(i).getName() + " has failed", e);
			}
		}
	}

	public static BridgeService getService(String name) {
		for (int i = 0; i < services.size(); i++) {
			if (services.get(i).getName().equals(name)) {
				return services.get(i);
			}
		}
		return null;
	}
}
