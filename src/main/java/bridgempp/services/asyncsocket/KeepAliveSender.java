package bridgempp.services.asyncsocket;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.messageformat.MessageFormat;
import bridgempp.services.socketservice.protobuf.ProtoBuf;

public class KeepAliveSender extends ChannelDuplexHandler {
	
	private ASyncSocketClient client;
	
	public KeepAliveSender(ASyncSocketClient client)
	{
		this.client = client;
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext context, Object event) {
		if (event instanceof IdleStateEvent) {
			IdleStateEvent idleEvent = (IdleStateEvent) event;
			if (idleEvent.state() == IdleState.WRITER_IDLE) {
				sendPing(context);
			} else if (idleEvent.state() == IdleState.READER_IDLE) {
				ShadowManager.log(Level.WARNING,
						"A Connection is stalling due to READER_IDLE");
				sendPing(context);
			} else if (idleEvent.state() == IdleState.ALL_IDLE) {
				ShadowManager.log(Level.WARNING,
								"Communications are stalling on a connection due to ALL_IDLE");
				sendPing(context);
			}
		}
	}

	private void sendPing(ChannelHandlerContext context) {
		ProtoBuf.Message protoMessage = ProtoBuf.Message.newBuilder()
				.setMessageFormat(MessageFormat.PLAIN_TEXT.getName())
				.setMessage("").setSender("").setTarget("")
				.setGroup("").build();
		ChannelFuture future = context.writeAndFlush(protoMessage);
		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				if (!future.isSuccess()) {
					ShadowManager.log(Level.SEVERE,
							"A Connection has been disconnected after PING: "
									+ future.toString()
									+ ", exiting...");
					client.disconnect();
				}
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ShadowManager.log(Level.WARNING,
						"Communications have broken down due to Exception on " + client.getUser().toString() + ": " + cause.getMessage());
		client.disconnect();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ShadowManager.log(Level.INFO,
						"Communications have broken down due to Channel Deactivation on " + client.getUser().toString());
		client.disconnect();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ShadowManager.log(Level.INFO,
						"Communications have broken down due to Channel Deregistration on " + client.getUser().toString());
		client.disconnect();
	}
}