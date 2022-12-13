package server.coordinator;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import server.entities.CoordinatorResult;
import server.entities.Server;
import server.entities.Client;

/**
 * Represents a Coordinator for the 2PC algorithm. Servers call
 * methods on this coordinator to begin the execution of 2PC for a particular
 * operation.
 * Sends a prepare message to all servers, waits for the response. If any server
 * responds negatively, aborts the operation;
 * else asks all the servers to execute the commit.
 */
public interface Coordinator extends Remote {

    /**
     * Used to create a new user in the database. All the server
     * replicas contain a reference to the newly created user.
     * Fails if the username is not unique.
     * 
     * @param username unique identifier for the user
     * @param password hashed password
     * @return whether the signup operation was successful
     */
    boolean signUp(String username, String password) throws RemoteException;

    /**
     * Sync all users with the caller of this method. Goes through all the servers
     * and returns the 1st-non empty userlist it can find. In case all servers
     * have empty userlists, returns an empty list.
     * 
     * @return list of users
     */
    List<Client> syncUsers() throws RemoteException;

    /**
     * Used to get a password from the cluster of servers.
     * 
     * @param password unique name of the password.
     * @return the document in question and a secret to later send with the ack.
     */
    CoordinatorResult GetPassword(Client user, String password) throws RemoteException;

    /**
     * An acknowledgement of the password that was queried is received. It
     * internally triggers a deletion of that password from its previous source.
     * 
     * @param password   unique password.
     * @param secret secret received with the result of
     *               {@link #GetPassword(Client user, String password)}
     * @param server the server calling the function.
     * @return True if accepted else false
     */
    Boolean GetPasswordAck(Client user, String password, String secret, Server server)
        throws RemoteException;

    /**
     * Initialize this coordinator by creating appropriate remote object
     * and setting up queue listener.
     */
    void init() throws RemoteException;
}
