package client;

import server.entities.Client;
import server.server.ClientServer;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class MockServer implements ClientServer {
    Map<String, Client> m = new HashMap<>();
    @Override
    public Client signUp(String username, String password) throws RemoteException {
        Client u = new Client(username, password);
        m.put(username, u);
        return u;
    }

    @Override
    public Client login(String username, String password) throws RemoteException {
        if (!m.containsKey(username))
            return null;
        if (!m.get(username).password.equals(password))
            return null;
        return m.get(username);
    }

    @Override
    public String GetPassword(Client user, String name) throws RemoteException {
        return "Exists";
    }

    @Override
    public Boolean DeletePassword(Client user, String name) throws RemoteException {
        return true;
    }

    @Override
    public Boolean PutPassword(Client user, String name, String value) throws RemoteException {
        return true;
    }
}
