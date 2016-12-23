package com.github.maxstupo.jflatlog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.maxstupo.jflatlog.exception.AlreadyInitializedException;
import com.github.maxstupo.jflatlog.formatter.DefaultLogFormatHandler;
import com.github.maxstupo.jflatlog.formatter.LogFormatHandler;

/**
 * JFlatLog is a lightweight logger that supports both log-to-file and log-to-console.
 * 
 * @author Maxstupo
 */
public class JFlatLog {

    /** Disable logging. */
    public static final int LEVEL_OFF = 600;

    /** Critical errors. The application will no longer work correctly. */
    public static final int LEVEL_SEVERE = 500;

    /** Errors. The application may no longer work correctly. */
    public static final int LEVEL_ERROR = 400;

    /** Warnings. The application may no longer work correctly. */
    public static final int LEVEL_WARN = 300;

    /** Informative messages. Sometimes used for release build. */
    public static final int LEVEL_INFO = 200;

    /** Debug messages. This level is useful during development. */
    public static final int LEVEL_DEBUG = 100;

    /** A lot of information is logged. */
    public static final int LEVEL_FINE = 0;

    /** The tag/category for the logger, this is used for exceptions. */
    public static final String LOGGER_TAG = JFlatLog.class.getSimpleName();

    public static final String TIMESTAMP_FORMAT_LOGFILE = "yyyy-MM-dd_hh-mm-ss";
    public static final String TIMESTAMP_FORMAT_CONSOLE = "yyyy/MM/dd hh:mm:ss a";

    private static JFlatLog instance;

    private boolean isLoggingToFile = true;
    private int logLevel = LEVEL_INFO;
    private String logfileTimestampFormat = TIMESTAMP_FORMAT_LOGFILE;
    private String consoleTimestampFormat = TIMESTAMP_FORMAT_CONSOLE;

    private File logFile;
    private boolean appendLog;
    private boolean hasInitialized;
    private BufferedWriter bw;
    private final StringBuilder logMessageBuilder = new StringBuilder();

    private LogFormatHandler logFormatHandler = new DefaultLogFormatHandler();

    /**
     * Create a new logger without log-to-file capabilities.
     * <p>
     * Use {@link #initLogging(File, boolean)} to give log-to-file capabilities.
     */
    public JFlatLog() {
    }

    /**
     * Create a new logger with log-to-file capabilities.
     * <p>
     * <i>Note: This constructor is equivalent to calling the default {@link #JFlatLog() constructor} and then calling
     * {@link #initLogging(File, boolean)}</i>
     * 
     * @param logFile
     *            See {@link #getLogFile()} for details.
     * @param appendLog
     *            See {@link #isAppendLog()} for details.
     * 
     */
    public JFlatLog(File logFile, boolean appendLog) {
        initLogging(logFile, appendLog);
    }

    /**
     * Initializes logging to file capabilities.
     * 
     * @param logFile
     *            See {@link #getLogFile()} for details.
     * @param appendLog
     *            See {@link #isAppendLog()} for details.
     * 
     */
    public void initLogging(File logFile, boolean appendLog) {
        if (hasInitialized)
            throw new AlreadyInitializedException("initLogging can only be called once.");

        this.appendLog = appendLog;
        this.logFile = logFile;

        if (!hasLoggingCapabilities()) {
            setLoggingToFile(false);
            return;
        }

        File file = appendLog ? logFile : appendDateAndTime(logFile);
        try {
            bw = new BufferedWriter(new FileWriter(file, appendLog));
        } catch (IOException e) {
            log(LEVEL_ERROR, null, LOGGER_TAG, "Failed to init logging! -", e, true, (Object[]) null);
        }

        hasInitialized = true;
    }

    public void log(int level, String tag, String category, String message, Throwable ex, boolean disableLogToFile, Object... args) {
        if (logLevel > level)
            return;
        logMessageBuilder.delete(0, logMessageBuilder.length());

        // Append the timestamp.
        String timestamp = getLogTimestamp();
        logMessageBuilder.append("[").append(timestamp).append("] ");

        // Append the log level tag.
        if (tag != null && !tag.isEmpty())
            logMessageBuilder.append("[").append(tag).append("]");

        // Append the category.
        if (category != null && !category.isEmpty()) {
            logMessageBuilder.append(" [").append(category).append("]: ");
        } else {
            category = null; // Make sure category is null for formatter
            logMessageBuilder.append(": ");
        }

        // Append the message.
        String formattedMessage = replaceReferences(message, args);
        logMessageBuilder.append(formattedMessage);

        // Append the exception if one exists.
        String exceptionString = null;
        if (ex != null) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            exceptionString = writer.toString().trim();
            logMessageBuilder.append(" - ").append(exceptionString);
        }

