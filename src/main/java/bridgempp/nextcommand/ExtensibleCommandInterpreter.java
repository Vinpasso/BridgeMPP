package bridgempp.nextcommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.regex.Matcher;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.messageformat.MessageFormat;

public class ExtensibleCommandInterpreter
{
	private static ExtensibleCommandInterpreter instance;
	private static ServiceLoader<BridgeMPPCommand> serviceLoader;

	public void processMessage(Message message)
	{
		Iterator<BridgeMPPCommand> iterator = serviceLoader.iterator();
		while (iterator.hasNext())
		{
			BridgeMPPCommand command = iterator.next();
			Matcher matcher = command.getCallCommand().matcher(message.getPlainTextMessage());
			if (matcher.matches())
			{
				if (!CommandInterpreter.checkPermission(message.getSender(), command.getRequiredPermissions()))
				{
					ShadowManager.log(Level.WARNING,
							message.getSender().toString(true)
									+ " tried to execute a BridgeMPP Server Command without necessary permissions: "
									+ message.getPlainTextMessage());
					message.getSender().sendMessage(
							new Message(message.getSender(), "Access Denied. You do not have the necessary Permissions",
									MessageFormat.PLAIN_TEXT));
				}
				Method commandMethod = command.getCommandMethod();
				Object[] parameters = parseParametersCommandLineStyle(commandMethod.getParameters(), matcher.group(1),
						message);
				if (parameters == null)
				{
					message.getSender().sendMessage(
							new Message(message.getSender(), "Help Topic:\n" + command.getHelpTopic(),
									MessageFormat.PLAIN_TEXT));
				}
				try
				{
					commandMethod.invoke(null, parameters);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					ShadowManager.log(Level.WARNING, "An error occured while trying to execute BridgeMPP Command.", e);
					message.getSender().sendOperatorMessage(
							"An error occured while trying to execute BridgeMPP Command: " + e.getMessage());
				}
			}
		}
	}

	private ExtensibleCommandInterpreter()
	{
		serviceLoader = ServiceLoader.load(BridgeMPPCommand.class);
	}

	public static synchronized ExtensibleCommandInterpreter getInstance()
	{
		if (instance == null)
		{
			instance = new ExtensibleCommandInterpreter();
		}
		return instance;
	}

	public static void interpretCommand(Message message)
	{
		getInstance().processMessage(message);
	}

	private Object[] parseParametersCommandLineStyle(Parameter[] parameters, String message, Message bridgemppMessage)
	{
		if (parameters.length == 0)
		{
			return new Object[0];
		}
		if (parameters.length == 1 && parameters[0].getType().equals(Message.class))
		{
			return new Object[] { bridgemppMessage };
		}
		String[] splittedString = splitCommandLine(message);
		Object[] parameterObjects = new Object[parameters.length];
		if (splittedString.length != parameters.length)
		{
			return null;
		}
		for (int i = 0; i < splittedString.length; i++)
		{
			switch (parameters[i].getType().getName())
			{
				case "java.lang.String":
					parameterObjects[i] = splittedString[i];
					break;
				case "boolean":
					parameterObjects[i] = Boolean.parseBoolean(splittedString[i]);
					break;
				case "int":
					parameterObjects[i] = Integer.parseInt(splittedString[i]);
					break;
				case "double":
					parameterObjects[i] = Double.parseDouble(splittedString[i]);
					break;
				default:
					parameterObjects[i] = splittedString[i];
					break;
			}
		}
		return parameterObjects;
	}

	private String[] splitCommandLine(String message)
	{
		message = message + " ";
		LinkedList<String> list = new LinkedList<>();
		char[] characters = message.toCharArray();
		char delimiter = 0;
		int startSequence = 0;
		for (int i = 0; i < characters.length; i++)
		{
			if (delimiter == 0)
			{
				if (Character.isWhitespace(characters[i]))
				{
					continue;
				}
				if (characters[i] == '\'' || characters[i] == '\"')
				{
					startSequence = i;
					delimiter = characters[i];
				} else
				{
					startSequence = i - 1;
					delimiter = ' ';
				}
			} else
			{
				if (characters[i] == delimiter && characters[i - 1] != '\\')
				{
					if (startSequence + 1 > i - 1)
					{
						list.add("");
					} else
					{
						list.add(message.substring(startSequence + 1, i).replace("\\" + delimiter, "" + delimiter));
					}
					delimiter = 0;
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

}
