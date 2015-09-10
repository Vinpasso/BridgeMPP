/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.socket;

import java.net.ServerSocket;
import java.util.Iterator;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.MultiBridgeServiceHandle;
import bridgempp.service.SingleToMultiBridgeService;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "SOCKET_SERVICE")
@DiscriminatorValue(value = "SOCKET_SERVICE")
public class SocketService extends SingleToMultiBridgeService
{

	transient ServerSocket serverSocket;

	@Column(name = "Listen_Address", nullable = false, length = 50)
	String listenAddress;

	@Column(name = "List_Port", nullable = false)
	int listenPort;

	private transient ServerListener serverListener;
	protected transient boolean pendingShutdown = false;

	private transient static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.XHTML, MessageFormat.PLAIN_TEXT };

	@Override
	public void connect()
	{
		ShadowManager.log(Level.INFO, "Loading TCP Server Socket Service...");
		serverListener = new ServerListener(this);
		new Thread(serverListener, "Socket Server Listener").start();
		ShadowManager.log(Level.INFO, "Loaded TCP Server Socket Service");
	}

	@Override
	public void disconnect()
	{
		pendingShutdown = true;
		while (!handles.isEmpty())
		{
			((SocketClient) handles.iterator().next()).disconnect();
		}
	}
	
	public void sendKeepAliveMessages()
	{
		Iterator<MultiBridgeServiceHandle<?>> iterator = handles.iterator();
		while(iterator.hasNext())
		{
			MultiBridgeServiceHandle<?> handle = iterator.next();
			handle.sendMessage(new Message(null, null, null, null, "",
					MessageFormat.PLAIN_TEXT));
		}
	}

	@Override
	public String getName()
	{
		return "TCPSocket";
	}

	@Override
	public boolean isPersistent()
	{
		return false;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats;
	}

	public enum ProtoCarry
	{
		Plain_Text, XML_Embedded, ProtoBuf, None
	}

	public void configure(String listenAddress, int listenPort)
	{
		this.listenAddress = listenAddress;
		this.listenPort = listenPort;
	}
}
