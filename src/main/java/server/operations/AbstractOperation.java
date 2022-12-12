package server.operations;

import java.util.Random;

public abstract class AbstractOperation implements Operation {
    private final String id;
    private final static int MIN_ID = 100000000;
    private final static int MAX_ID = 999999999;

    protected AbstractOperation() {
        this("Operation");
    }

    protected AbstractOperation(String idPrefix) {
        this.id = idPrefix + "@" + getRandomIntInRange(MIN_ID, MAX_ID);
    }

    @Override
    public String getId() {
        return id;
    }

    private int getRandomIntInRange(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
}
