package bridgempp.services.socket;

import java.net.URL;
import java.util.logging.Level;

import bridgempp.GroupManager;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.MessageBuilder;
import bridgempp.message.formats.media.ImageMessageBody;
import bridgempp.message.formats.text.HTMLMessageBody;
import bridgempp.message.formats.text.PlainTextMessageBody;
import bridgempp.message.formats.text.XHTMLMessageBody;
import bridgempp.service.BridgeService;
import bridgempp.services.socket.protobuf.Group;
import bridgempp.services.socket.protobuf.Message.Builder;
import bridgempp.services.socket.protobuf.MessageBody;
import bridgempp.services.socket.protobuf.MessageType;

public class ProtoBufUtils
{

	public static void parseMessage(bridgempp.services.socket.protobuf.Message protoMessage, MessageBuilder messageBuilder)
	{
		protoMessage.getDestinationGroupsList().forEach(group -> messageBuilder.addDestinationsFromGroupNoLoopback(GroupManager.findGroup(group.getName())));
		protoMessage.getDestinationsList()
				.forEach(destinationEndpoint -> messageBuilder.addMessageDestination(DataManager.getEndpointForIdentifier(destinationEndpoint.getIdentifier())));
		protoMessage.getMessageBodiesList().forEach(messageBody -> {
			switch (messageBody.getMessageType())
			{
				case HTML:
					messageBuilder.addMessageBody(new HTMLMessageBody(messageBody.getContents()));
					break;
				case IMAGE_URL:
					try
					{
						messageBuilder.addMessageBody(new ImageMessageBody(new URL(messageBody.getContents()).openConnection()));
					} catch (Exception e)
					{
						ShadowManager.log(Level.WARNING, "Could not decode Image URL");
					}
					break;
					
				case IMAGE_INLINE:
					messageBuilder.addMessageBody(new ImageMessageBody(mimeType, fileName, inputStream));
				case PLAIN_TEXT:
					messageBuilder.addPlainTextBody(messageBody.getContents());
					break;
				case XHTML:
					messageBuilder.addMessageBody(new XHTMLMessageBody(messageBody.getContents()));
					break;
					
				case UNRECOGNIZED:
				default:
					ShadowManager.log(Level.WARNING, "Got unrecognized Message Type!");
					break;
			}
		});
	}

	public static bridgempp.services.socket.protobuf.Message serializeMessage(bridgempp.message.Message message)
	{
		Builder builder = bridgempp.services.socket.protobuf.Message.newBuilder();
		builder.setSender(serializeUser(message.getSender()));
		builder.setOrigin(serializeEndpoint(message.getOrigin()));
		message.getGroups().forEach(group -> builder.addDestinationGroups(serializeGroup(group)));
		message.getDestinations().forEach(deliveryGoal -> builder.addDestinations(serializeEndpoint(deliveryGoal.getTarget())));
		message.getMessageBodies().forEach(body -> builder.addMessageBodies(serializeMessageBody(body)));
		return builder.build();
	}

	public static MessageBody serializeMessageBody(bridgempp.message.MessageBody body)
	{
		MessageBody.Builder builder = MessageBody.newBuilder();
		if(body instanceof PlainTextMessageBody)
		{
			builder.setMessageType(MessageType.PLAIN_TEXT);
			builder.setContents(((PlainTextMessageBody)body).getText());
		}
		else if(body instanceof HTMLMessageBody)
		{
			builder.setMessageType(MessageType.HTML);
			builder.setContents(((HTMLMessageBody)body).getText());
		}
		else if(body instanceof XHTMLMessageBody)
		{
			builder.setMessageType(MessageType.XHTML);
			builder.setContents(((XHTMLMessageBody)body).getText());
		}
		else if(body instanceof ImageMessageBody)
		{
			builder.setMessageType(MessageType.IMAGE_URL);
			builder.setContents(((ImageMessageBody)body).getURL().toString());
		}
		return builder.build();
	}

	public static Group serializeGroup(bridgempp.data.Group group)
	{
		return Group.newBuilder()
				.setName(group.getName())
				.build();
	}

	public static bridgempp.services.socket.protobuf.Endpoint serializeEndpoint(Endpoint origin)
	{
		return bridgempp.services.socket.protobuf.Endpoint.newBuilder()
				.setIdentifier(origin.getIdentifier())
				.setService(ProtoBufUtils.serializeService(origin.getService()))
				.build();
	}

	public static bridgempp.services.socket.protobuf.Service serializeService(BridgeService service)
	{
		return bridgempp.services.socket.protobuf.Service.newBuilder()
				.setIdentifier(service.getIdentifier())
				.build();
	}

	public static bridgempp.services.socket.protobuf.User serializeUser(User user)
	{
		return bridgempp.services.socket.protobuf.User.newBuilder().setIdentifier(user.getIdentifier()).setName(user.getName()).build();
	}

}
