package server.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import org.apache.log4j.Logger;
import server.coordinator.MutableCoordinator;
import server.dto.LockableUserDao;
import server.dto.LockableUserMapDao;
import server.entities.*;
import server.operations.Operation;
import util.Encryptor;

public class Server extends java.rmi.server.UnicastRemoteObject
    implements ClientServer, CoordinatorServer {

    private final Map<Client, Map<String, String>> map;
    private final Map<String, Operation> operationsById;
    private MutableCoordinator coordinator;
    private final LockableUserDao users;
    private String host;
    private Integer port;
    private final String jmsHost;
    private final int jmsPort;
    private final server.entities.Server self;

    private Encryptor encryptor;

    private static final Logger log = Logger.getLogger("Server");

    public Server(String ownHost, int ownPort, String cordHost, int cordPort, String name)
            throws MalformedURLException, RemoteException, NotBoundException {
        this.map = new ConcurrentHashMap<>();
        this.users = new LockableUserMapDao();
        this.encryptor =  new Encryptor();
        this.operationsById = new ConcurrentHashMap<>();
        this.jmsHost = cordHost;
        this.jmsPort = cordPort;
        LocateRegistry.createRegistry(ownPort);
        Naming.rebind("rmi://localhost:" + ownPort + "/Service", this);

        log.info("Server up and running: " + "rmi://localhost:" + ownPort + "/Service");

        this.addServerToCoordinator(ownHost, ownPort, name);
        this.self = new server.entities.Server(name, ownHost, ownPort);

    }

    @Override
    public boolean prepare(Operation operation) throws RemoteException {
        if (operation == null) {
            throw new IllegalArgumentException("The operation is null.");
        }

        log.info(String.format("Prepare : %s", operation));

        // First, save this operation
        this.operationsById.put(operation.getId(), operation);

        // then commit - place lock on the username
        boolean isCommitted = operation.commit(users);
        if (isCommitted) {
            log.info(String.format("Committed: %s", operation));
            return true;
        } else {
            log.info(String.format("Commit Failed: %s", operation));
            return false;
        }
    }

    @Override
    public void execute(String operationId) throws RemoteException {
        if (operationId == null || !this.operationsById.containsKey(operationId))
            return;

        Operation operation = this.operationsById.get(operationId);

        log.info(String.format("Execute : %s", operation));

        // No use of this operation anymore
        this.operationsById.remove(operationId);

        boolean isExecuted = operation.execute(users);
        if (isExecuted) {
            log.info(String.format("Executed: %s", operation));
        } else {
            log.info(String.format("Execution Failed: %s", operation));
        }
    }

    @Override
    public void abort(String operationId) throws RemoteException {
        if (operationId == null || !this.operationsById.containsKey(operationId))
            return;

        Operation operation = this.operationsById.get(operationId);

        log.info(String.format("Abort : %s", operation));

        // No use of this operation anymore
        this.operationsById.remove(operationId);

        operation.abort(users);
    }

    @Override
    public Result getPassword(Client user, String name) throws RemoteException {
        Result res = new Result(name, null);
        if (!this.users.checkUserExists(user)) {
            return res;
        }
        Map<String, String> map = this.map.get(user);
        if (map != null)
            res.pvalue = map.get(name);
        return res;
    }

    @Override
    public Boolean hasPassword(Client user, String name) throws RemoteException {
        if (!this.users.checkUserExists(user)) {
            return false;
        }
        Map<String, String> map = this.map.get(user);
        return map != null && map.containsKey(name);
    }

    @Override
    public Boolean deletePassword(Client user, String name) throws RemoteException {
        if (!this.users.checkUserExists(user)) {
            return false;
        }
        Map<String, String> map = this.map.get(user);
        return map != null && map.remove(name) != null;
    }

    @Override
    public Client signUp(String username, String password) throws RemoteException {
        boolean result = this.coordinator.signUp(username, password);
        if (result) {
            log.info("Successfully signed up new user.");
            return this.users.findUser(username, password);
        } else {
            log.info("Couldn't create a new user with these credentials.");
            return null;
        }
    }

    @Override
    public Client login(String username, String password) throws RemoteException {
        return this.users.findUser(username, password);
    }

    public static void main(String[] args) {
        try {
            Server s = new Server("localhost", 50002, "localhost",
                50001, "hi3");
            System.out.println("Server started...");
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String GetPassword(Client user, String name) throws RemoteException {
        log.info(String.format("GetPassword received for %s by %s", name, user.username));
        Result res = this.getPassword(user, name);
        if (res.pvalue != null) {
            log.info(String.format("Password %s got value %s for user %s", name, res.pvalue,
                user.username));
            res.pvalue = encryptor.decrypt(res.pvalue);

            return res.pvalue;
        }
        try {
            CoordinatorResult result = this.coordinator.GetPassword(user, name);
            if (result.isSuccess && this.coordinator.GetPasswordAck(user, name, result.secret,
                this.self)) {
                Map<String, String> uMap = this.map.getOrDefault(user, new ConcurrentHashMap<>());
                uMap.put(name, result.result.pvalue);
                this.map.put(user, uMap);
                log.info(String.format("Password %s got value %s for user %s", name,
                    result.result.pvalue, user.username));
                result.result.pvalue = encryptor.decrypt(result.result.pvalue);

                return result.result.pvalue;
            }
        } catch (RemoteException e) {
            log.error(String.format("Error in getting password %s for %s :: %s", name, user.username,
                e.getMessage()));
            e.printStackTrace();
        }
        log.info(String.format("Password %s not found for user %s", name, user.username));
        return null;
    }

    @Override
    public Boolean DeletePassword(Client user, String name) throws RemoteException {
        log.info(String.format("DeletePassword received for %s by %s", name, user.username));
        Map<String, String> map = this.map.get(user);
        if (map != null && this.deletePassword(user, name)) {
            log.info(String.format("Password %s by %s not deleted.", name, user.username));
            return true;
        }
        try {
            CoordinatorResult reasult = this.coordinator.GetPassword(user, name);
            if (reasult.isSuccess && this.coordinator.GetPasswordAck(user, name, reasult.secret,
                this.self)) {
                log.info(String.format("Password %s by %s was deleted.", name, user.username));
                return true;
            }
        } catch (RemoteException e) {
            log.error(String.format("Error in deleting password %s for %s :: %s", name,
                user.username, e.getMessage()));
            e.printStackTrace();
        }
        log.info(String.format("Password %s by %s not deleted.", name, user.username));
        return false;
    }

    @Override
    public Boolean PutPassword(Client user, String name, String value) throws RemoteException {
        Encryptor encryptor = new Encryptor();
        value = encryptor.encrypt(value);
        log.info(String.format("PutPassword received for %s by %s with value %s", name,
            user.username, value));
        if (!this.users.checkUserExists(user)) {
            log.info(String.format("Password %s by %s was not added.", name, user.username));
            return false;
        }
        Map<String, String> keyValueMap = this.map.get(user);
        if (keyValueMap == null) {
            keyValueMap = new ConcurrentHashMap<>();
            this.map.put(user, keyValueMap);
        }
        keyValueMap.put(name, value);
        log.info(String.format("Password %s by %s with value %s was added.", name,
            user.username, value));
        return true;
    }

    private void addServerToCoordinator(String host, int port, String name) {
        log.info("Adding server to coordinator");
        String link = String.format("tcp://%s:%d", this.jmsHost, this.jmsPort);
        ActiveMQConnectionFactory conF = new ActiveMQConnectionFactory(link);
        conF.setTrustAllPackages(true);
        try {
            ActiveMQConnection con = (ActiveMQConnection) conF.createConnection();
            Session session;
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("server change queue");
            MessageProducer sender = session.createProducer(queue);
            ObjectMessage obj = session.createObjectMessage
                (new ServerListOperation(ServerOperation.Add,
                    new server.entities.Server(name, host, port)));
            sender.send(obj, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY,
                Message.DEFAULT_TIME_TO_LIVE);
            sender.close();
            session.close();
            con.close();
        } catch (JMSException e) {
            log.error(String.format("Error in adding server to coordinator :: %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public void sendCoordinatorInfo(server.entities.Server coordinatorServer)
        throws RemoteException {
        this.host = coordinatorServer.host;
        this.port = coordinatorServer.port;
        try {
            this.coordinator = (MutableCoordinator) Naming
                    .lookup(String.format("rmi://%s:%d/Service", this.host, this.port));
            log.info("Server and coordinator synced.");

            // Sync all users
            List<Client> userList = this.coordinator.syncUsers();
            addUsersToDao(userList);
            log.info("Synced User DB");

        } catch (MalformedURLException | NotBoundException e) {
            log.error(String.format("Error in saving coordinator :: %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public List<Client> getAllUsers() throws RemoteException {
        return this.users.findAllUser();
    }

    private void addUsersToDao(List<Client> userList) {
        for (Client user : userList) {
            this.users.addUser(user.username, user.password);
        }
    }
}
