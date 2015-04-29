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
import java.util.logging.Logger;

/**
 *
 * @author Vinpasso
 */
public class BridgeMPP {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		if(args.length != 0)
		{
			for(int i = 0; i < args.length; i++)
			{
				if(args[i].trim().equalsIgnoreCase("-stopTime"))
				{
					try
					{
						startExitSync(Long.parseLong(args[i + 1]));
					}
					catch(Exception e)
					{
						System.err.println("Syntax Error");
						System.exit(0);
					}
				}
			}
		}
		ShadowManager.log(Level.INFO, "Server startup commencing...");
		ConfigurationManager.initializeConfiguration();
		PermissionsManager.loadAccessKeys();
		ServiceManager.loadAllServices();
		GroupManager.loadAllGroups();
		ShadowManager.log(Level.INFO, "Server Initialization completed");
	}

	private static void startExitSync(final long parseLong) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("Exit Time set to: " + parseLong);
				try {
					Thread.sleep(parseLong);
				} catch (InterruptedException e) {
					System.err.println("Exit Sync Interrupted! Emergency Shutdown!");
				}
				exit();
			}
		}).start();
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
			Logger.getLogger(BridgeMPP.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return null;
	}

	public static void exit()
    {
        ShadowManager.log(Level.INFO, "Server shutdown commencing...");
        try
        {
        PermissionsManager.saveAccessKeys();
        GroupManager.saveAllGroups();
        ServiceManager.unloadAllServices();
        }
        catch(Exception e)
        {
        	ShadowManager.log(Level.WARNING, "Clean server shutdown has failed. Will forcefully continue shutdown", e);
        }
        ShadowManager.log(Level.INFO, "Server shutdown completed");
        ShadowManager.log(Level.INFO, "Syncing System Exit (60 Seconds)");
        long syncTime = 0;
        	for(Thread thread : Thread.getAllStackTraces().keySet())
        	{
        		if(syncTime > 60000)
        		{
        			break;
        		}
        		if(thread.equals(Thread.currentThread()))
        		{
        			continue;
        		}
        		if(thread.isAlive())
        		{
        			long startTime = System.currentTimeMillis();
        			try {
        				thread.interrupt();
						thread.join(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
        			syncTime += System.currentTimeMillis() - startTime;
        		}
        	}
        ShadowManager.log(Level.INFO, "Killing Process");
        System.exit(0);
    }
}
