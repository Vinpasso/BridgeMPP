/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class CommandShadowOperations {

    static void cmdAddShadow(Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.ADD_REMOVE_SHADOW)) {
            ShadowManager.log(Level.WARNING, "Shadow has been subscribed by " + sender.toString());
            ShadowManager.shadowEndpoints.add(sender);
            sender.sendMessage("BridgeMPP: Your endpoint has been added to the list of Shadows");
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdListShadows(Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.LIST_SHADOW)) {
            sender.sendMessage("BridgeMPP: Listing shadows");
            for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++) {
                sender.sendMessage("BridgeMPP: Shadow: " + ShadowManager.shadowEndpoints.get(i).toString());
            }
            sender.sendMessage("BridgeMPP: Done listing shadows");
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdRemoveShadow(Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.ADD_REMOVE_SHADOW)) {
            ShadowManager.log(Level.WARNING, "Shadow has been removed by " + sender.toString());
            ShadowManager.shadowEndpoints.remove(sender);
            sender.sendMessage("BridgeMPP: Your endpoint has been removed from the list of Shadows");
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

}
