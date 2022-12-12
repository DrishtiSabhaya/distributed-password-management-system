package server.driver;

import server.server.Server;
import util.Utility;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class ServerDriver {
   // eg: -p 1111 -cp 2222 -ch localhost -n server
    public static void main(String[] args) {

        //TODO: Handle errors when disconnecting and reconnecting

        if (args.length < 8) {
            System.out.println("Invalid number of arguments");
            System.exit(1);
        }

        String ownHost = "localhost";

        int port = -1;
        String coordHost = "";
        int coordPort = -1;
        String name = "";

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i].toLowerCase()) {
                case "-p":
                    String portStr = args[i + 1];
                    if (Utility.isValidPort(portStr)
                            && Utility.isAvailablePort(Integer.parseInt(portStr))) {
                        port = Integer.parseInt(portStr);
                    }
                    break;
                case "-cp":
                    String coordPortStr = args[i + 1];
                    if (Utility.isValidPort(coordPortStr)) {
                        coordPort = Integer.parseInt(coordPortStr);
                    }
                    break;
                case "-ch":
                    coordHost = args[i + 1];
                    break;
                case "-n":
                    name = args[i + 1];
                    break;
                default:
                    break;
            }
        }


        if (port == -1 || coordPort == -1||
                name.isEmpty() || coordHost.isEmpty()) {
            System.out.println("Invalid arguments!");
            System.exit(1);
        }


        try {
            new Server(ownHost, port, coordHost, coordPort, name);
        } catch (MalformedURLException | NotBoundException | RemoteException e) {
            System.out.println("Encountered unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
