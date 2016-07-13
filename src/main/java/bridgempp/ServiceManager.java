/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.DataManager;
import bridgempp.service.BridgeService;
import bridgempp.services.ConsoleService;
import bridgempp.state.EventManager;
import bridgempp.state.EventManager.Event;
import bridgempp.storage.PersistanceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class ServiceManager
{

	private static ArrayList<BridgeService> services;

	// Load Services from Config
	public static void connectAllServices()
	{
		ShadowManager.log(Level.INFO, "Connecting all Services...");

		services = new ArrayList<>();
		Collection<BridgeService> serviceConfigurations = PersistanceManager.getPersistanceManager().getServiceConfigurations();
		Iterator<BridgeService> iterator = serviceConfigurations.iterator();
		while (iterator.hasNext())
		{
			BridgeService service = iterator.next();
			ShadowManager.log(Level.INFO, "Connecting Service: " + service.getName());
			loadService(service);
			ShadowManager.log(Level.INFO, "Connected Service: " + service.getName());
		}
		if (serviceConfigurations.isEmpty())
		{
			setupFirstRun();
		}
		ShadowManager.log(Level.INFO, "All Services connected");

	}

	private static void setupFirstRun()
	{
		ShadowManager.log(Level.INFO, "No Services loaded. Automatically loading Console-Service");
		loadService(new ConsoleService());
		ShadowManager.log(Level.INFO, "Loaded Service: Console Service. Due to automatic loading.");
		ShadowManager.log(Level.INFO, "Creating Server Key. Due to automatic loading");
		String key = PermissionsManager.generateKey(Integer.MAX_VALUE, true);
		ShadowManager.log(Level.INFO, "Created Server Key " + key + ". Due to automatic loading");
	}

	public static void loadService(BridgeService service)
	{
		services.add(service);
		if(!service.isEnabled())
		{
			ShadowManager.log(Level.WARNING, "Service: " + service.toString() + " is Disabled");
			return;
		}
		connectService(service);
		PersistanceManager.getPersistanceManager().updateState(service);
		EventManager.fireEvent(Event.SERVICE_LOADED, service);
	}

	private static void connectService(BridgeService service)
	{
		try
		{
			service.connect();
			EventManager.fireEvent(Event.SERVICE_CONNECTED, service);
		} catch (Exception e)
		{
			ShadowManager.log(Level.SEVERE, "Could not load Service: " + service.toString());
			ShadowManager.fatal(e);
		}
	}

	public static void disconnectAllServices()
	{
		ShadowManager.log(Level.INFO, "Disconnecting all Services...");
		for (int i = 0; i < services.size(); i++)
		{
			try
			{
				ShadowManager.log(Level.INFO, "Disconnecting Service: " + services.get(i).getName());
				if(!services.get(i).isEnabled())
				{
					ShadowManager.log(Level.WARNING, "Service: " + services.get(i).toString() + " is Disabled");
					continue;
				}
				disconnectService(services.get(i));
				ShadowManager.log(Level.INFO, "Disconnected Service: " + services.get(i).getName());
			} catch (Exception e)
			{
				ShadowManager.log(Level.INFO, "Shutdown of Service " + services.get(i).getName() + " has failed", e);
			}
		}
		ShadowManager.log(Level.INFO, "Disconnected all Services...");
	}

	public static BridgeService getServiceByServiceIdentifier(int serviceIdentifierId)
	{
		return DataManager.getFromPrimaryKey(BridgeService.class, serviceIdentifierId);
	}

	public static List<BridgeService> listServices()
	{
		return Collections.unmodifiableList(services);
	}

	public static void removeService(int serviceID)
	{
		BridgeService service = getServiceByServiceIdentifier(serviceID);
		ShadowManager.log(Level.WARNING, "Removing Service: " + service.toString());
		services.remove(service);
		PersistanceManager.getPersistanceManager().removeState(service);
		disconnectService(service);
	}

	private static void disconnectService(BridgeService service)
	{
		try
		{
			service.disconnect();
			EventManager.fireEvent(Event.SERVICE_DISCONNECTED, service);
		} catch (Exception e)
		{
			ShadowManager.log(Level.SEVERE, "Could not disconnect Service: " + service.toString());
			ShadowManager.fatal(e);

		}
	}
}
