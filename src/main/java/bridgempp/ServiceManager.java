/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.storage.PersistanceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
		Collection<BridgeService> serviceConfigurations = PersistanceManager.getPersistanceManager().getServiceConfigurations();
		Iterator<BridgeService> iterator = serviceConfigurations.iterator();
		while(iterator.hasNext())
		{
			BridgeService service = iterator.next();
			ShadowManager.log(Level.INFO, "Loading Service: " + service.getName());
			services.add(service);
			service.connect();
			ShadowManager.log(Level.INFO, "Loaded Service: " + service.getName());
		}
		ShadowManager.log(Level.INFO, "All Services loaded");

	}

	public static void unloadAllServices() {
		ShadowManager.log(Level.INFO, "Unloading all Services...");
		for (int i = 0; i < services.size(); i++) {
			try {
				ShadowManager.log(Level.INFO, "Unloading Service: " + services.get(i).getName());
				services.get(i).disconnect();
				ShadowManager.log(Level.INFO, "Unloaded Service: " + services.get(i).getName());
			} catch (Exception e) {
				ShadowManager.log(Level.INFO, "Unloading of Service " + services.get(i).getName() + " has failed", e);
			}
		}
		ShadowManager.log(Level.INFO, "Unloaded all Services...");
	}

	public static BridgeService getServiceByServiceIdentifier(String string)
	{
		Iterator<BridgeService> iterator = services.iterator();
		while(iterator.hasNext())
		{
			BridgeService service = iterator.next();
			if(service.getIdentifier().equalsIgnoreCase(string))
			{
				return service;
			}
		}
		return null;
	}
}
