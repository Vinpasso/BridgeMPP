/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.PermissionsManager.Permission;
import bridgempp.ShadowManager;

/**
 *
 * @author Vinpasso
 */
public class CommandShadowOperations extends CommandInterpreter {

	public static void cmdAddShadow(Message message) {
		ShadowManager.log(Level.WARNING, "Shadow has been subscribed by "
				+ message.getSender().toString());
		ShadowManager.shadowEndpoints.add(message.getSender());
		message.getSender().sendOperatorMessage(
				"Your endpoint has been added to the list of Shadows");
	}

	public static Permission permAddShadow() {
		return Permission.ADD_REMOVE_SHADOW;
	}

	public static String helpAddShadow() {
		return formatHelp("Adds the current Endpoint to the list of Shadows.");
	}

	public static void cmdListShadows(Message message) {
		message.getSender().sendOperatorMessage("Listing shadows");
		for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++) {
			message.getSender().sendOperatorMessage(
					"Shadow: "
							+ ShadowManager.shadowEndpoints.get(i).toString());
		}
		message.getSender().sendOperatorMessage("Done listing shadows");
	}

	public static Permission permListShadows() {
		return Permission.LIST_SHADOW;
	}

	public static String helpListShadows() {
		return formatHelp("Lists all Endpoints in the List of Shadows.");
	}

	public static void cmdRemoveShadow(Message message) {
		ShadowManager.log(Level.WARNING, "Shadow has been removed by "
				+ message.getSender().toString());
		ShadowManager.shadowEndpoints.remove(message.getSender());
		message.getSender().sendOperatorMessage(
				"Your endpoint has been removed from the list of Shadows");
	}

	public static Permission permRemoveShadow() {
		return Permission.ADD_REMOVE_SHADOW;
	}

	public static String helpRemoveShadow() {
		return formatHelp("Removes the current Endpoint from the list of Shadows.");
	}

}
