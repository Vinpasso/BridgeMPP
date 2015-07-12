/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.Message;
import bridgempp.PermissionsManager;
import bridgempp.ShadowManager;

/**
 *
 * @author Vinpasso
 */
public class CommandServerOperations {

    static void cmdExit(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.EXIT)) {
            ShadowManager.log(Level.WARNING, "Server is being remotely shutdown by " + message.getOrigin().toString());
            message.getOrigin().sendOperatorMessage("Shutting down");
            BridgeMPP.exit();
        } else {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }
    
}
