package bridgempp.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import bridgempp.Message;
import bridgempp.PermissionsManager.Permission;

public class CommandCommandInterpreterOperations extends CommandInterpreter {

	public static void cmdHelp(Message message)
	{
		String helpCommand = getStringFromArgument(message.getPlainTextMessage());
		boolean success = false;
		try {
			for (Class<?> commandClass : commandClasses) {
				try {
					Method helpMethod = commandClass.getMethod("help" + helpCommand);
					String helpTopic = (String)helpMethod.invoke(null);
					success = true;
					message.getSender().sendOperatorMessage("Help: " + helpTopic);
					break;
				} catch (NoSuchMethodException e) {
				}
			}
		} catch (SecurityException e) {
			message.getSender().sendOperatorMessage(
					"No security access available");
		} catch (IllegalAccessException e) {
			message.getSender().sendOperatorMessage(
					"Illegal security access");
		} catch (IllegalArgumentException e) {
			message.getSender().sendOperatorMessage(
					"Illegal argument access");
		} catch (InvocationTargetException e) {
			message.getSender().sendOperatorMessage(
					"Illegal Invocation target access");
		}
		if(!success)
		{
			message.getSender().sendOperatorMessage("No Help Topic found with name: " + helpCommand);
		}
	}
	
	public static Permission permHelp()
	{
		return Permission.NONE;
	}
	
}
