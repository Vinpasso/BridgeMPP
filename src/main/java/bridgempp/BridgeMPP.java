/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import bridgempp.command.CommandInterpreter;
import bridgempp.data.processing.Schedule;
import bridgempp.state.EventManager;
import bridgempp.state.EventManager.Event;
import bridgempp.statistics.StatisticsManager;
import bridgempp.storage.PersistanceManager;
import bridgempp.util.JUnitTestTest;

/**
 *
 * @author Vinpasso
 */
public class BridgeMPP {

	private static volatile boolean shutdownCommencing = false;
	private static volatile boolean scheduledShutdown = false;
	private static ReentrantReadWriteLock lockdown = new ReentrantReadWriteLock();

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		writeLock();
		try {
			if (args.length != 0) {
				for (int i = 0; i < args.length; i++) {
					if (args[i].trim().equalsIgnoreCase("-stopTime")) {
						try {
							startExitSync(Long.parseLong(args[i + 1]));
						} catch (Exception e) {
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
			ServiceManager.connectAllServices();
			StatisticsManager.loadStatistics();
			EventManager.fireEvent(Event.BRIDGEMPP_STARTUP, null);
			ShadowManager.log(Level.INFO, "Waiting for all units to catch up before unlocking");
			Thread.sleep(5000);
		} catch (Exception e) {
			ShadowManager.log(Level.SEVERE, "Fatal Error: ", e);
			exit();
		}
		writeUnlock();
		ShadowManager.log(Level.INFO, "Server Initialization completed");
	}

	private static void startExitSync(final long parseLong) {
		Schedule.scheduleOnce(() -> {
			executeScheduledShutdown();
		}, parseLong, TimeUnit.MILLISECONDS);
		ShadowManager.log(Level.INFO, "Scheduled shutdown to occur in "
				+ parseLong + " milliseconds");
	}

	public static String getPathLocation() {
		try {
			URL url = BridgeMPP.class.getProtectionDomain().getCodeSource()
					.getLocation();
			String path = new File(url.toURI()).getPath();
			if (path.endsWith(".jar")) {
				path = path.substring(0, path.lastIndexOf("/"));
			}
			return path;
		} catch (URISyntaxException ex) {
			ShadowManager.log(Level.SEVERE, "Cannot find URI of Jar!", ex);
		}
		return null;
	}

	public static void exit() {
		if(Schedule.isShutdown())
		{
			return;
		}
		Schedule.schedule(() -> executeShutdown());
		ShadowManager.log(Level.INFO, "Scheduled a system shutdown");
	}
	
	public static void executeScheduledShutdown()
	{
		ShadowManager.log(Level.INFO, "Server will shut down due to scheduled restart");
		scheduledShutdown = true;
		exit();
	}
		
	private static void executeShutdown()
	{
		if (shutdownCommencing || JUnitTestTest.isJUnitTest()) {
			return;
		}
		shutdownCommencing = true;
		ShadowManager.log(Level.INFO, "Server shutdown commencing...");
		writeLock();
		EventManager.fireEvent(Event.BRIDGEMPP_SHUTDOWN, null);
		try {
			ServiceManager.disconnectAllServices();
			StatisticsManager.saveStatistics();
			PersistanceManager.getPersistanceManager().shutdown();
			Schedule.shutdownAsynchronous();
		} catch (Exception e) {
			ShadowManager
					.log(Level.WARNING,
							"Clean server shutdown has failed. Will forcefully continue shutdown",
							e);
		}
		writeUnlock();
		ShadowManager.log(Level.INFO, "Server shutdown completed");
		ShadowManager.log(Level.INFO, "Syncing System Exit (60 Seconds)");
		long syncTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - syncTime < 60000)
		{
			Thread.getAllStackTraces().forEach((t, st) -> {
				if(t.isDaemon() || t.equals(Thread.currentThread()) || !t.isAlive())
				{
					return;
				}
				ShadowManager.log(Level.INFO, "Active thread: " + t.getName());
			});
			Thread.getAllStackTraces().forEach((t, st) -> {
				if(isNonRelevantThread(t))
				{
					return;
				}
				ShadowManager.log(Level.INFO, "Waiting for: " + t.getName());
				try
				{
					t.interrupt();
					t.join(10000);
				} catch (Exception e)
				{
					ShadowManager.log(Level.WARNING, "Interrupt while waiting for thread join", e);
				}
				if(t.isAlive())
				{
					ShadowManager.log(Level.INFO, t.getName() + " is still alive");
				}
				else
				{
					ShadowManager.log(Level.INFO, t.getName() + " has exited");
				}
			});
			if(Thread.getAllStackTraces().entrySet().stream().allMatch(e -> isNonRelevantThread(e.getKey())))
			{
				ShadowManager.log(Level.INFO, "All threads have exited successfully");
				break;
			}
		}
		ShadowManager.log(Level.INFO, "Continuing shutdown with " + Thread.activeCount() + " alive threads.");
		ShadowManager
				.log(Level.INFO, "Killing Process. This was"
						+ ((scheduledShutdown) ? " " : " NOT ")
						+ "a scheduled restart");
		Runtime.getRuntime().exit((scheduledShutdown) ? 0 : -1);
	}

	private static boolean isNonRelevantThread(Thread t)
	{
		return t.isDaemon() || t.equals(Thread.currentThread()) || t.getName().equals("DestroyJavaVM") || !t.isAlive();
	}

	public static void writeLock() {
		lockdown.writeLock().lock();
	}

	public static void writeUnlock() {
		lockdown.writeLock().unlock();
	}

	public static void readLock() {
		lockdown.readLock().lock();
	}

	public static void readUnlock() {
		lockdown.readLock().unlock();
	}

	private static void addShutdownHook() {
		ShadowManager.log(Level.INFO, "Registering OS System Shutdown Hook");
		Thread hook = new Thread(new Runnable() {

			@Override
			public void run() {
				ShadowManager.log(Level.WARNING,
						"System shutdown triggered by JVM-Shutdown");
				exit();
			}

		});
		hook.setName("OS Shutdown Hook Executor");
		Runtime.getRuntime().addShutdownHook(hook);
		ShadowManager.log(Level.INFO, "Registered OS System Shutdown Hook");
	}
}
