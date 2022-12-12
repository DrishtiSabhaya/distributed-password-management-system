package util;

/**
 * Class that represents an implementation of the Logger interface for the
 * client computer.
 */
public class ClientLogger implements Logger{
    @Override
    public void log(String message) {
        System.out.println(String.format("Logging message to the client...\n%s. %s",
                message, System.currentTimeMillis()));
    }
}
