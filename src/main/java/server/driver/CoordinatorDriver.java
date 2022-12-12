package server.driver;

import server.coordinator.CoordinatorImpl;
import util.Utility;

import java.rmi.RemoteException;

public class CoordinatorDriver {

    /**
     * @param args e.g. -p 4444 -jp 5555
     */
    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Invalid number of arguments!");
            System.exit(1);
        }

        String host = "localhost"; //default host
        int port = -1;
        int jmsPort = -1;

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i].toLowerCase()) {
                case "-p":
                    String portStr = args[i + 1];
                    if (Utility.isValidPort(portStr)
                            && Utility.isAvailablePort(Integer.parseInt(portStr))) {
                        port = Integer.parseInt(portStr);
                    }
                    break;
                case "-jp":
                    String jmsPortStr = args[i + 1];
                    if (Utility.isValidPort(jmsPortStr)
                            && Utility.isAvailablePort(Integer.parseInt(jmsPortStr))) {
                        jmsPort = Integer.parseInt(jmsPortStr);
                    }
                    break;
                default:
                    break;
            }
        }

        if (port == -1 || jmsPort == -1 || port == jmsPort) {
            System.out.println("Invalid arguments!");
            System.exit(1);
        }

        try {
            new CoordinatorImpl(host, port, jmsPort).init();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
