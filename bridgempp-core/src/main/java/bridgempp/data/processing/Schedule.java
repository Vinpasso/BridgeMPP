package bridgempp.data.processing;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import bridgempp.log.Log;

public class Schedule
{
	private static ScheduledThreadPoolExecutor executor;
	
	static
	{
		Log.log(Level.INFO, "Starting the Scheduler's schedule...");
		ThreadFactory factory = new ThreadFactory() {
			private volatile int number = 0;
			
			@Override
			public Thread newThread(Runnable r)
			{
				return new Thread(r, "BridgeMPP Schedule Executor #" + number++);
			}
		};
		executor = new ScheduledThreadPoolExecutor(2, factory);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		Log.log(Level.INFO, "Started the Scheduler's schedule...");
	}
	
	public static void schedule(Runnable runnable)
	{
		executor.submit(runnable);
	}
	
	public static <T> Future<T> schedule(Callable<T> callable)
	{
		return executor.submit(callable);
	}
	
	public static Future<?> scheduleOnce(Runnable runnable, long delay, TimeUnit unit)
	{
		return executor.schedule(runnable, delay, unit);
	}
	
	public static <T> Future<T> scheduleOnce(Callable<T> callable, long delay, TimeUnit unit)
	{
		return executor.schedule(callable, delay, unit);
	}
	
	public static Future<?> scheduleRepeatWithPeriod(Runnable runnable, long initialDelay, long periodDelay, TimeUnit unit)
	{
		return executor.scheduleAtFixedRate(runnable, initialDelay, periodDelay, unit);
	}
	
	public static Future<?> scheduleRepeatWithDelay(Runnable runnable, long initialDelay, long periodDelay, TimeUnit unit)
	{
		return executor.scheduleWithFixedDelay(runnable, initialDelay, periodDelay, unit);
	}
	
	public static void shutdownSynchronous()
	{
		Log.log(Level.INFO, "Shutting down the schedule executor...");
		executor.shutdown();
		try
		{
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e)
		{
			Log.log(Level.SEVERE, "Failed to await termination of schedule: ", e);
		}
		Log.log(Level.INFO, "Schedule executor was shut down");
	}

	public static void shutdownAsynchronous()
	{
		Log.log(Level.INFO, "Shutting down the schedule executor...");
		executor.shutdown();
		Log.log(Level.INFO, "Schedule executor was shut down.");
	}

	public static boolean isShutdown()
	{
		return executor.isShutdown();
	}
}
