package server.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * A client class which consists of the login information in the form of username and password.
 */
public class Client implements Serializable {

    public String username;
    public String password;

    public Client(String name, String password) {
        this.username = name;
        this.password = password;
    }

    public Client(Client otherUser) {
        this.username = otherUser.username;
        this.password = otherUser.password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.username, this.password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Client other = (Client) obj;
        return (Objects.equals(this.username, other.username) || this.username.equals(other.username))
                && (Objects.equals(this.password, other.password) || this.password.equals(other.password));
    }
}
