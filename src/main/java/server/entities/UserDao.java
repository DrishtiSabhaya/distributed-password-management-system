package server.entities;

import java.util.List;

/**
 * Represents a set of operations that can be performed on user database.
 */
public interface UserDao {

    /**
     * Checks if a user with the given username already exists in the Database.
     *
     * @param username unique id for the user.
     * @return whether a user with given username exists in our db
     */
    boolean exists(String username);

    /**
     * Checks if the given user exists in the database.
     * @param user the user to be checked
     * @return whether the user exists in the db
     */
    boolean exists(User user);

    /**
     * Adds a new user to the database.
     * @param username username of the new user
     * @param password password of the new user
     * @return the newly created user or null if there was an error while creating the user (like the user already exists)
     */
    User add(String username, String password);

    /**
     * Get the user identified with the given username and password
     * @param username username of the user
     * @param password password of the user
     * @return the user or null if such a user doesn't exist.
     */
    User find(String username, String password);

    /**
     * Get all the users
     */
    List<User> findAll();
}
