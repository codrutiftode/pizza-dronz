package uk.ac.ed.inf;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

/**
 * Logs information and errors to the console, together with the system time.
 * Used for debugging and error logging.
 */
public class CustomLogger {
    private static CustomLogger logger = null;
    private final Logger internalLogger;

    private CustomLogger(Logger logger) {
        this.internalLogger = logger;
    }

    public void log(String message) {
        this.internalLogger.log(Level.INFO, message);
    }

    public void error(String message) {
        this.internalLogger.log(Level.SEVERE, message);
    }

    private static class MyFormatter extends Formatter {
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_WHITE = "\u001B[37m";
        public static final String ANSI_RESET = "\u001B[0m";

        @Override
        public String format(LogRecord record) {
            String fullTimeStr = LocalDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_TIME);
            String timeStr = fullTimeStr.split("\\.")[0]; // Remove milliseconds

            StringBuilder builder = new StringBuilder();
            String colour = record.getLevel() == Level.INFO
                    ? ANSI_GREEN
                    : record.getLevel() == Level.SEVERE
                    ? ANSI_RED
                    : ANSI_WHITE;

            builder.append(colour);
            builder.append(timeStr);
            builder.append(" - ");
            builder.append(record.getMessage());
            builder.append("\n");
            builder.append(ANSI_RESET);
            return builder.toString();
        }
    }

    public static CustomLogger getLogger() {
        if (logger == null) {
            LogManager.getLogManager().reset();
            Logger newLogger = LogManager.getLogManager().getLogger("");
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new MyFormatter());
            newLogger.addHandler(consoleHandler);

            CustomLogger customLogger = new CustomLogger(newLogger);
            logger = customLogger;
            return customLogger;
        }
        return logger;
    }
}
