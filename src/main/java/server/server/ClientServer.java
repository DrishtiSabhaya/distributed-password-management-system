package server.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import server.entities.Client;

/**
 * Represents the features of the server exposed to the client.
 */
public interface ClientServer extends Remote {

    /**
     * Create a new user with the given username and password
     * 
     * @param username unique identifier for the user
     * @return the newly created user
     */
    Client signUp(String username, String password) throws RemoteException;

    /**
     * Log in the existing user with the given username
     * 
     * @param username username of the user trying to log in
     * @param password password of the user
     * @return the logged-in user
     */
    Client login(String username, String password) throws RemoteException;

    /**
     * Gets a password of a user if it exists.
     * 
     * @param user the owner of the password.
     * @param name the name of the password.
     * @return the password if it exists else null.
     */
    String GetPassword(Client user, String name) throws RemoteException;

    /**
     * Deletes the password of a user if it exists.
     * 
     * @param user the owner of the password.
     * @param name the name of the password.
     * @return true if the server deletes the password for the user else false.
     */
    Boolean DeletePassword(Client user, String name) throws RemoteException;

    /**
     * Adds a password to a user.
     * 
     * @param user  the owner of the password.
     * @param name  the name of the password.
     * @param value value of the password
     * @return true if password is added.
     */
    Boolean PutPassword(Client user, String name, String value) throws RemoteException;
}
