package server.coordinator;

import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;

import server.entities.CoordinatorResult;
import server.entities.PasswordSourceInfo;
import server.entities.LRU;
import server.entities.Server;
import server.entities.User;
import server.operations.Operation;
import server.operations.SignUpOperation;
import server.server.CoordinatorServer;
import util.MyListener;

public class CoordinatorImpl extends java.rmi.server.UnicastRemoteObject
    implements MutableCoordinator {

    // TODO: Log
    private final Map<String, PasswordSourceInfo> keyToPasswordSourceMap;
    private final LRU<String, PasswordSourceInfo> keyToPasswordCache;
    private final Map<String, Server> serverList;
    private final Map<String, ReentrantLock> keyLockMap;
    private final String ip;
    private final int port;

    private final int jmsPort;
    private static Logger log = Logger.getLogger(CoordinatorImpl.class.getName());
    private static final int CAPACITY = 10;

    public CoordinatorImpl(String ip, int port, int jmsPort) throws RemoteException {
        this.keyToPasswordSourceMap = new ConcurrentHashMap<>();
        this.keyToPasswordCache = new LRU<>(CAPACITY);
        this.serverList = new ConcurrentHashMap<>();
        this.keyLockMap = new ConcurrentHashMap<>();
        this.ip = ip;
        this.port = port;
        this.jmsPort = jmsPort;
    }

    @Override
    public boolean signUp(String username, String password) throws RemoteException {
        log.info(String.format("Received SignUp(%s, %s)", username, password));

        if (this.serverList.isEmpty()) {
            log.info("Server List empty.");
            return false;
        }

        final Operation signUpOperation = new SignUpOperation(username, password);

        // Ask all servers to prepare
        boolean allServersReady = prepareAllServers(signUpOperation);

        if (!allServersReady) {
            // At least one server is not ready. So abort the operation
            abortOnAllServers(signUpOperation);
        } else {
            // Ask servers to execute
            // TODO: Should we assume that the execute operation will pass?
            executeOnAllServers(signUpOperation);
        }

        return allServersReady;
    }

    @Override
    public List<User> syncUsers() throws RemoteException {

        List<User> allUsers = new ArrayList<>();

        if (this.serverList.isEmpty()) {
            return allUsers;
        }

        // find first non-empty user list and send
        for (Server server : this.serverList.values()) {
            try {
                CoordinatorServer c = getRemoteServer(server);
                List<User> tempUsers = c.getAllUsers();

                if (!tempUsers.isEmpty()) {
                    allUsers.addAll(tempUsers);
                    break;
                }
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }

        return allUsers;
    }

    @Override
    public void addServer(String host, int port, String name) throws RemoteException {
        synchronized ("serverList") {
            try {
                CoordinatorServer c = (CoordinatorServer) Naming
                        .lookup("rmi://" + host + ":" + port + "/Service");
                c.sendCoordinatorInfo(new Server("coord", this.ip, this.port));
                this.serverList.put(name, new Server(name, host, port));
                log.info(String.format("Server added %s(%s, %s)", name, host, port));
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                e.printStackTrace();
                log.error(String.format("Error in adding server %s %s/%d :: %s", name, host, port,
                        e.getMessage()));
            }
        }
    }

    @Override
    public void removeServer(String name) throws RemoteException {
        synchronized ("serverList") {
            Server s = this.serverList.remove(name);
            log.info(String.format("Server removed %s(%s, %s)", s.name, s.host, s.port));
        }
    }

    @Override
    public CoordinatorResult GetPassword(User user, String password) {
        log.info(String.format("GetPassword received for %s from %s", password, user.name));
        Lock l = this.getLock(password);
        l.lock();
        if (this.keyToPasswordCache.containsKey(password)) {
            PasswordSourceInfo info = this.keyToPasswordCache.get(password);
            try {
                CoordinatorServer c = getRemoteServer(info.server);
                CoordinatorResult res = new CoordinatorResult(c.getPassword(user, password),
                        UUID.randomUUID().toString(),
                        true);
                info.secret = res.secret;
                this.keyToPasswordSourceMap.put(password, info);
                this.keyToPasswordCache.remove(password);
                log.info(String.format("GetPassword found %s for %s on %s:%d and sent back",
                    res.result.value,
                    res.result.name, info.server.host, info.server.port));
                return res;
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                e.printStackTrace();
                l.unlock();
                this.keyLockMap.remove(password);
                log.error(String.format("Error in getting password %s of user %s :: %s",
                    password, user.name,
                        e.getMessage()));
                try {
                    this.removeServer(info.server.name);
                    log.info(String.format("Server %s removed", info.server.name));
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                    log.error(String.format("Error in removing server %s :: %s",
                        info.server.name, e.getMessage()));
                }
                return new CoordinatorResult();
            }
        }
        CoordinatorResult res = new CoordinatorResult();
        PasswordSourceInfo info = new PasswordSourceInfo();
        res.isSuccess = false;
        for (Server server : serverList.values()) {
            try {
                CoordinatorServer c = getRemoteServer(server);
                if (!c.hasPassword(user, password))
                    continue;
                res.result = c.getPassword(user, password);
                res.secret = UUID.randomUUID().toString();
                info.secret = res.secret;
                info.server = server;
                res.isSuccess = true;
                this.keyToPasswordSourceMap.put(password, info);
                this.keyToPasswordCache.remove(password);
                log.info(String.format("GetPassword found %s for %s on %s:%d and sent back",
                    res.result.value,
                    res.result.name,
                    server.host, server.port));
                break;
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                e.printStackTrace();
                log.error(String.format("Error in connecting to server %s :: %s",
                    server.name, e.getMessage()));
                try {
                    this.removeServer(server.name);
                    log.info(String.format("Server %s removed", server.name));
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                    log.error(String.format("Error in removing server %s :: %s",
                        info.server.name, e.getMessage()));
                }
            }
        }
        if (!res.isSuccess) {
            l.unlock();
            this.keyLockMap.remove(password);
        }
        return res;
    }

    @Override
    public Boolean GetPasswordAck(User user, String password, String secret, Server server) {
        log.info(String.format("GetPasswordAck received for %s from %s", password, user.name));
        if (!this.keyToPasswordSourceMap.containsKey(password)) {
            return false;
        }
        PasswordSourceInfo info = this.keyToPasswordSourceMap.get(password);
        if (!info.secret.equals(secret)) {
            return false;
        }
        try {
            CoordinatorServer c = getRemoteServer(info.server);
            if (!c.hasPassword(user, password) || !c.deletePassword(user, password)) {
                log.info(String.format("GetPasswordAck received for %s from %s not accepted",
                    password, user.name));
                return false;
            }
            this.keyToPasswordSourceMap.remove(password);
            info.server = server;
            this.keyToPasswordCache.put(password, info);
            Lock l = this.getLock(password);
            l.unlock();
            this.keyLockMap.remove(password);
            log.info(String.format("GetPasswordAck received for %s from %s accepted",
                password, user.name));
            return true;
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
            log.error(String.format("Error in connecting to server %s :: %s",
                info.server.name, e.getMessage()));
        }
        return false;
    }

    private synchronized ReentrantLock getLock(String name) {
        ReentrantLock lock = this.keyLockMap.get(name);
        if (lock == null) {
            lock = new ReentrantLock();
            this.keyLockMap.put(name, lock);
        }
        return lock;
    }

    private boolean prepareAllServers(Operation operation) {
        log.info("Preparing " + operation.toString());
        return this.serverList.values().stream().allMatch(server -> {
            try {
                CoordinatorServer c = getRemoteServer(server);
                return c.prepare(operation);
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                log.error(String.format("Error in sending prepare request to %s/%d :: %s",
                    server.host, server.port,
                    e.getMessage()));
                return false;
            }
        });// If any server responded negatively, return false, else true
    }

    private void abortOnAllServers(Operation operation) {
        log.info("Aborting " + operation.toString());
        this.serverList.values().forEach(server -> {
            try {
                CoordinatorServer c = getRemoteServer(server);
                c.abort(operation.getId());
            } catch (MalformedURLException | RemoteException | NotBoundException e) {
                log.error(String.format("Error in sending abort request to %s/%d :: %s",
                    server.host, server.port,
                    e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    private void executeOnAllServers(Operation operation) {
        log.info("Executing " + operation.toString());
        this.serverList.values().forEach(server -> {
            try {
                CoordinatorServer c = getRemoteServer(server);
                c.execute(operation.getId());
            } catch (MalformedURLException | NotBoundException | RemoteException e) {
                log.error(String.format("Error in sending execute request to %s/%d :: %s",
                    server.host, server.port,
                    e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    private CoordinatorServer getRemoteServer(Server serverInfo)
            throws MalformedURLException, NotBoundException, RemoteException {
        CoordinatorServer c = (CoordinatorServer) Naming
                .lookup(String.format("rmi://%s:%d/Service", serverInfo.host, serverInfo.port));
        return c;
    }

    public static void main(String[] args) {
        CoordinatorImpl c;
        try {
            log.error("Hello this is a debug message");
            c = new CoordinatorImpl("localhost", 50000, 50001);
            LocateRegistry.createRegistry(50000);
            Naming.rebind("rmi://localhost:" + 50000 + "/Service", c);
            c.startListener();
            System.out.println("Server started...");
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        try {
            LocateRegistry.createRegistry(this.port);
            Naming.rebind(String.format("rmi://%s:%d/Service", this.ip, this.port), this);
            this.startListener();
            log.info("Coordinator up and running...");
        } catch (RemoteException | MalformedURLException e) {
            log.error("Error initializing the coordinator: " + e.getMessage());
        }
    }

    private void startListener() {
        try {
            log.info(String.format("Staring listener on %d", port));
            String link = String.format("tcp://%s:%d", "localhost", this.jmsPort);
            BrokerService bService =
                BrokerFactory.createBroker(new URI("broker:(" + link + ")"));
            bService.start();
            ActiveMQConnectionFactory conF = new ActiveMQConnectionFactory(link);
            conF.setTrustAllPackages(true);
            ActiveMQConnection con = (ActiveMQConnection) conF.createConnection();
            String cliString = "client";
            con.setClientID(cliString);
            Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("server change queue");
            con.start();
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(new MyListener(this));
        } catch (JMSException e) {
            System.out.println("Error creating connection.");
            log.error(String.format("Error creating JMS connection :: %s", e.getMessage()));
        } catch (Exception e) {
            log.error(String.format("Unexpected error :: %s", e.getMessage()));
            System.out.println(e);
        }
    }

}
