package server.dto;

import server.entities.Client;

import java.util.List;

/**
 * Represents a set of operations that can be performed on user database.
 */
public interface UserInfoDao {

    /**
     * Checks if the user is present in the datastore.
     *
     * @param username id of the user
     * @return user object
     */
    boolean checkUserExists(String username);

    /**
     * Checks if the user is present in the datastore.
     *
     * @param user user's object
     * @return user object
     */
    boolean checkUserExists(Client user);

    /**
     * Add a new user to the database.
     * @param username username of the user
     * @param password password of the user
     * @return user object
     */
    Client addUser(String username, String password);

    /**
     * Find the user that exists in datatsore and return it
     * @param username username of the user
     * @param password password of the user
     * @return user object
     */
    Client findUser(String username, String password);

    /**
     * Get all the users from the datatstore.
     */
    List<Client> findAllUser();
}
