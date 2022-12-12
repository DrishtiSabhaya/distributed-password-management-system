package server.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserMapDao implements UserDao {
    private final Map<String, User> users;

    public UserMapDao() {
        this.users = new ConcurrentHashMap<>();
    }

    @Override
    public boolean exists(String username) {
        return this.users.containsKey(username);
    }

    @Override
    public boolean exists(User user) {
        if (user == null) return false;

        if (!this.exists(user.name)) return false;

        return user.equals(this.users.get(user.name));
    }

    @Override
    public User add(String username, String password) {

        if (username == null
            || username.isEmpty()
            || password == null
            || password.isEmpty()) return null;

        if (this.exists(username)) return null;

        this.users.put(username, new User(username, password));

        return new User(this.users.get(username));
    }

    @Override
    public User find(String username, String password) {
        if (username == null
            || username.isEmpty()
            || password == null
            || password.isEmpty()) return null;

        if (!this.exists(new User(username, password))) return null;

        return new User(this.users.get(username));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(this.users.values());
    }
}
