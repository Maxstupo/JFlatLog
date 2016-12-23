package com.github.maxstupo.jflatlog.formatter;

/**
 *
 * @author Maxstupo
 */
public interface LogFormatHandler {
	String format(String consoleVersion, String timestamp, String tag, String category, String message, String exceptionString);
}
