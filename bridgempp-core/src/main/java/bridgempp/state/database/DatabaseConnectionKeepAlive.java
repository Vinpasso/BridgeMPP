package bridgempp.state.database;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.data.processing.Schedule;
import bridgempp.log.Log;
import bridgempp.state.Event;
import bridgempp.state.EventListener;
import bridgempp.statistics.StatisticsManager;
import bridgempp.state.EventSubscribe;

@EventSubscribe(Event.BRIDGEMPP_STARTUP)
public class DatabaseConnectionKeepAlive implements EventListener<Void>
{

	@Override
	public void onEvent(Void eventMessage)
	{
		Schedule.scheduleRepeatWithPeriod(() -> {
			Log.log(Level.INFO, "Keeping database connection alive");
			try
			{
				StatisticsManager.saveStatistics();
			}
			catch(Exception e)
			{
				Log.log(Level.SEVERE, "Error while keeping database connection alive", e);
				BridgeMPP.exit();
			}
		}, 5, 5, TimeUnit.MINUTES); 
	}

}
