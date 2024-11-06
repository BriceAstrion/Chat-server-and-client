package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;


public final class Welcome {
    private String msg;
    private int code;
    private String status;

    @JsonCreator
    public Welcome(@JsonProperty("message") String message,
                   @JsonProperty("status") String status,
                   @JsonProperty("code") int code) {
        this.msg = message;
        this.status = status;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Welcome) obj;
        return Objects.equals(this.msg, that.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg);
    }

    @Override
    public String toString() {
        return "Welcome[" +
                "msg=" + msg + ']';
    }
}
