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
import bridgempp.PermissionsManager.Permission;

/**
 *
 * @author Vinpasso
 */
public class CommandShadowOperations {

    static void cmdAddShadow(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.ADD_REMOVE_SHADOW)) {
            ShadowManager.log(Level.WARNING, "Shadow has been subscribed by " + message.getSender().toString());
            ShadowManager.shadowEndpoints.add(message.getSender());
            message.getSender().sendOperatorMessage("Your endpoint has been added to the list of Shadows");
        } else {
            message.getSender().sendOperatorMessage("Access denied");
        }
    }

    static void cmdListShadows(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.LIST_SHADOW)) {
            message.getSender().sendOperatorMessage("Listing shadows");
            for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++) {
                message.getSender().sendOperatorMessage("Shadow: " + ShadowManager.shadowEndpoints.get(i).toString());
            }
            message.getSender().sendOperatorMessage("Done listing shadows");
        } else {
            message.getSender().sendOperatorMessage("Access denied");
        }
    }

    static void cmdRemoveShadow(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.ADD_REMOVE_SHADOW)) {
            ShadowManager.log(Level.WARNING, "Shadow has been removed by " + message.getSender().toString());
            ShadowManager.shadowEndpoints.remove(message.getSender());
            message.getSender().sendOperatorMessage("Your endpoint has been removed from the list of Shadows");
        } else {
            message.getSender().sendOperatorMessage("Access denied");
        }
    }

}
