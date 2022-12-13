package server.dto;

import server.entities.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserInfoMapInfoDao implements UserInfoDao {
    private final Map<String, Client> users;

    public UserInfoMapInfoDao() {
        this.users = new ConcurrentHashMap<>();
    }

    @Override
    public boolean checkUserExists(String username) {
        return this.users.containsKey(username);
    }

    @Override
    public boolean checkUserExists(Client user) {
        if (user == null) return false;

        if (!this.checkUserExists(user.username)) return false;

        return user.equals(this.users.get(user.username));
    }

    @Override
    public Client addUser(String username, String password) {

        if (username == null
            || username.isEmpty()
            || password == null
            || password.isEmpty()) return null;

        if (this.checkUserExists(username)) return null;

        this.users.put(username, new Client(username, password));

        return new Client(this.users.get(username));
    }

    @Override
    public Client findUser(String username, String password) {
        if (username == null
            || username.isEmpty()
            || password == null
            || password.isEmpty()) return null;

        if (!this.checkUserExists(new Client(username, password))) return null;

        return new Client(this.users.get(username));
    }

    @Override
    public List<Client> findAllUser() {
        return new ArrayList<>(this.users.values());
    }
}
