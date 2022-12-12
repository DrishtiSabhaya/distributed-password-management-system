package server.entities;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    public String name;
    public String password;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User(User otherUser) {
        this.name = otherUser.name;
        this.password = otherUser.password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        return (Objects.equals(this.name, other.name) || this.name.equals(other.name))
                && (Objects.equals(this.password, other.password) || this.password.equals(other.password));
    }
}
