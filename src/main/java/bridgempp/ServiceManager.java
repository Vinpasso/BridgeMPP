/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.ServiceConfiguration;
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
		Collection<ServiceConfiguration> serviceConfigurations = PersistanceManager.getPersistanceManager().getServiceConfigurations();
		Iterator<ServiceConfiguration> iterator = serviceConfigurations.iterator();
		while(iterator.hasNext())
		{
			ServiceConfiguration serviceConfiguration = iterator.next();
			BridgeService service = serviceConfiguration.createService();
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

	public static BridgeService getService(String name) {
		for (int i = 0; i < services.size(); i++) {
			if (services.get(i).getName().equals(name)) {
				return services.get(i);
			}
		}
		return null;
	}
}
