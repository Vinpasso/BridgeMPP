package bridgempp.services.telegram;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Level;

import javax.activation.MimeTypeParseException;

import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.message.formats.media.ImageMessageBody;

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
		if (message.getPhoto() != null)
		{
			Optional<PhotoSize> photo = message.getPhoto().stream().sorted(new Comparator<PhotoSize>() {

				@Override
				public int compare(PhotoSize o1, PhotoSize o2)
				{
					return (o1.getWidth() * o1.getHeight()) - (o2.getWidth() - o2.getHeight());
				}

			}).findFirst();
			if (photo.isPresent())
			{
				Message bridgeMessage = new MessageBuilder(user, endpoint).build();
				GetFile getFile = new GetFile();
				getFile.setFileId(photo.get().getFileId());
				try
				{
					org.telegram.telegrambots.api.objects.File file = getFile(getFile);
					URL imageURL = new URL("https://api.telegram.org/bot" + getBotToken() + "/" + file.getFilePath());
					ImageMessageBody body = new ImageMessageBody(imageURL.openConnection());
					bridgeMessage.addMessageBody(body);
					if (message.getCaption() != null)
					{
						body.setCaption(message.getCaption());
					}
					service.receiveMessage(bridgeMessage);
				} catch (TelegramApiException | IOException | MimeTypeParseException e)
				{
					ShadowManager.log(Level.SEVERE, "Failed to generate Telegram file request", e);
				}
			}
		}
	}

	public void sendMessage(Message bridgeMessage, DeliveryGoal deliveryGoal)
	{
		Endpoint endpoint = deliveryGoal.getTarget();
		try
		{
			if (bridgeMessage.isTextMessage())
			{
				SendMessage message = new SendMessage();
				message.setChatId(endpoint.getPartOneIdentifier());
				message.setText(bridgeMessage.getPlainTextMessageBody());
				sendMessage(message);
			} else if (bridgeMessage.hasOriginalMessageBody(ImageMessageBody.class))
			{
				ImageMessageBody body = bridgeMessage.getMessageBody(ImageMessageBody.class);
				if (body == null)
				{
					return;
				}
				SendPhoto message = new SendPhoto();
				message.setChatId(endpoint.getPartOneIdentifier());
				message.setNewPhoto(body.getIdentifier(), body.getURL().openStream());
				if (body.hasCaption())
				{
					message.setCaption(body.getCaption());
				}
				sendPhoto(message);
			}
			deliveryGoal.setDelivered();
		} catch (TelegramApiException | IOException e)
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
