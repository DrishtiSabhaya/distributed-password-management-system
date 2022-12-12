package server.entities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LockableUserMapDao extends UserMapDao implements LockableUserDao {

    private final Map<String, String> lockedByLocker;

    public LockableUserMapDao() {
        super();
        this.lockedByLocker = new ConcurrentHashMap<>();
    }

    @Override
    public boolean lock(String username, String locker) {
        if (username == null || username.isEmpty()
                || locker == null || locker.isEmpty()) return false;

        // if the username is already locked, return false
        if (lockedByLocker.containsKey(username)) return false;

        this.lockedByLocker.put(username, locker);

        return true;
    }

    @Override
    public boolean unlock(String username, String unlocker) {
        if (username == null || username.isEmpty()
                || unlocker == null || unlocker.isEmpty()) return false;

        // If the username is already unlocked, return false.
        if (!lockedByLocker.containsKey(username)) return false;

        // if the unlocker is same as the locker, unlock and return true.
        if (unlocker.equals(lockedByLocker.get(username))) {
            lockedByLocker.remove(username);
            return true;
        }

        return false;
    }

    @Override
    public User add(String username, String password) {
        // If the username is locked, then return null.
        if (username != null && lockedByLocker.containsKey(username)) return null;

        return super.add(username, password);
    }
}
