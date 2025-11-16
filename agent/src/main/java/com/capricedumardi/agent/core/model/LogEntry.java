package com.capricedumardi.agent.core.model;


import java.util.Collections;
import java.util.Map;

public class LogEntry {

    private final String message;
    private final String level;
    private final String loggerName;
    private final String threadName;
    private final String stackTrace;
    private final Map<String, String> mdc;
    private final String timestamp;

    public LogEntry(String message, String level, String loggerName, String timestamp) {
        this(message, level, loggerName, timestamp, null, null, null);
    }

    public LogEntry(String message, String level, String loggerName, String timestamp, String threadName, String stackTrace, Map<String, String> mdc) {
        this.message = message;
        this.level = level;
        this.loggerName = loggerName;
        this.timestamp = timestamp;
        this.threadName = threadName;
        this.stackTrace = stackTrace;
        this.mdc = mdc != null ? Collections.unmodifiableMap(mdc) : null;
    }

    public String getMessage() {
        return message;
    }

    public String getLevel() {
        return level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getTimestamp() {
        return timestamp;
    }
    
    public String getThreadName() { return threadName; }
    
    public String getStackTrace() { return stackTrace; }
    
    public Map<String, String> getMdc() { return mdc; }

}
