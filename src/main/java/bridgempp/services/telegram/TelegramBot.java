package bridgempp.services.telegram;

import java.util.logging.Level;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

public class TelegramBot extends TelegramLongPollingBot
{

	private TelegramService service;

	@Override
	public String getBotUsername()
	{
		return "BridgeMPP";
	}

	@Override
	public void onUpdateReceived(Update update)
	{
		org.telegram.telegrambots.api.objects.Message message = update.getMessage();
		if (message != null && message.hasText())
		{
			handleIncomingMessage(message);
		}
	}

	private void handleIncomingMessage(org.telegram.telegrambots.api.objects.Message message)
	{
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(message.getChatId() + "", service);
		User user = DataManager.getOrNewUserForIdentifier(message.getFrom().getId() + "", endpoint);
		Message bridgeMessage = new Message(user, endpoint, message.getText(), MessageFormat.PLAIN_TEXT);
		CommandInterpreter.processMessage(bridgeMessage);
	}
	
	public void sendMessage(Message bridgeMessage)
	{
		SendMessage message = new SendMessage();
		message.setChatId(bridgeMessage.getDestination().getIdentifier());
		message.setText(bridgeMessage.getPlainTextMessage());
		try
		{
			sendMessage(message);
		} catch (TelegramApiException e)
		{
			ShadowManager.log(Level.SEVERE, "Telegram Api Error", e);
		}
	}

	@Override
	public String getBotToken()
	{
		return service.getToken();
	}

}
