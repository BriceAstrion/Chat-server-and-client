package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


public final class JoinGameResp {
    private final String status;
    private final int code;
    private final String message;

    @JsonCreator
    public JoinGameResp(@JsonProperty("status") String status,
                        @JsonProperty("code") int code,
                        @JsonProperty("message") String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public String status() {
        return status;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (JoinGameResp) obj;
        return Objects.equals(this.status, that.status) &&
                this.code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, code);
    }

    @Override
    public String toString() {
        return "JoinGameResp[" +
                "status=" + status + ", " +
                "code=" + code + ']';
    }


}
