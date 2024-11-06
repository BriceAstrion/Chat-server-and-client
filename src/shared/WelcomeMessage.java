package shared;


public class WelcomeMessage implements Response{

    private String message;
    private String status;
    private int code;

    public WelcomeMessage() {
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
