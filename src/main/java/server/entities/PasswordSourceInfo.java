package server.entities;

/**
 * Store information of the initial source of a password. Also stores a secret
 * used to modify the record.
 */
public class PasswordSourceInfo {
    public String secret;
    public Server server;
}
