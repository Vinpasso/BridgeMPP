package bridgempp.services.irc;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;
import org.jibble.pircbot.PircBot;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.crypto.Data;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

@Entity(name = "IRC_SERVICE")
@DiscriminatorValue(value = "IRC_SERVICE")
public class IRCService extends BridgeService {

    @Column(name = "HOST", nullable = false, length = 50)
    private String host;

    @Column(name = "PORT", nullable = false)
    private int port;

    @Column(name = "CONNECT_SSL", nullable = false)
    private boolean connect_ssl;

    @Column(name = "NICKNAME", nullable = false, length = 50)
    private String nickname;

    private transient BridgePircBot bot;

    @Override
    public void connect() throws Exception {
        bot = new BridgePircBot(this);
        bot.setVerbose(true);
        bot.connect(host, port);

        for (Endpoint endpoint : endpoints) {
            // Join any channels
            String endpointIdentifier = endpoint.getIdentifier();
            if(endpointIdentifier.startsWith("#")) {
                ShadowManager.log(Level.INFO, "Rejoining IRC channel: " + endpointIdentifier);
                bot.joinChannel(endpointIdentifier);
            }
        }
    }

    @Override
    public void disconnect() throws Exception {
        bot.disconnect();
    }

    @Override
    public void sendMessage(Message message) {
        bot.sendMessage(message.getDestination().getIdentifier(), message.getPlainTextMessage());
    }

    @Override
    public String getName() {
        return "IRC";
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public MessageFormat[] getSupportedMessageFormats() {
        return MessageFormat.PLAIN_TEXT_ONLY;
    }

    public void configure(String host, int port, boolean connect_ssl, String nickname) {
        this.host = host;
        this.port = port;
        this.connect_ssl = connect_ssl;
        this.nickname = nickname;
    }
}
