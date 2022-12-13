package server.server;

import server.entities.Result;
import server.entities.Server;
import server.entities.Client;
import server.operations.Operation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Represents features of the Server exposed to the
 * {@link server.coordinator.Coordinator}
 */
public interface CoordinatorServer extends Remote {
    /**
     * Prepares the server to execute the commit.
     * 
     * @param operation the operation to be performed
     * @return whether the server prepared for the operation.
     */
    boolean prepare(Operation operation) throws RemoteException;

    /**
     * Executes the operation with the specified operationId on the datastore. MUST
     * prepare the operation first.
     * 
     * @param operationId unique identifier of the operation to be executed
     */
    void execute(String operationId) throws RemoteException;

    /**
     * Aborts the commit represented by the given operationId.
     * 
     * @param operationId unique identifier for the operation to be aborted
     */
    void abort(String operationId) throws RemoteException;

    /**
     * Returns a list of all the users registered with this server.
     * 
     * @return list of users
     */
    List<Client> getAllUsers() throws RemoteException;

    /**
     * Gets a password of a user if it exists.
     * 
     * @param user the owner of the password.
     * @param name the name of the password.
     * @return A result object with the password or null as value
     */
    Result getPassword(Client user, String name) throws RemoteException;

    /**
     * Checks if a password of a user exists.
     * 
     * @param user the owner of the password.
     * @param name the name of the password.
     * @return true if the server has the password for the user else false.
     */
    Boolean hasPassword(Client user, String name) throws RemoteException;

    /**
     * Deletes the password of a user if it exists.
     * 
     * @param user the owner of the password.
     * @param name the name of the password.
     * @return true if the server deletes the password for the user else false.
     */
    Boolean deletePassword(Client user, String name) throws RemoteException;

    /**
     * Sets the coordinator information for the server.
     * 
     * @param coordinatorServer information of the coordinator.
     */
    void sendCoordinatorInfo(Server coordinatorServer) throws RemoteException;
}
