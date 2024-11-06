package shared;

public class StartGuessingGame implements Response {
    public String status, message;

    public int code;

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
}
