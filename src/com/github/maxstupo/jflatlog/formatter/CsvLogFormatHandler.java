package com.github.maxstupo.jflatlog.formatter;

/**
 * This class is a basic implementation of {@link ILogFormatHandler} formatting each log message into CSV.
 * 
 * @author Maxstupo
 */
public class CsvLogFormatHandler implements ILogFormatHandler {

    private final StringBuilder sb = new StringBuilder();

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
