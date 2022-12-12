package util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Class for implementing utility methods that do not fit into a
 * defined category.
 */
public class Utility {
    private static final int minPort = 0;
    private static final int maxPort = 65535;

    /**
     * Verify that a port is valid.
     *
     * @param port port to be verified
     * @return true if port is valid or false otherwise
     */
    public static boolean isValidPort(String port) {
        boolean isValid = true;
        int portNumber = -1;
        try {
            portNumber = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            isValid = false;
        }
        if (portNumber < minPort || portNumber > maxPort) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Verify that a port is not currently used by another process.
     * Argument must be a valid port number.
     *
     * @param port valid port number
     * @return true if port is available or false otherwise
     */
    public  static boolean isAvailablePort(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
