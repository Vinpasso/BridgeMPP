package bridgempp.command;

import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.message.Message;
import bridgempp.statistics.StatisticsManager;

public class CommandStatisticsOperations {

	@CommandName("!statistics: Show general Statistics")
	@CommandTrigger("!statistics")
	@HelpTopic("Show general BridgeMPP Server Statistics, such as messages sent")
	public static void cmdShowStatistics(Message message) {
    	message.getOrigin().sendOperatorMessage(StatisticsManager.getStatistics());		
	}
}
