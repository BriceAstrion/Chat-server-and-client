package shared;


public class PrivateMessageResp implements Response {

    public int code;

    public String status;

    public String message;

    public PrivateMessageResp() { }

    public PrivateMessageResp(int code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

