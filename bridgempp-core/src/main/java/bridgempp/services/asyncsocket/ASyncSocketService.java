package bridgempp.services.asyncsocket;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.service.SingleToMultiBridgeService;
import bridgempp.services.socket.protobuf.Message;
import bridgempp.state.EventManager;
import bridgempp.state.handle.ASyncSocketHandleRemover;

@Entity(name = "ASYNC_SOCKET_SERVICE")
@DiscriminatorValue(value = "ASYNC_SOCKET_SERVICE")
public class ASyncSocketService extends SingleToMultiBridgeService<ASyncSocketService, ASyncSocketClient>
{

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
	public void connect() throws UnknownHostException
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
				pipeline.addLast("idleStateHandler", new IdleStateHandler(120, 60, 120));
				pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
				pipeline.addLast("protobufDecoder", new ProtobufDecoder(Message.getDefaultInstance()));
				pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
				pipeline.addLast("protobufEncoder", new ProtobufEncoder());
				pipeline.addLast("keepAliveSender", new KeepAliveSender(handle));
				pipeline.addLast(new SimpleChannelInboundHandler<Message>() {

					@Override
					protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception
					{
						handle.messageReceived(msg);
					}

				});
			}

		});

		bootstrap.bind(InetAddress.getByName(listenAddress), listenPort);
		EventManager.loadEventListenerClass(new ASyncSocketHandleRemover());
	}

	@Override
	public void disconnect() throws Exception
	{
		serverGroup.shutdownGracefully().await();
		clientGroup.shutdownGracefully().await();
		while (!handles.isEmpty())
		{
			handles.values().iterator().next().disconnect();
		}
		super.disconnect();
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

	public void configure(String listenAddress, int listenPort, int serverThreads, int clientThreads)
	{
		this.listenAddress = listenAddress;
		this.listenPort = listenPort;
		this.numServerThreads = serverThreads;
		this.numClientThreads = clientThreads;
	}

}
