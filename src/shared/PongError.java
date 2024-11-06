package shared;

public class PongError implements Response{

    int code;
    String status;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setCode(int code) { this.code = code; }

}
