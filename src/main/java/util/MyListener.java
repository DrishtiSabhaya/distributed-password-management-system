package util;

import java.rmi.RemoteException;

import javax.jms.*;

import server.coordinator.MutableCoordinator;
import server.entities.ModificationOperation;
import server.entities.ServerListModificationRequest;

/**
 * An implementation of MessageListener which helps create async calls to add or remove server.
 */
public class MyListener implements MessageListener {
    private final MutableCoordinator coordinator;

    public MyListener(MutableCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void onMessage(Message m) {
        try {
            ObjectMessage msg = (ObjectMessage) m;
            ServerListModificationRequest req = (ServerListModificationRequest) msg.getObject();
            if (req.op == ModificationOperation.Add) {
                this.coordinator.addServer(req.server.host, req.server.port, req.server.name);
            } else if (req.op == ModificationOperation.Remove) {
                this.coordinator.removeServer(req.server.name);
            }
        } catch (JMSException e) {
            System.out.println(e);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}