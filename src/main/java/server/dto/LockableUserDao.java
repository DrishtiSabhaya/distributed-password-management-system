package server.dto;

public interface LockableUserDao extends UserInfoDao {
    /**
     * A lock is placed on the username and hence it cannot be accessed by any other server.
     *
     * @param username the username of user
     * @param locker unique identifier of the entity that places the lock on this user.
     * @return whether the username was successfully locked by the locker.
     */
    boolean lock(String username, String locker);

    /**
     * Unlock a username after which it can be accessed.
     *
     * @param username username of the user
     * @param unlocker unique identifier of the entity that originally locked the username.
     * @return whether the username was unlocked.
     */
    boolean unlock(String username, String unlocker);
}
