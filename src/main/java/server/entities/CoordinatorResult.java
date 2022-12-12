package server.entities;

import java.io.Serializable;

/**
 * A wrapper around Result. Holds an indicator of success and a secret to send
 * acknowledge of reception to the coordinator.
 */
public class CoordinatorResult implements Serializable {
    public Result result;
    public String secret;
    public Boolean isSuccess;

    public CoordinatorResult() {
        this.result = null;
        this.secret = null;
        this.isSuccess = false;
    }

    public CoordinatorResult(Result result, String secret, Boolean isSuccess) {
        this.result = result;
        this.secret = secret;
        this.isSuccess = isSuccess;
    }
}