        // Print message to console.
        System.out.println(logMessageBuilder.toString());

        if (isLoggingToFile() && !disableLogToFile) {
            String formatedMessage = (logFormatHandler == null) ? logMessageBuilder.toString() : logFormatHandler.format(logMessageBuilder.toString(), timestamp, tag, category, formattedMessage, exceptionString);
            logToFile(formatedMessage);
        }
    }

    private void logToFile(String message) {
        if (!hasLoggingCapabilities())
            return;

        try {
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            log(LEVEL_ERROR, null, LOGGER_TAG, "Failed to write log message to log file! -", e, true, (Object[]) null);
        }
    }

    private String replaceReferences(String msg, Object... args) {
        if (args == null)
            return msg;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null)
                arg = "null";
            msg = msg.replace("{" + i + "}", arg.toString());
        }
        return msg;
    }

    /**
     * Close the writer to the log file. This call is ignored if the logger doesn't have {@link #hasLoggingCapabilities() logging capabilities}.
     */
    public void close() {
        if (!hasLoggingCapabilities())
            return;
        try {
            bw.close();
            bw = null;
            logFile = null;
            hasInitialized = false;
        } catch (IOException e) {
            log(LEVEL_ERROR, null, LOGGER_TAG, "Failed to close log file writer!", e, true, (Object[]) null);
        }
    }

    private String getLogTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(consoleTimestampFormat);
        return LocalDateTime.now().format(formatter);
    }

    private File appendDateAndTime(File file) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(logfileTimestampFormat);
        String dateTime = LocalDateTime.now().format(formatter);

        int indexOfDot = file.getName().lastIndexOf('.');
        String name = file.getName().substring(0, indexOfDot);
        String ext = file.getName().substring(indexOfDot);
        return new File(file.getParentFile(), name + "_" + dateTime + ext);
    }

    // @region ****************************** FINE LOG METHODS ****************************************
    public void fine(String category, String message, Object... objs) {
        fine(category, message, null, objs);
    }

    public void fine(String category, String message) {
        fine(category, message, null, (Object[]) null);
    }

    public void fine(String category, String message, Throwable ex) {
        fine(category, message, ex, (Object[]) null);
    }

    public void fine(String category, String message, Throwable ex, Object... objs) {
        log(LEVEL_FINE, "FINE", category, message, ex, false, objs);
    }
    // @endregion *************************************************************************************

    // @region ****************************** DEBUG LOG METHODS ***************************************
    public void debug(String category, String message, Object... objs) {
        debug(category, message, null, objs);
    }

    public void debug(String category, String message) {
        debug(category, message, null, (Object[]) null);
    }

    public void debug(String category, String message, Throwable ex) {
        debug(category, message, ex, (Object[]) null);
    }

    public void debug(String category, String message, Throwable ex, Object... objs) {
        log(LEVEL_DEBUG, "DEBUG", category, message, ex, false, objs);
    }
    // @endregion *************************************************************************************

    // @region ****************************** INFO LOG METHODS ****************************************
    public void info(String category, String message, Object... objs) {
        info(category, message, null, objs);
    }

    public void info(String category, String message) {
        info(category, message, null, (Object[]) null);
    }

    public void info(String category, String message, Throwable ex) {
        info(category, message, ex, (Object[]) null);
    }

    public void info(String category, String message, Throwable ex, Object... objs) {
        log(LEVEL_INFO, "INFO", category, message, ex, false, objs);
    }
    // @endregion *************************************************************************************

    // @region ****************************** WARN LOG METHODS ****************************************
    public void warn(String category, String message, Object... objs) {
        warn(category, message, null, objs);
    }

    public void warn(String category, String message) {
        warn(category, message, null, (Object[]) null);
    }

    public void warn(String category, String message, Throwable ex) {
        warn(category, message, ex, (Object[]) null);
    }

    public void warn(String category, String message, Throwable ex, Object... objs) {
        log(LEVEL_WARN, "WARN", category, message, ex, false, objs);
    }
    // @endregion *************************************************************************************

    // @region ****************************** ERROR LOG METHODS ***************************************
    public void error(String category, String message, Object... objs) {
        error(category, message, null, objs);
    }

    public void error(String category, String message) {
        error(category, message, null, (Object[]) null);
    }

    public void error(String category, String message, Throwable ex) {
        error(category, message, ex, (Object[]) null);
    }

    public void error(String category, String message, Throwable ex, Object... objs) {
        log(LEVEL_ERROR, "ERROR", category, message, ex, false, objs);
    }
    // @endregion ************************************************************************************

    // @region ****************************** SEVERE LOG METHODS **************************************
    public void severe(String category, String message, Object... objs) {
        severe(category, message, null, objs);
    }

    public void severe(String category, String message) {
        severe(category, message, null, (Object[]) null);
    }

    public void severe(String category, String message, Throwable ex) {
        severe(category, message, ex, (Object[]) null);
    }

    public void severe(String category, String message, Throwable ex, Object... objs) {
        log(LEVEL_SEVERE, "SEVERE", category, message, ex, false, objs);
    }
    // @endregion *************************************************************************************

    /**
     * If the given value is true the logger will write, all log messages to file, only if this logger {@link #hasLoggingCapabilities()} if it
     * doesn't, use {@link #initLogging(File, boolean)} first.
     * 
     * @param isLoggingToFile
     *            If true will log messages to file.
     */
    public void setLoggingToFile(boolean isLoggingToFile) {
        this.isLoggingToFile = isLoggingToFile;
    }

    public void setLogfileTimestampFormat(String logfileTimestampFormat) {
        this.logfileTimestampFormat = logfileTimestampFormat;
    }

    public void setConsoleTimestampFormat(String consoleTimestampFormat) {
        this.consoleTimestampFormat = consoleTimestampFormat;
    }

    /**
     * The format handler for logging to a file. This allows log messages to be logged in different formats.
     * 
     * @param logFormatHandler
     *            The interface for formatting messages that get logged to file.
     */
    public void setLogFormatHandler(LogFormatHandler logFormatHandler) {
        this.logFormatHandler = logFormatHandler;
    }

    /**
     * Set the logging level, any level bellow this value will not be logged.
     * 
     * @param logLevel
     *            the log level.
     */
    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * If true the logger will append log entries into the file, instead of creating a new file with a timestamp. This variable is readonly and is set
     * from {@link #initLogging(File, boolean)} or {@link #JFlatLog(File, boolean)}
     * 
     * @return true if the logger will append log entries into the file.
     */
    public boolean isAppendLog() {
        return appendLog;
    }

    public boolean isLoggingToFile() {
        return isLoggingToFile;
    }

    /**
     * Returns true if {@link #initLogging(File, boolean)} method has been called.
     * 
     * @return true if {@link #initLogging(File, boolean)} method has been called.
     */
    public boolean hasLoggingCapabilities() {
        return logFile != null;
    }

    /**
     * @return See {@link #setLogFormatHandler(LogFormatHandler)} for details.
     */
    public LogFormatHandler getLogFormatHandler() {
        return logFormatHandler;
    }

    public String getLogfileTimestampFormat() {
        return logfileTimestampFormat;
    }

    public String getConsoleTimestampFormat() {
        return consoleTimestampFormat;
    }

    /**
     * @return See {@link #setLogLevel(int)} for details.
     */
    public int getLogLevel() {
        return logLevel;
    }

    /**
     * The file path the logger will log to.
     * 
     * @return a file object where the logger will be logging to.
     */
    public File getLogFile() {
        return logFile;
    }

    /**
     * Returns a logger for logging to console only, use {@link #initLogging(File, boolean)} to allow this instance to log-to-file.
     * 
     * @return a logger instance.
     * @see #hasLoggingCapabilities()
     */
    public static final JFlatLog get() {
        if (instance == null)
            instance = new JFlatLog();
        return instance;
    }

}
