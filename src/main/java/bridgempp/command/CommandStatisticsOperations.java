package bridgempp.command;

import bridgempp.Message;
import bridgempp.statistics.StatisticsManager;

public class CommandStatisticsOperations {

	public static void cmdShowStatistics(Message message) {
    	message.getOrigin().sendOperatorMessage(StatisticsManager.getStatistics());		
	}
}
