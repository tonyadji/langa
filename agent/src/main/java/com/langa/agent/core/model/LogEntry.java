package com.langa.agent.core.model;


public class LogEntry {

    private String message;
    private String level;
    private String loggerName;
    private String timestamp;

    public LogEntry(String message, String level, String loggerName, String timestamp) {
        this.message = message;
        this.level = level;
        this.loggerName = loggerName;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
