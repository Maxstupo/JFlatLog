package com.github.maxstupo.jflatlog.formatter;

/**
 *
 * @author Maxstupo
 */
public class CsvLogFormatHandler implements LogFormatHandler {

	private final StringBuilder sb = new StringBuilder();

	public CsvLogFormatHandler() {
	}

	@Override
	public String format(String consoleVersion, String timestamp, String tag, String category, String message, String exceptionString) {
		sb.delete(0, sb.length());

		sb.append("\"").append(timestamp).append("\"").append(",");
		sb.append("\"").append(tag).append("\"").append(",");
		sb.append("\"").append(category).append("\"").append(",");
		sb.append("\"").append(message).append("\"").append(",");
		sb.append("\"").append(exceptionString).append("\"");
		return sb.toString().replace("\n", "");
	}

}
