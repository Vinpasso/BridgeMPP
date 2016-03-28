package bridgempp.data.processing;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import bridgempp.ShadowManager;

public class DataProcessor
{
	private static ScheduledThreadPoolExecutor executor;
	
	static
	{
		executor = new ScheduledThreadPoolExecutor(2);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
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
		executor.shutdown();
		try
		{
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e)
		{
			ShadowManager.log(Level.INFO, "Failed to await termination of schedule: ", e);
		}
	}
}
