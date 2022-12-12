package server.entities;

public interface LockableUserDao extends UserDao {
    /**
     * Place a lock on a particular username. Whenever a username is locked, the user with
     * that username cannot be edited or a new user with that username cannot be
     * created unless it is unlocked again. A lock doesn't affect the ability to read.
     *
     * @param username the username to be locked
     * @param locker unique identifier of the entity that places the lock on this user.
     * @return whether the username was successfully locked by the locker.
     */
    boolean lock(String username, String locker);

    /**
     * Unlocks a username. Whenever a username is unlocked, a user with that username can be created/edited.
     * A username can be unlocked only by the entity that originally placed the lock. Returns false even if
     * the username was already unlocked.
     *
     * @param username username to be unlocked
     * @param unlocker unique identifier of the entity that originally locked the username.
     * @return whether the username was unlocked.
     */
    boolean unlock(String username, String unlocker);
}
