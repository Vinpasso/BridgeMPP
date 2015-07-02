/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class ConfigurationManager {

    public static XMLConfiguration serviceConfiguration;
    public static XMLConfiguration groupConfiguration;
    public static XMLConfiguration endpointConfiguration;
    public static XMLConfiguration permissionConfiguration;
    private static XMLConfiguration randomDataStore;

    public static void initializeConfiguration() {
        try {
            ShadowManager.log(Level.INFO, "Configuration files are being loaded...");
            serviceConfiguration = new XMLConfiguration(BridgeMPP.getPathLocation() + "/config.xml");
            serviceConfiguration.setEncoding("UTF-8");
            groupConfiguration = new XMLConfiguration(BridgeMPP.getPathLocation() + "/groups.xml");
            groupConfiguration.setEncoding("UTF-8");
            endpointConfiguration = new XMLConfiguration(BridgeMPP.getPathLocation() + "/endpoints.xml");
            endpointConfiguration.setEncoding("UTF-8");
            permissionConfiguration = new XMLConfiguration(BridgeMPP.getPathLocation() + "/keys.xml");
            permissionConfiguration.setEncoding("UTF-8");
            randomDataStore = new XMLConfiguration(BridgeMPP.getPathLocation() + "/datastore.xml");
            randomDataStore.setEncoding("UTF-8");
            ShadowManager.log(Level.INFO, "Configuration files have been loaded");
        } catch (ConfigurationException ex) {
            ShadowManager.log(Level.SEVERE, "Error while initializing XML Configuration", ex);
        }
    }

    public static String getGroupIdentifier(String groupName) {
        List<Object> groups = groupConfiguration.getList("groups");
        for (int i = 0; i < groups.size(); i++) {
            if (groupConfiguration.getString("groups.group(" + i + ").name").equalsIgnoreCase(groupName)) {
                return "groups.group(" + i + ")";
            }
        }
        return "";
    }
    
    public static String getServiceConfigurationIdentifier(String serviceIdentifier)
    {
		int serviceDefinitionCount = getServiceCount();
    	for(int i = 0; i < serviceDefinitionCount; i++)
    	{
    		if(serviceConfiguration.getString("services.service(" + i + ").type").equalsIgnoreCase(serviceIdentifier))
    		{
    			return "services.service(" + i + ")";
    		}
    	}
    	return null;
    }

	public static int getServiceCount() {
		return ConfigurationManager.serviceConfiguration.getRootNode().getChild(0)
				.getChildrenCount();
	}
	
	public static void storeData(BridgeService service, String key, Object data)
	{
		randomDataStore.setProperty(service.getName() + "." + key, data.toString());
		try {
			randomDataStore.save();
		} catch (ConfigurationException e) {
            ShadowManager.log(Level.SEVERE, "Error while saving XML Configuration random data store", e);
		}
	}
	
	public static Object loadData(BridgeService service, String key)
	{
		return randomDataStore.getProperty(service.getName() + "." + key);
	}
}
