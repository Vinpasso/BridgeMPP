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
        LogRecord record = new LogRecord(level, message);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        record.setSourceClassName(stackTraceElement.getClassName());
        record.setSourceMethodName(stackTraceElement.getMethodName());
        Logger.getGlobal().log(record);
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
                shadowEndpoints.get(i).sendMessage(logRecord);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
