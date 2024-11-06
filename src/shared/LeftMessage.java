package shared;

public class LeftMessage implements Response{

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
}
