package bridgempp.command.wrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.regex.Pattern;

import bridgempp.PermissionsManager;
import bridgempp.PermissionsManager.Permissions;
import bridgempp.message.Message;
import bridgempp.ShadowManager;

public class CommandWrapper {

	private static ArrayList<Class<?>> commands;

	public static void executeCommand(Message message) {
		for (Class<?> commandClass : commands) {
			for (Method method : commandClass.getDeclaredMethods()) {
				if (!attemptMatch(method, message)) {
					continue;
				}
				ShadowManager.log(Level.INFO, "Invoking Command: " + method.getName()
						+ " due to " + message.getSender().toString());
				if (!testPermissions(method, message)) {
					sendInsufficientPermissions(method, message);
					continue;
				}
				Object[] parameters = fetchParameters(method, message);
				if (parameters == null) {
					ShadowManager
							.logAndReply(
									Level.INFO,
									"Invalid Arguments. Type \"!command <command>\" to access the command's help topic",
									message);
					continue;
				}
				callCommand(method, parameters, message);
				ShadowManager.log(Level.INFO, "Command executed: " + method.getName()
						+ " due to " + message.getSender().toString());
			}
		}
	}

	private static void sendInsufficientPermissions(Method method,
			Message message) {
		RequiredPermission requiredPermissions = method
				.getAnnotation(RequiredPermission.class);
		if (requiredPermissions == null) {
			ShadowManager.logAndReply(Level.SEVERE,
					"Access denied due to non existant Permission", message);
		}
		ShadowManager.logAndReply(Level.WARNING,
				"Access Denied. Required Permissions: "
						+ requiredPermissions.value().toString(), message);
	}

	private static void callCommand(Method method, Object[] parameters,
			Message message) {
		try {
			Object result = method.invoke(null, parameters);
			if (result != null) {
				message.getOrigin().sendOperatorMessage(result.toString());
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			ShadowManager
					.logAndReply(
							Level.WARNING,
							"Failed to execute BridgeMPP Command. Could not invoke Command Method",
							message, e);
		}
	}

	private static Object[] fetchParameters(Method command, Message message) {
		return parseParametersCommandLineStyle(command.getParameters(), message);
	}

	private static boolean testPermissions(Method command, Message message) {
		RequiredPermission permissionDefinition = command
				.getAnnotation(RequiredPermission.class);
		if (permissionDefinition == null) {
			return true;
		}
		if (PermissionsManager.hasPermissions(message.getOrigin(),
				Permissions.getPermission(permissionDefinition.value()))) {
			return true;
		}
		return false;
	}

	private static boolean attemptMatch(Method command, Message message) {
		CommandTrigger triggerDefinition = command
				.getAnnotation(CommandTrigger.class);
		if (triggerDefinition == null) {
			return false;
		}
		if (Pattern
				.compile(triggerDefinition.value(), Pattern.CASE_INSENSITIVE)
				.matcher(message.getPlainTextMessage()).find()) {
			return true;
		}
		return false;
	}

	private static Object[] parseParametersCommandLineStyle(
			Parameter[] parameters, Message bridgemppMessage) {
		if (parameters.length == 0) {
			return new Object[0];
		}
		if (parameters.length == 1
				&& parameters[0].getType().equals(Message.class)) {
			return new Object[] { bridgemppMessage };
		}
		String[] splittedString = splitCommandLine(bridgemppMessage
				.getPlainTextMessage());
		Object[] parameterObjects = new Object[parameters.length];
		int splittedStringCounter = 1;
		for (int i = 0; i < parameterObjects.length; i++) {
			if (parameters[i].getType().equals(Message.class)) {
				parameterObjects[i] = bridgemppMessage;
				continue;
			}
			if (splittedStringCounter >= splittedString.length) {
				return null;
			}
			try {
				switch (parameters[i].getType().getName()) {
				case "java.lang.String":
					parameterObjects[i] = splittedString[splittedStringCounter];
					break;
				case "boolean":
					parameterObjects[i] = Boolean
							.parseBoolean(splittedString[splittedStringCounter]);
					break;
				case "int":
					parameterObjects[i] = Integer
							.parseInt(splittedString[splittedStringCounter]);
					break;
				case "double":
					parameterObjects[i] = Double
							.parseDouble(splittedString[splittedStringCounter]);
					break;
				default:
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			splittedStringCounter++;
		}
		return parameterObjects;
	}

	private static String[] splitCommandLine(String message) {
		message = message + " ";
		LinkedList<String> list = new LinkedList<>();
		char[] characters = message.toCharArray();
		char delimiter = 0;
		int startSequence = 0;
		for (int i = 0; i < characters.length; i++) {
			if (delimiter == 0) {
				if (Character.isWhitespace(characters[i])) {
					continue;
				}
				if (characters[i] == '\'' || characters[i] == '\"') {
					startSequence = i;
					delimiter = characters[i];
				} else {
					startSequence = i - 1;
					delimiter = ' ';
				}
			} else {
				if (characters[i] == delimiter && characters[i - 1] != '\\') {
					if (startSequence + 1 > i - 1) {
						list.add("");
					} else {
						list.add(message.substring(startSequence + 1, i)
								.replace("\\" + delimiter, "" + delimiter));
					}
					delimiter = 0;
				}
			}
		}
		return list.toArray(new String[list.size()]);
	}

	static Class<?>[] listCommands() {
		return commands.toArray(new Class<?>[commands.size()]);
	}

	public static Method getCommand(String command) {
		for (Class<?> commandClass : commands) {
			for (Method method : commandClass.getDeclaredMethods()) {
				if (method.getDeclaredAnnotation(CommandTrigger.class) != null
						&& Pattern
								.compile(
										method.getDeclaredAnnotation(
												CommandTrigger.class).value(),
										Pattern.CASE_INSENSITIVE)
								.matcher(command).find()) {
					return method;
				}
			}
		}
		return null;
	}

	public static void loadCommandClass(Class<?> commandClass) {
		ShadowManager.log(Level.INFO,
				"Loading Command Class: " + commandClass.getSimpleName());
		if (commands == null) {
			commands = new ArrayList<Class<?>>();
		}
		commands.add(commandClass);
	}

}
