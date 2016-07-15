package bridgempp.state.database;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.processing.Schedule;
import bridgempp.state.EventListener;
import bridgempp.state.EventManager.Event;
import bridgempp.statistics.StatisticsManager;
import bridgempp.state.EventSubscribe;

@EventSubscribe(Event.BRIDGEMPP_STARTUP)
public class DatabaseConnectionKeepAlive extends EventListener<Void>
{

	@Override
	public void onEvent(Void eventMessage)
	{
		Schedule.scheduleRepeatWithPeriod(() -> {
			ShadowManager.log(Level.INFO, "Keeping database connection alive");
			try
			{
				StatisticsManager.saveStatistics();
			}
			catch(Exception e)
			{
				ShadowManager.fatal("Error while keeping database connection alive", e);
			}
		}, 5, 5, TimeUnit.MINUTES); 
	}

}
