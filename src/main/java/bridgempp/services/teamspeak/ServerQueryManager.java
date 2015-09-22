package bridgempp.services.teamspeak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.Message;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

public class ServerQueryManager implements Runnable
{

	private Socket socket;
	private PrintStream printStream;
	private BufferedReader bufferedReader;
	private TeamspeakService service;
	private boolean running;
	
	private Pattern messagePattern = Pattern.compile("notifytextmessage targetmode=. msg=(.*?) invokerid=. invokername=(.*?) invokderuid=.*?");
	
	public void sendMessage(Message message)
	{
		printStream.println("sendtextmessage targetmode=3 target=1 msg=\"" + message.toSimpleString(MessageFormat.PLAIN_TEXT_ONLY) + "\"");
	}
	
	public void startReadingThread()
	{
		Thread readingThread = new Thread(this);
		readingThread.setName("Teamspeak ServerQuery Thread");
		readingThread.start();
	}

	@Override
	public void run()
	{
		try
		{
			socket = new Socket(service.getServerAddress(), service.getServerPort());
			printStream = new PrintStream(socket.getOutputStream(), true);
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			printStream.println("login " + service.getQueryUsername() + " " + service.getQueryPassword());
			printStream.println("use " + service.getVirtualServerID());
			printStream.println("servernotifyregister event=textserver");
			while(running)
			{
				String line = bufferedReader.readLine();
				Matcher matcher = messagePattern.matcher(line);
				while(matcher.find())
				{
					Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier("TalkMPP", service);
					User user = DataManager.getOrNewUserForIdentifier(matcher.group(2), endpoint);
					Message message = new Message(user, endpoint, matcher.group(1), MessageFormat.PLAIN_TEXT);
					CommandInterpreter.processMessage(message);
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
