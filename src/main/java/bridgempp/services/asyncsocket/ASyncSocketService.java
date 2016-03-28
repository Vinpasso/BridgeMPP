package bridgempp.services.asyncsocket;

import java.net.InetAddress;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.SingleToMultiBridgeService;
import bridgempp.services.socketservice.protobuf.ProtoBuf;

@Entity(name = "ASYNC_SOCKET_SERVICE")
@DiscriminatorValue(value = "ASYNC_SOCKET_SERVICE")
public class ASyncSocketService extends SingleToMultiBridgeService<ASyncSocketService, ASyncSocketClient>
{

	private transient static final MessageFormat[] SUPPORTED_MESSAGE_FORMATS = new MessageFormat[] { MessageFormat.XHTML, MessageFormat.PLAIN_TEXT };
	private transient EventLoopGroup serverGroup;
	private transient EventLoopGroup clientGroup;

	@Column(name = "Listen_Address", length = 50, nullable = false)
	private String listenAddress;
	
	@Column(name = "Listen_Port", nullable = false)
	private int listenPort;
	
	@Column(name = "Server_Threads", nullable = false)
	private int numServerThreads;

	@Column(name = "Client_Threads", nullable = false)
	private int numClientThreads;
	
	@Override
	public void connect()
	{
		serverGroup = new NioEventLoopGroup(numServerThreads);
		clientGroup = new NioEventLoopGroup(numClientThreads);
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(serverGroup, clientGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.option(ChannelOption.SO_BACKLOG, 100);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception
			{
				String identifier = ch.remoteAddress().toString();
				Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(identifier, ASyncSocketService.this);
				User user = DataManager.getOrNewUserForIdentifier(identifier, endpoint);

				ASyncSocketClient handle = new ASyncSocketClient(ASyncSocketService.this, endpoint, user, ch);
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("idleStateHandler", new IdleStateHandler(120, 60,
						120));
				pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
				pipeline.addLast("protobufDecoder", new ProtobufDecoder(
						ProtoBuf.Message.getDefaultInstance()));
				pipeline.addLast("frameEncoder",
						new ProtobufVarint32LengthFieldPrepender());
				pipeline.addLast("protobufEncoder", new ProtobufEncoder());
				pipeline.addLast("keepAliveSender", new KeepAliveSender(handle));
				pipeline.addLast(new SimpleChannelInboundHandler<ProtoBuf.Message>() {

					@Override
					protected void channelRead0(ChannelHandlerContext ctx, bridgempp.services.socketservice.protobuf.ProtoBuf.Message msg) throws Exception
					{
						handle.messageReceived(msg);
					}

				});
			}
			
		});
		try
		{
			bootstrap.bind(InetAddress.getByName(listenAddress), listenPort);
		} catch (Exception e)
		{
			ShadowManager.log(Level.SEVERE, "Failed to bind Server Bootstrap", e);
		}
	}

	@Override
	public void disconnect()
	{
		try
		{
			serverGroup.shutdownGracefully().await();
			clientGroup.shutdownGracefully().await();
			while(!handles.isEmpty())
			{
				handles.values().iterator().next().disconnect();
			}
			super.disconnect();
		}
		catch(Exception e)
		{
			ShadowManager.log(Level.WARNING, "Could not disconnect ASyncSocketService", e);
		}
	}

	@Override
	public String getName()
	{
		return "ASyncTCPSocket";
	}

	@Override
	public boolean isPersistent()
	{
		return false;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return SUPPORTED_MESSAGE_FORMATS;
	}

	public void configure(String listenAddress, int listenPort, int serverThreads, int clientThreads)
	{
		this.listenAddress = listenAddress;
		this.listenPort = listenPort;
		this.numServerThreads = serverThreads;
		this.numClientThreads = clientThreads;
	}

}
