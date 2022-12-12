package util;

/**
 * An interface that represents a logger for logging messages
 * to an output stream.
 */
public interface Logger {
    /**
     * Log message to output stream.
     *
     * @param message message to be logged
     */
    void log(String message);
}
