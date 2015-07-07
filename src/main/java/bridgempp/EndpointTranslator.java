/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import org.apache.commons.configuration.ConfigurationException;

import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class EndpointTranslator {

    public static String getHumanReadableEndpoint(Endpoint endpoint) {
        if (ConfigurationManager.endpointConfiguration.containsKey("endpoints.id" + computeEndpointID(endpoint.getIdentifer()))) {
            return ConfigurationManager.endpointConfiguration.getString("endpoints.id" + computeEndpointID(endpoint.getIdentifer()));
        }
        return "";
    }

    public static synchronized void saveHumanReadableEndpoint(Endpoint endpoint, String endpointName) {
        ShadowManager.log(Level.INFO, "Setting alias of " + endpoint.toString(false) + " to " + endpointName);
    	try {
            ConfigurationManager.endpointConfiguration.setProperty("endpoints.id" + computeEndpointID(endpoint.getIdentifer()), endpointName);
            ConfigurationManager.endpointConfiguration.save();
        } catch (ConfigurationException ex) {
            ShadowManager.log(Level.SEVERE, "Error while adjusting XML Configuration with Human Readable Endpoint", ex);
        }
        ShadowManager.log(Level.INFO, "New alias of " + endpoint.toString(false) + " is " + endpointName);
    }

    public static synchronized void removeHumanReadableEndpoint(Endpoint endpoint) {
        ConfigurationManager.endpointConfiguration.clearProperty("endpoints.id" + computeEndpointID(endpoint.getIdentifer()));
    }

    public static int computeEndpointID(String string) {
        return string.hashCode();
    }
}
