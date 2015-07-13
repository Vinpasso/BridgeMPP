package bridgempp.command.wrapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class CommandHelp
{
	@CommandName("!help: This help topic")
	@CommandTrigger("!help ")
	@HelpTopic("Shows a list of BridgeMPP Commands")
	public static String help()
	{
		return "This is the BridgeMPP Help Topic\nThis is a list of available Commands:\n" + getListOfCommands() + "\nType !helpcommand <command> for the help topic of a command";
	}
	
	@CommandName("!helpcommand: Detailed help for a Command")
	@CommandTrigger("!helpcommand")
	@HelpTopic("Show the help topic for a given BridgeMPP Command, along with the required Syntax")
	public static String help(String command)
	{
		return "Syntax: " + getSyntax(command) + "\nHelp: " + getHelp(command);
	}

	private static String getHelp(String command)
	{
		Method method = CommandWrapper.getCommand(command);
		if(method == null)
		{
			return "Command not found";
		}
		HelpTopic help = method.getAnnotation(HelpTopic.class);
		if(help == null)
		{
			return "Command does not have a help topic";
		}
		return help.value();
	}

	private static String getSyntax(String command)
	{
		Method method = CommandWrapper.getCommand(command);
		if(method == null)
		{
			return "Command not found";
		}
		String help = method.getName() + " ";
		for(Parameter parameter : method.getParameters())
		{
			if(parameter.getType().getName().equalsIgnoreCase("bridgempp.Message"))
			{
				continue;
			}
			help += "<" +  parameter.getType().getSimpleName() + "> ";
		}
		return help.trim();
	}

	private static String getListOfCommands()
	{
		String commands = "";
		for(Class<?> commandClass : CommandWrapper.listCommands())
		{
			for(Method command : commandClass.getMethods())
			{
				CommandName annotation = command.getAnnotation(CommandName.class);
				if(annotation != null)
				{
					commands += annotation.value() + "\n";
				}
			}
		}
		return commands.trim();
	}
	
}
