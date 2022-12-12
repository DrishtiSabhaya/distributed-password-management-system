package server.operations;

import server.entities.LockableUserDao;

public class SignUpOperation extends AbstractOperation {

    private final String username;
    private final String password;

    public SignUpOperation(String username, String password) {
        super("SignUp");
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean commit(LockableUserDao userDao) {
        if (userDao == null) return false;

        if (this.username == null || this.username.isEmpty()
                || this.password == null || this.password.isEmpty()) return false;

        if (userDao.exists(this.username)) return false;

        return userDao.lock(this.username, this.getId());
    }

    @Override
    public boolean execute(LockableUserDao userDao) {
        if (userDao == null) {
            return false;
        }

        // Try to unlock resource. If unable, return false
        if (!userDao.unlock(this.username, this.getId())) return false;

        // Now that the resource is unlocked, add new user and return false if new user is null
        return userDao.add(username, password) != null;
    }

    @Override
    public void abort(LockableUserDao userDao) {
        if (userDao == null) return;

      userDao.unlock(this.username, this.getId());
    }

    @Override
    public String toString() {
        return String.format("%s::SignUp(%s,%s)", getId(), username, password);
    }
}
