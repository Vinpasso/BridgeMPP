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

@Entity(name = "IRC_SERVICE")
@DiscriminatorValue(value = "IRC_SERVICE")
public class IRCService extends BridgeService {

    @Column(name = "HOST", nullable = false, length = 50)
    private String host;

    @Column(name = "PORT", nullable = false)
    private int port;

    @Column(name = "SSL", nullable = false)
    private boolean ssl;

    @Column(name = "NICKNAME", nullable = false, length = 50)
    private String nickname;

    private transient BridgePircBot bot;

    @Override
    public void connect() throws Exception {
        bot = new BridgePircBot(this);
        bot.setVerbose(true);
        bot.connect(host, port);
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
}
