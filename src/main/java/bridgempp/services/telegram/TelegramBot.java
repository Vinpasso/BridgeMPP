package bridgempp.services.telegram;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.messageformat.MessageFormat;

public class TelegramBot extends TelegramLongPollingBot
{

	private TelegramService service;

	public TelegramBot(TelegramService service)
	{
		super();
		this.service = service;
	}

	@Override
	public String getBotUsername()
	{
		return "BridgeMPP";
	}

	@Override
	public void onUpdateReceived(Update update)
	{
		org.telegram.telegrambots.api.objects.Message message = update.getMessage();
		if (message != null)
		{
			handleIncomingMessage(message);
		}
	}

	private void handleIncomingMessage(org.telegram.telegrambots.api.objects.Message message)
	{
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(message.getChatId() + "@telegram-groups", service);
		User user = DataManager.getOrNewUserForIdentifier(message.getFrom().getId() + "@telegram-users", endpoint);
		if (!user.hasAlias())
		{
			String username = "";
			username += (message.getFrom().getFirstName() == null) ? "" : message.getFrom().getFirstName() + " ";
			username += (message.getFrom().getLastName() == null) ? "" : message.getFrom().getLastName();
			username = (username.length() == 0) ? message.getFrom().getUserName() : username;
			user.setName(username.trim());
			ShadowManager.log(Level.INFO, "Automatically extracted Alias from Telegram");
		}
		if (message.hasText())
		{
			Message bridgeMessage = new MessageBuilder(user, endpoint).addPlainTextBody(message.getText()).build();
			service.receiveMessage(bridgeMessage);
		}
		if(message.getPhoto() != null)
		{
			Optional<PhotoSize> photo = message.getPhoto().stream().sorted(new Comparator<PhotoSize>() {

				@Override
				public int compare(PhotoSize o1, PhotoSize o2)
				{
					return (o1.getWidth() * o1.getHeight()) - (o2.getWidth() - o2.getHeight());
				}
				
			}).findFirst();
			if(photo.isPresent())
			{
				Message bridgeMessage = new Message(user, endpoint, null, MessageFormat.FILE_BACKED_IMAGE_FORMAT);
				GetFile getFile = new GetFile();
				getFile.setFileId(photo.get().getFileId());
				try {
					org.telegram.telegrambots.api.objects.File file = getFile(getFile);
					String stringembeddedimage = IOUtils.toString(new URL("https://api.telegram.org/bot" + getBotToken() + "/" + file.getFilePath()), StandardCharsets.UTF_8);
					bridgeMessage.setMessage(stringembeddedimage);
				} catch (TelegramApiException | IOException e) {
					ShadowManager.log(Level.SEVERE, "Failed to generate Telegram file request", e);
					bridgeMessage.setMessage("Failed to decode Telegram image");
				}
				service.receiveMessage(bridgeMessage);
			}
		}
	}

	public void sendMessage(Message bridgeMessage, Endpoint endpoint)
	{
		try
		{
			Entry<MessageFormat, String> converted = bridgeMessage.getClosestConversion(MessageFormat.PLAIN_TEXT, MessageFormat.FILE_BACKED_IMAGE_FORMAT);
			if (converted.getKey().equals(MessageFormat.PLAIN_TEXT))
			{
				SendMessage message = new SendMessage();
				message.setChatId(bridgeMessage.getDestination().getPartOneIdentifier());
				message.setText(bridgeMessage.toSimpleString(MessageFormat.PLAIN_TEXT));
				sendMessage(message);
			} else if (converted.getKey().equals(MessageFormat.FILE_BACKED_IMAGE_FORMAT))
			{
				SendPhoto message = new SendPhoto();
				message.setChatId(bridgeMessage.getDestination().getPartOneIdentifier());
				message.setPhoto(converted.getValue());
				sendPhoto(message);
			}
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
