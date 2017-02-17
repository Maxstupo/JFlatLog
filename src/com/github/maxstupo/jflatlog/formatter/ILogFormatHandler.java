package com.github.maxstupo.jflatlog.formatter;

/**
 * This interface allows custom formating of log messages for log files (e.g. Writing each message to CSV)
 * 
 * @author Maxstupo
 */
public interface ILogFormatHandler {

    /**
     * Format the given log.
     * 
     * @param consoleVersion
     *            the console version of the log message.
     * @param timestamp
     *            the timestamp of the message.
     * @param tag
     *            the tag of the message.
     * @param category
     *            the category of the message.
     * @param message
     *            the message.
     * @param exceptionString
     *            the exception if given.
     * @return the formated string.
     */
    String format(String consoleVersion, String timestamp, String tag, String category, String message, String exceptionString);
}
