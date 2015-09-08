/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import bridgempp.command.CommandInterpreter;
import bridgempp.state.EventManager;
import bridgempp.statistics.StatisticsManager;
import bridgempp.storage.PersistanceManager;
import bridgempp.util.LockdownLock;

/**
 *
 * @author Vinpasso
 */
public class BridgeMPP
{

	private static volatile boolean shutdownCommencing = false;
	private static LockdownLock lockdown = new LockdownLock();

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args)
	{
		lockdown();
		if (args.length != 0)
		{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].trim().equalsIgnoreCase("-stopTime"))
				{
					try
					{
						startExitSync(Long.parseLong(args[i + 1]));
					} catch (Exception e)
					{
						System.err.println("Syntax Error");
						System.exit(0);
					}
				}
			}
		}
		ShadowManager.log(Level.INFO, "Server startup commencing...");
		addShutdownHook();
		EventManager.loadCentralEventSubscribers();
		CommandInterpreter.loadCommands();
		ServiceManager.loadAllServices();
		StatisticsManager.loadStatistics();
		allClear();
		ShadowManager.log(Level.INFO, "Server Initialization completed");
	}

	private static void startExitSync(final long parseLong)
	{
		Thread timeThread = new Thread(new Runnable() {

			@Override
			public void run()
			{
				System.out.println("Exit Time set to: " + parseLong);
				try
				{
					Thread.sleep(parseLong);
				} catch (InterruptedException e)
				{
					ShadowManager.log(Level.WARNING, "Server Exit Sync interrupted. Shutting down BridgeMPP");
				}
				ShadowManager.log(Level.INFO, "Server will shut down due to scheduled restart");
				exit();
			}
		});
		timeThread.setName("Exit Time Timer Thread");
		timeThread.start();
	}

	public static String getPathLocation()
	{
		try
		{
			URL url = BridgeMPP.class.getProtectionDomain().getCodeSource().getLocation();
			String path = new File(url.toURI()).getPath();
			if (path.endsWith(".jar"))
			{
				path = path.substring(0, path.lastIndexOf("/"));
			}
			return path;
		} catch (URISyntaxException ex)
		{
			ShadowManager.log(Level.SEVERE, "Cannot find URI of Jar!", ex);
		}
		return null;
	}

	public static void exit()
	{
		if (shutdownCommencing)
		{
			return;
		}
		shutdownCommencing = true;
		ShadowManager.log(Level.INFO, "Server shutdown commencing...");
		lockdown();
		try
		{
			ServiceManager.unloadAllServices();
			StatisticsManager.saveStatistics();
			PersistanceManager.getPersistanceManager().shutdown();
		} catch (Exception e)
		{
			ShadowManager.log(Level.WARNING, "Clean server shutdown has failed. Will forcefully continue shutdown", e);
		}
		ShadowManager.log(Level.INFO, "Server shutdown completed");
		ShadowManager.log(Level.INFO, "Syncing System Exit (60 Seconds)");
		long syncTime = 0;
		for (Thread thread : Thread.getAllStackTraces().keySet())
		{
			if (syncTime > 60000)
			{
				break;
			}
			if (thread.equals(Thread.currentThread()))
			{
				continue;
			}
			if (thread.isAlive())
			{
				long startTime = System.currentTimeMillis();
				try
				{
					if (!thread.isDaemon() && !thread.getName().equalsIgnoreCase("DestroyJavaVM"))
					{
						ShadowManager.log(Level.INFO, "Waiting on Thread: " + thread.getName());
						thread.interrupt();
						thread.join(60000);
						ShadowManager.log(Level.INFO, "Thread has exited: " + thread.getName());
					}
				} catch (InterruptedException e)
				{
					ShadowManager.log(Level.SEVERE, "Waiting on Thread " + thread.getName()
							+ " for 60 seconds did not result in exit");
				}
				syncTime += System.currentTimeMillis() - startTime;
			}
		}
		ShadowManager.log(Level.INFO, "Killing Process");
		Runtime.getRuntime().halt(0);
	}

	public static void lockdown()
	{
		lockdown.lock();
	}

	public static void allClear()
	{
		lockdown.unlock();
	}

	public static void syncLockdown() throws InterruptedException
	{
		lockdown.syncLock();
	}

	private static void addShutdownHook()
	{
		ShadowManager.log(Level.INFO, "Registering OS System Shutdown Hook");
		Thread hook = new Thread(new Runnable() {

			@Override
			public void run()
			{
				ShadowManager.log(Level.WARNING, "System shutdown triggered by JVM-Shutdown");
				exit();
			}

		});
		hook.setName("OS Shutdown Hook Executor");
		Runtime.getRuntime().addShutdownHook(hook);
		ShadowManager.log(Level.INFO, "Registered OS System Shutdown Hook");
	}
}
