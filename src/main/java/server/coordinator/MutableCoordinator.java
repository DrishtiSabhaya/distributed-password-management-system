package server.coordinator;

import java.rmi.RemoteException;

/**
 * As servers start to connect, we need a mechanism to let the coordinator know
 * about their existence. Same goes when the servers go offline.
 * This interface offers features to let the coordinator know of any changes to
 * the servers connected to it.
 */
public interface MutableCoordinator extends Coordinator {
    /**
     * Adds a new server to the list of servers after finding it in RMI registry.
     *
     * @param host host for the registry
     * @param port port of the registry
     * @param name name of the remote object
     */
    void addServer(String host, int port, String name) throws RemoteException;

    /**
     * Removes the server specified by the given name from the list of coordinator's
     * list of servers.
     *
     * @param name name of the server to be removed.
     */
    void removeServer(String name) throws RemoteException;
}
