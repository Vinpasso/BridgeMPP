/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;

/**
 *
 * @author Vinpasso
 */
public class EndpointTranslator {

    //MUST REMOVE @ SIGNS AND REPLACE WITH MINUS
    public static String getHumanReadableEndpoint(Endpoint endpoint) {
        String identifier = endpoint.getIdentifer();
        if (ConfigurationManager.endpointConfiguration.containsKey("endpoints.id" + computeEndpointID(endpoint.getIdentifer()))) {
            return ConfigurationManager.endpointConfiguration.getString("endpoints.id" + computeEndpointID(endpoint.getIdentifer()));
        }
        return "";
//        int endpointTranslations = ConfigurationManager.endpointConfiguration.getRootNode().getChild(0).getChildrenCount();
//        for(int i = 0; i < endpointTranslations; i++)
//        {
//            String service = ConfigurationManager.endpointConfiguration.getString("endpoints.endpoint(" + i + ").service");
//            String target = ConfigurationManager.endpointConfiguration.getString("endpoints.endpoint(" + i + ").target");
//            String extra = ConfigurationManager.endpointConfiguration.getString("endpoints.endpoint(" + i + ").extra");
//            if(endpoint.getService().getName().equals(service) && endpoint.getTarget().equals(target) && endpoint.getExtra().equals(extra))
//            {
//                return ConfigurationManager.endpointConfiguration.getString("endpoints.endpoint(" + i + ").name");
//            }
//        }
    }

        //MUST REMOVE @ SIGNS AND REPLACE WITH MINUS
    public static void saveHumanReadableEndpoint(Endpoint endpoint, String endpointName) {
        try {
            ConfigurationManager.endpointConfiguration.setProperty("endpoints.id" + computeEndpointID(endpoint.getIdentifer()), endpointName);
            ConfigurationManager.endpointConfiguration.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(EndpointTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void removeHumanReadableEndpoint(Endpoint endpoint) {
        ConfigurationManager.endpointConfiguration.clearProperty("endpoints.id" + computeEndpointID(endpoint.getIdentifer()));
    }

    public static int computeEndpointID(String string) {
        return string.hashCode();
    }
}
