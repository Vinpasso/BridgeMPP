/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.DataManager;
import bridgempp.data.processing.Schedule;
import bridgempp.log.Log;
import bridgempp.service.BridgeService;
import bridgempp.service.ServiceStatus;
import bridgempp.services.ConsoleService;
import bridgempp.state.Event;
import bridgempp.state.EventManager;
import bridgempp.storage.PersistanceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
		Log.log(Level.INFO, "Connecting all Services...");

		services = new ArrayList<>();
		Collection<BridgeService> serviceConfigurations = DataManager.getServiceConfigurations();
		Iterator<BridgeService> iterator = serviceConfigurations.iterator();
		while (iterator.hasNext())
		{
			BridgeService service = iterator.next();
			Log.log(Level.INFO, "Connecting Service: " + service.getName());
			loadService(service);
			Log.log(Level.INFO, "Connected Service: " + service.getName());
		}
		if (serviceConfigurations.isEmpty())
		{
			setupFirstRun();
		}
		Log.log(Level.INFO, "All Services connected");

	}

	private static void setupFirstRun()
	{
		Log.log(Level.INFO, "No Services loaded. Automatically loading Console-Service");
		loadService(new ConsoleService());
		Log.log(Level.INFO, "Loaded Service: Console Service. Due to automatic loading.");
		Log.log(Level.INFO, "Creating Server Key. Due to automatic loading");
		String key = PermissionsManager.generateKey(Integer.MAX_VALUE, true);
		Log.log(Level.INFO, "Created Server Key " + key + ". Due to automatic loading");
	}

	public static void loadService(BridgeService service)
	{
		services.add(service);
		if(service.getStatus() == ServiceStatus.DISABLED)
		{
			Log.log(Level.WARNING, "Service: " + service.toString() + " is disabled");
			return;
		}
		connectService(service);
		PersistanceManager.getPersistanceManager().updateState(service);
		EventManager.fireEvent(Event.SERVICE_LOADED, service);
	}
	
	public static void onServiceError(BridgeService service, String message, Exception error)
	{
		Log.log(Level.INFO, "Received error in service " + service.getName() + ": " + message, error);
		try
		{
			service.disconnect();
		} catch (Exception e)
		{
			Log.log(Level.WARNING, "Received additional error trying to disconnect service " + service.getName(), e);
		}
		EventManager.fireEvent(Event.SERVICE_DISCONNECTED, service);
		Schedule.scheduleOnce(() -> {
			Log.log(Level.INFO, "Restarting previously crashed service: " + service.getName());
			try
			{
				service.connect();
			}
			catch(Exception e)
			{
				Log.log(Level.SEVERE, "Disabling Service: " + service.getName() + " due to multiple failures.");
				service.setStatus(ServiceStatus.DISABLED);
			}
		}, 60, TimeUnit.SECONDS);
	}

	private static void connectService(BridgeService service)
	{
		try
		{
			service.connect();
			service.setStatus(ServiceStatus.ONLINE);
			EventManager.fireEvent(Event.SERVICE_CONNECTED, service);
		} catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not load Service: " + service.toString(), e);
		}
	}

	public static void disconnectAllServices()
	{
		Log.log(Level.INFO, "Disconnecting all Services...");
		for (int i = 0; i < services.size(); i++)
		{
			try
			{
				Log.log(Level.INFO, "Disconnecting Service: " + services.get(i).getName());
				if(services.get(i).getStatus() == ServiceStatus.DISABLED)
				{
					Log.log(Level.WARNING, "Service: " + services.get(i).toString() + " is disabled");
					continue;
				}
				disconnectService(services.get(i));
				Log.log(Level.INFO, "Disconnected Service: " + services.get(i).getName());
			} catch (Exception e)
			{
				Log.log(Level.INFO, "Shutdown of Service " + services.get(i).getName() + " has failed", e);
			}
		}
		Log.log(Level.INFO, "Disconnected all Services...");
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
		Log.log(Level.WARNING, "Removing Service: " + service.toString());
		services.remove(service);
		PersistanceManager.getPersistanceManager().removeState(service);
		disconnectService(service);
	}

	private static void disconnectService(BridgeService service)
	{
		try
		{
			service.disconnect();
		} catch (Exception e)
		{
			Log.log(Level.SEVERE, "Encountered error while disconnecting service: " + service.toString(), e);
		}
		service.setStatus(ServiceStatus.OFFLINE);
		EventManager.fireEvent(Event.SERVICE_DISCONNECTED, service);
	}
}
