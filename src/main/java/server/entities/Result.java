package server.entities;

import java.io.Serializable;

/**
 * Result returned when getting a document from another server
 */
public class Result implements Serializable {
    public String name;
    public String value;

    public Result(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
