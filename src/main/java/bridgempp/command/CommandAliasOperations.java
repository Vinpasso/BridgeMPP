/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.ServiceManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.data.DataManager;
import bridgempp.data.User;
import bridgempp.ShadowManager;

/**
 *
 * @author Vinpasso
 */
public class CommandAliasOperations {

    static void cmdCreateAlias(Message message) {
        if (!message.getSender().hasAlias()) {
            message.getOrigin().sendOperatorMessage("You already have an Alias, overwriting your old Alias");
        }
        String newAlias = CommandInterpreter.getStringFromArgument(message.getPlainTextMessage());
        ShadowManager.log(Level.FINER, "Endpoint: " + message.getOrigin().toString() + " now has assigned Alias: " + newAlias);
        message.getSender().setName(newAlias);
        message.getOrigin().sendOperatorMessage("Alias successfully assigned");
    }

    static void cmdImportAliasList(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), Permission.IMPORT_ALIAS)) {
            String[] commands = CommandInterpreter.getStringFromArgument(message.getPlainTextMessage()).split("; ");
            if(commands.length % 3 != 0)
            {
            	message.getOrigin().sendOperatorMessage("Length of Commands not modulo 3! Last import will be dropped! Expected: user; service; alias");
            }
            for(int i = 0; i < commands.length - 1; i += 2) {
                User user = DataManager.getUserForIdentifier(commands[i], ServiceManager.getServiceByServiceIdentifier(Integer.parseInt(commands[i+1])));
                if(user == null)
                {
                	message.getOrigin().sendOperatorMessage("User: " + commands[i] + " not found. Skipping...");
                	continue;
                }
				user.setName(rearrangeNameFormat(commands[i+2]));
            }
            message.getOrigin().sendOperatorMessage("Alias List successfully imported");
        }
        else
        {
            message.getOrigin().sendOperatorMessage("Access Denied");
        }
    }
    
    static void removeAlias(Message message)
    {
    	if(!message.getSender().hasAlias())
    	{
    		message.getOrigin().sendOperatorMessage("No aliases found");
    	}
    	message.getSender().setName("");
    	message.getOrigin().sendOperatorMessage("Aliases successfully deleted");
    }

    //If user is written in outlook Last Name, First Name format then rearrange it to First Name Last Name
    private static String rearrangeNameFormat(String name) {
        if (name.contains(", ")) {
            name = name.substring(name.indexOf(", ") + 2) + " " + name.substring(0, name.indexOf(", "));
        }
        return name;
    }

}
