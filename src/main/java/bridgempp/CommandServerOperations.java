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

    static void cmdExit(Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.EXIT)) {
            ShadowManager.log(Level.WARNING, "Server is being remotely shutdown by " + sender.toString());
            sender.sendMessage("BridgeMPP: Shutting down");
            BridgeMPP.exit();
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }
    
}
