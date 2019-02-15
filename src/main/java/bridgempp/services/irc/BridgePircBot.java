package bridgempp.services.irc;

import bridgempp.Message;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;
import org.jibble.pircbot.PircBot;

public class BridgePircBot extends PircBot {

    private final BridgeService service;

    public BridgePircBot(BridgeService service) {
        this.service = service;
        setName("BridgeMPP");
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        Endpoint endpoint = DataManager.getEndpointForIdentifier(channel);
        User user = DataManager.getOrNewUserForIdentifier(sender, endpoint);
        service.receiveMessage(new Message(user, endpoint, message, MessageFormat.PLAIN_TEXT));
    }
}
