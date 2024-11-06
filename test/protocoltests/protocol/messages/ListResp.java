package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ListResp {
    private int code;
    private String status;
    private String message;
    private List<String> users;

    public ListResp() {}

    public ListResp(int code, String status, String message, List<String> users) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.users = users;
    }

    @Override
    public String toString() {
        return "ListResp[" +
                "code=" + code + ", " +
                "status=" + status + ", " +
                "message=" + message + ", " +
                "users=" + users + ']';
    }

    public int code() {
        return code;
    }

    public String status() {
        return status;
    }

    public String message() {
        return message;
    }

    public List<String> users() {
        return users;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ListResp) obj;
        return this.code == that.code &&
                Objects.equals(this.status, that.status) &&
                Objects.equals(this.message, that.message) &&
                Objects.equals(this.users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, status, message, users);
    }


}
