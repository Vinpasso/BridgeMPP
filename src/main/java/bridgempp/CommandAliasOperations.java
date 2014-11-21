/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.PermissionsManager.Permission;
import java.util.Scanner;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class CommandAliasOperations {

    static void cmdCreateAlias(Endpoint sender, String command) {
        if (!sender.hasAlias()) {
            sender.sendMessage("You already have an Alias, overwriting your old Alias");
        }
        String newAlias = CommandInterpreter.getStringFromArgument(command);
        ShadowManager.log(Level.FINER, "Endpoint: " + sender.toString() + " now has assigned Alias: " + newAlias);
        EndpointTranslator.saveHumanReadableEndpoint(sender, newAlias);
        sender.sendMessage("Alias successfully assigned");
    }

    static void cmdImportAliasList(Endpoint sender, String command) {
        if (CommandInterpreter.checkPermission(sender, Permission.IMPORT_ALIAS)) {
            Scanner scanner = new Scanner(CommandInterpreter.getStringFromArgument(command));
            scanner.useDelimiter(";");
            while (scanner.hasNext()) {
                EndpointTranslator.saveHumanReadableEndpoint(new Endpoint(null, scanner.next()), rearrangeNameFormat(scanner.next()));
            }
            sender.sendMessage("Alias List successfully imported");
        }
        else
        {
            sender.sendMessage("Access Denied");
        }
    }

    //If user is written in outlook Last Name, First Name format then rearrange it to First Name Last Name
    private static String rearrangeNameFormat(String name) {
        if (name.contains(", ")) {
            name = name.substring(name.indexOf(", ") + 2) + " " + name.substring(0, name.indexOf(", "));
        }
        return name;
    }

}
