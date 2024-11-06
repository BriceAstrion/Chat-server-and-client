package shared;

public class DCSN implements Response{

    String reason, status;
    int code;

    public String getReason() {
        return reason;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
