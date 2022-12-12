package server.entities;

import java.io.Serializable;

/**
 * A data transfer object indicating a modification to server list on coordinator.
 */
public class ServerListModificationRequest implements Serializable {
    public final ModificationOperation op;
    public final Server server;

    public ServerListModificationRequest(ModificationOperation op, Server server) {
        this.op = op;
        this.server = server;
    }
}
