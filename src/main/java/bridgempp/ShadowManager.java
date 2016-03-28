/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import bridgempp.data.Endpoint;

/**
 *
 * @author Vinpasso
 */
public class ShadowManager {
    static LogHandler handler;
    public static ArrayList<Endpoint> shadowEndpoints = new ArrayList<>();
    public static void initialize()
    {
        handler = new LogHandler();
        Logger.getGlobal().addHandler(handler);
    }
    
    public static void log(Level level, String message)
    {
        logWithStackTraceDepth(level, message, 3);
    }

	private static void logWithStackTraceDepth(Level level, String message, int depth)
	{
		LogRecord record = new LogRecord(level, message);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[depth];
        record.setSourceClassName(stackTraceElement.getClassName());
        record.setSourceMethodName(stackTraceElement.getMethodName());
        Logger.getGlobal().log(record);
	}
    
    public static void logAndReply(Level level, String message, Message bridgeMessage)
    {
    	logWithStackTraceDepth(level, message, 3);
    	bridgeMessage.getOrigin().sendOperatorMessage(message);
    }
    
    public static void logAndReply(Level level, String message, Message bridgeMessage, Exception e)
    {
    	logWithStackTraceDepth(level, message, 3);
    	e.printStackTrace();
    	bridgeMessage.getOrigin().sendOperatorMessage(message);
    }

    private static class LogHandler extends Handler {

        public LogHandler() {
        }

        @Override
        public void publish(LogRecord record) {
            String logRecord = record.toString();
            System.out.println(logRecord);
            for(int i = 0; i < shadowEndpoints.size(); i++)
            {
                shadowEndpoints.get(i).sendOperatorMessage(logRecord);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

	public static void log(Level level, String message, Throwable cause) {
		logWithStackTraceDepth(level, message, 3);
		cause.printStackTrace();
	}

	public static void info(String msg)
	{
		log(Level.INFO, msg);
	}
	
	public static void fatal(Exception e) {
		log(Level.SEVERE, "Fatal BridgeMPP Error\nWill respawn", e);
		BridgeMPP.exit();
	}

	public static void fatal(String msg) {
		log(Level.SEVERE, "Fatal BridgeMPP Error\nWill respawn\n" + msg);
		BridgeMPP.exit();		
	}

}
