package server.operations;

import java.util.Random;

/**
 * An abstract class which implements Operation and stores the operation's id.
 */
public abstract class AbstractOperation implements Operation {
    private final String operationId;
    private final static int MIN_ID = 100000000;
    private final static int MAX_ID = 999999999;

    protected AbstractOperation() {
        this("Operation");
    }

    protected AbstractOperation(String idPrefix) {
        Random rand = new Random();
        int num =  rand.nextInt((this.MAX_ID - this.MIN_ID) + 1) + this.MIN_ID;
        this.operationId = idPrefix + "@" + num;
    }

    @Override
    public String getId() {
        return this.operationId;
    }

}
