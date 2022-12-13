package server.entities;

import java.io.Serializable;

/**
 * A data transfer object indicating a modification to server list on coordinator.
 */
public class ServerListOperation implements Serializable {
    public final ServerOperation op;
    public final Server server;

    public ServerListOperation(ServerOperation op, Server server) {
        this.op = op;
        this.server = server;
    }
}
