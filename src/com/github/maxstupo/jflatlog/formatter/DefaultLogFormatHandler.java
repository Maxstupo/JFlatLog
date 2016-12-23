package com.github.maxstupo.jflatlog.formatter;

/**
 *
 * @author Maxstupo
 */
public class DefaultLogFormatHandler implements LogFormatHandler {

	@Override
	public String format(String consoleVersion, String timestamp, String tag, String category, String message, String exceptionString) {
		return consoleVersion;
	}

}
