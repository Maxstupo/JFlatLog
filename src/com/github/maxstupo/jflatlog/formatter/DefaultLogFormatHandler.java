package com.github.maxstupo.jflatlog.formatter;

import com.github.maxstupo.jflatlog.JFlatLog;

/**
 * This class is the default formatter used by {@link JFlatLog} it returns the console version of the log message.
 * 
 * @author Maxstupo
 */
public class DefaultLogFormatHandler implements ILogFormatHandler {

    @Override
    public String format(String consoleVersion, String timestamp, String tag, String category, String message, String exceptionString) {
        return consoleVersion;
    }

}
