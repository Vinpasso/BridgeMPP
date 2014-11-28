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
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ShadowManager.log(Level.INFO, "Server startup commencing...");
        ConfigurationManager.initializeConfiguration();
        PermissionsManager.loadAccessKeys();
        ServiceManager.loadAllServices();
        GroupManager.loadAllGroups();
        ShadowManager.log(Level.INFO, "Server Initialization completed");
    }
    
    public static String getPathLocation()
    {
        try {
            URL url = BridgeMPP.class.getProtectionDomain().getCodeSource().getLocation();
            String path =  new File(url.toURI()).getPath();
            if(path.endsWith(".jar"))
            {
                path = path.substring(0, path.lastIndexOf("/"));
            }
            return path;
        } catch (URISyntaxException ex) {
            Logger.getLogger(BridgeMPP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static void exit()
    {
        ShadowManager.log(Level.INFO, "Server shutdown commencing...");
        PermissionsManager.saveAccessKeys();
        GroupManager.saveAllGroups();
        ServiceManager.unloadAllServices();
        ShadowManager.log(Level.INFO, "Server shutdown completed");
    }
}
