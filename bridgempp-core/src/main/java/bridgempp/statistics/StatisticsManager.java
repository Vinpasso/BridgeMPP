package bridgempp.statistics;

import java.util.Iterator;
import java.util.logging.Level;

import bridgempp.data.DataManager;
import bridgempp.log.Log;
import bridgempp.message.Message;
import bridgempp.util.TimeUtil;

public class StatisticsManager
{

	private static StatisticStore statisticStore;

	public static synchronized void processMessage(Message message)
	{
		statisticStore.messageCount++;
	}

	public static String getStatistics()
	{
		String statisticsString = "\n";
		statisticsString += "Install Date: " + TimeUtil.timeFormat(statisticStore.installDate) + "\n";
		statisticsString += "Uptime: " + TimeUtil.timeDeltaNow(statisticStore.lastStartup) + "\n";
		statisticsString += "Total Uptime: " + TimeUtil.timeDelta(0, getCurrentAndTotalUptime()) + "\n";
		statisticsString += "Availability: " + (getCurrentAndTotalUptime()*100) / getTimeSinceInstall() + "%\n";
		statisticsString += "Messages sent: " + statisticStore.messageCount;
		return statisticsString;
	}

	private static long getTimeSinceInstall()
	{
		return System.currentTimeMillis() - statisticStore.installDate;
	}
	
	private static long getCurrentAndTotalUptime()
	{
		return statisticStore.uptime + (System.currentTimeMillis() - statisticStore.lastStartup);
	}

	private static void startedUp()
	{
		if (statisticStore.installDate == 0)
		{
			Log.log(Level.WARNING, "No existing Statistics found. Creating new Statistics Store");
			statisticStore.installDate = System.currentTimeMillis();
		}
		statisticStore.lastStartup = System.currentTimeMillis();
	}

	private static void shuttingDown()
	{
		if(statisticStore == null)
		{
			return;
		}
		statisticStore.uptime += System.currentTimeMillis() - statisticStore.lastStartup;
	}

	public static void loadStatistics()
	{
		Log.log(Level.INFO, "Loading Statistics...");
		Iterator<StatisticStore> iterator = DataManager.getStatisticsStore().iterator();
		if (iterator.hasNext())
		{
			statisticStore = iterator.next();
		} else
		{
			statisticStore = new StatisticStore();
		}
		startedUp();
		Log.log(Level.INFO, "Loaded Statistics");
	}

	public static void saveStatistics()
	{
		Log.log(Level.INFO, "Saving Statistics...");
		shuttingDown();
		DataManager.saveStatisticsStore(statisticStore);
		Log.log(Level.INFO, "Saved Statistics");
	}

}
