package shared;

public class DisconnectMessage implements Response{

    int code;
    String status;

    public DisconnectMessage() {
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }
}
