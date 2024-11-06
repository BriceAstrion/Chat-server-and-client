package shared;

public class GameNotification implements Response {

    public int code;

    public String message, status;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) { this.message = message; }

    public String getMessage() { return message; }

}
