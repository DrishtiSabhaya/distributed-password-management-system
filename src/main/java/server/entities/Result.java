package server.entities;

import java.io.Serializable;

/**
 * Result returned when getting a document from another server
 */
public class Result implements Serializable {
    public String pkey;
    public String pvalue;

    public Result(String name, String value) {
        this.pkey = name;
        this.pvalue = value;
    }
}
