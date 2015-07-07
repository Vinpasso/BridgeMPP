package bridgempp.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.util.TimeUtil;

public class StatisticsManager {

	private static final String STATISTICS_FILE = "statistics.txt";
	private static Properties statistics;
	private static StatisticStore statisticStore;
	
	public static void processMessage(Message message)
	{
		statisticStore.messageCount++;
	}
	
	public static String getStatistics()
	{
		String statisticsString = "";
		statisticsString += "Install Date: " + TimeUtil.timeFormat(statisticStore.installDate) + "\n";
		statisticsString += "Uptime: " + TimeUtil.timeDeltaNow(statisticStore.lastStartup) + "\n";
		statisticsString += "Total Uptime: " + TimeUtil.timeDelta(0, statisticStore.uptime) + "\n";
		statisticsString += "Availability: " + (statisticStore.uptime)/(float)(System.currentTimeMillis() - statisticStore.installDate) + "\n";
		statisticsString += "Messages sent: " + statisticStore.messageCount;
		return statisticsString;
	}
	
	private static void startedUp() {
		if(statisticStore.installDate == 0)
		{
			ShadowManager.log(Level.WARNING, "No existing Statistics found. Creating new Statistics Store");
			statisticStore.installDate = System.currentTimeMillis();
		}
		statisticStore.lastStartup = System.currentTimeMillis();
	}
	
	private static void shuttingDown()
	{
		statisticStore.uptime += System.currentTimeMillis() - statisticStore.lastStartup;
	}
	
	public static void loadStatistics()
	{
		ShadowManager.log(Level.INFO, "Loading Statistics...");
		statistics = new Properties();
		File statisticsFile = new File(STATISTICS_FILE);
		if(!statisticsFile.exists())
		{
			try {
				statisticsFile.createNewFile();
			} catch (IOException e) {
				ShadowManager.log(Level.WARNING, "Could not create Statistics File at: " + STATISTICS_FILE, e);
			}
		}
		try {
			statistics.load(new FileReader(STATISTICS_FILE));
		} catch (FileNotFoundException e) {
			ShadowManager.log(Level.WARNING, "Could not find Statistics File at: " + STATISTICS_FILE, e);
		} catch (IOException e) {
			ShadowManager.log(Level.WARNING, "Could not read Statistics File at: " + STATISTICS_FILE, e);
		}
		statisticStore = new StatisticStore();
		statisticStore.load(statistics);
		startedUp();
		ShadowManager.log(Level.INFO, "Loaded Statistics");
	}

	public static void saveStatistics()
	{
		ShadowManager.log(Level.INFO, "Saving Statistics...");
		shuttingDown();
		statisticStore.store(statistics);
		try {
			statistics.store(new FileWriter(STATISTICS_FILE), "BridgeMPP Statistics File");
		} catch (IOException e) {
			ShadowManager.log(Level.WARNING, "Could not write Statistics File at: " + STATISTICS_FILE, e);
		}
		ShadowManager.log(Level.INFO, "Saved Statistics");
	}
	
}
