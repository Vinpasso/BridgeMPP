package bridgempp.statistics;

import java.util.Properties;

public class StatisticStore {

	public int messageCount = 0;
	public long lastStartup = 0;
	public long uptime = 0;
	public long installDate = 0;
	
	public void load(Properties properties)
	{
		messageCount = (int) properties.getOrDefault("messagecount", 0);
		lastStartup = ((Number)properties.getOrDefault("laststartup", 0)).longValue();
		uptime = ((Number)properties.getOrDefault("uptime", 0)).longValue();
		installDate = ((Number)properties.getOrDefault("installdate", 0)).longValue();
	}
	
	public void store(Properties properties)
	{
		properties.put("messagecount", messageCount);
		properties.put("laststartup", lastStartup);
		properties.put("uptime", uptime);
		properties.put("installdate", installDate);
	}

}
