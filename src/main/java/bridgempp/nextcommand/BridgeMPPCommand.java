package bridgempp.nextcommand;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import bridgempp.PermissionsManager.Permission;

public abstract class BridgeMPPCommand
{
	private Pattern callCommand;
	private Permission permission;
	private Method commandMethod;
	
	public BridgeMPPCommand()
	{
		
	}
	
	public Pattern getCallCommand()
	{
		return callCommand;
	}
	
	public Permission getRequiredPermissions()
	{
		return permission;
	}

	public Method getCommandMethod()
	{
		return commandMethod;
	}

	public abstract String getHelpTopic();
}
