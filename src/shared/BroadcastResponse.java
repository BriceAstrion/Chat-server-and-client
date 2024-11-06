package shared;

public class BroadcastResponse implements Response{

    int code ;
    String status;


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

