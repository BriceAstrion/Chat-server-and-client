package shared;

public class JoinedMessage implements Response{
    int code;
    String status;
    String username;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCode(int code) { this.code = code; }

    public void setStatus(String status) {
        this.status = status;
    }
}
