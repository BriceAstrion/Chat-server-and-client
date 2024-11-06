package shared;

public class PrivateMessage implements Sendable{

    public String sender;

    public String message;

    public int code;

    public String status;
    public String receiver;


    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCode(int code) { this.code = code; }

    public int getCode() { return code; }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
