/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.PermissionsManager;
import bridgempp.ShadowManager;

/**
 *
 * @author Vinpasso
 */
public class CommandShadowOperations {

    static void cmdAddShadow(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.ADD_REMOVE_SHADOW)) {
            ShadowManager.log(Level.WARNING, "Shadow has been subscribed by " + message.getOrigin().toString());
            ShadowManager.shadowEndpoints.add(message.getOrigin());
            message.getOrigin().sendOperatorMessage("Your endpoint has been added to the list of Shadows");
        } else {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }

    static void cmdListShadows(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.LIST_SHADOW)) {
            message.getOrigin().sendOperatorMessage("Listing shadows");
            for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++) {
                message.getOrigin().sendOperatorMessage("Shadow: " + ShadowManager.shadowEndpoints.get(i).toString());
            }
            message.getOrigin().sendOperatorMessage("Done listing shadows");
        } else {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }

    static void cmdRemoveShadow(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.ADD_REMOVE_SHADOW)) {
            ShadowManager.log(Level.WARNING, "Shadow has been removed by " + message.getOrigin().toString());
            ShadowManager.shadowEndpoints.remove(message.getOrigin());
            message.getOrigin().sendOperatorMessage("Your endpoint has been removed from the list of Shadows");
        } else {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }

}
