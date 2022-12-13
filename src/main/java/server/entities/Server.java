package server.entities;

import java.io.Serializable;

/**
 * A server class whose attributes are name, host and port.
 */
public class Server implements Serializable {

    public String name;
    public String host;
    public int port;

    public Server(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }
}
