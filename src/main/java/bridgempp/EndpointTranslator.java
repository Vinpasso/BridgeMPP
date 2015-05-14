/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import org.apache.commons.configuration.ConfigurationException;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        try {
            ConfigurationManager.endpointConfiguration.setProperty("endpoints.id" + computeEndpointID(endpoint.getIdentifer()), endpointName);
            ConfigurationManager.endpointConfiguration.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(EndpointTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static synchronized void removeHumanReadableEndpoint(Endpoint endpoint) {
        ConfigurationManager.endpointConfiguration.clearProperty("endpoints.id" + computeEndpointID(endpoint.getIdentifer()));
    }

    public static int computeEndpointID(String string) {
        return string.hashCode();
    }
}
