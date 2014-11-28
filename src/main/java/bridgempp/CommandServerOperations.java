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
public class CommandServerOperations {

    static void cmdExit(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.EXIT)) {
            ShadowManager.log(Level.WARNING, "Server is being remotely shutdown by " + message.getSender().toString());
            message.getSender().sendOperatorMessage("Shutting down");
            BridgeMPP.exit();
        } else {
            message.getSender().sendOperatorMessage("Access denied");
        }
    }
    
}
