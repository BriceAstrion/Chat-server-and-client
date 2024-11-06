package protocoltests.protocol.messages;


import java.util.Objects;

public final class Joined {
    private String username;
    private int code;
    private String status;

    public Joined() {}

    public Joined(String username, int code, String status) {
        this.username = username;
        this.code = code;
        this.status = status;
    }

    public String username() {
        return username;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Joined) obj;
        return Objects.equals(this.username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "Joined[" +
                "username=" + username + ']';
    }
}
