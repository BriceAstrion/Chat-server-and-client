package shared;

public class PrivateMessageReq {

    public String receiver;

    public String message;

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
