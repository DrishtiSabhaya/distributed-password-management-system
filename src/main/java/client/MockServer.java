package client;

import server.entities.User;
import server.server.ClientServer;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class MockServer implements ClientServer {
    Map<String, User> m = new HashMap<>();
    @Override
    public User signUp(String username, String password) throws RemoteException {
        User u = new User(username, password);
        m.put(username, u);
        return u;
    }

    @Override
    public User login(String username, String password) throws RemoteException {
        if (!m.containsKey(username))
            return null;
        if (!m.get(username).password.equals(password))
            return null;
        return m.get(username);
    }

    @Override
    public String GetPassword(User user, String name) throws RemoteException {
        return "Exists";
    }

    @Override
    public Boolean DeletePassword(User user, String name) throws RemoteException {
        return true;
    }

    @Override
    public Boolean PutPassword(User user, String name, String value) throws RemoteException {
        return true;
    }
}
