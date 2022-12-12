package server.operations;

import server.entities.LockableUserDao;

import java.io.Serializable;

/**
 * An {@link Operation} is an abstraction that represents any operation that can be executed on
 * our datastore.
 */
public interface Operation extends Serializable {

    // Extends serializable because this object is going to be sent via RMI

    /**
     * Execute this operation on the password database.
     *
     * @return whether the execution was successful.
     */
    boolean execute(LockableUserDao userDao);

    /**
     * Commit this operation by placing appropriate locks and making validity checks.
     *
     * @param userDao the datastore
     * @return whether this operation was committed successfully
     */
    boolean commit(LockableUserDao userDao);

    /**
     * Abort the operation. Undo all changes that were made during the commit phase.
     *
     * @param userDao the datastore
     */
    void abort(LockableUserDao userDao);

    String getId();
}
