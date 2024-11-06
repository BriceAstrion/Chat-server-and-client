package shared;

public class ByeResponse implements Response {
    private int code;
    private String status;

    public ByeResponse() { }

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
