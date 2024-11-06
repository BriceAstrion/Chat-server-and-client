package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


public final class StartGuessingGameResp {
    private String status;
    private String message;
    private int code;

    public StartGuessingGameResp() { }

    @JsonCreator
    public StartGuessingGameResp(@JsonProperty("status") String status,
                                 @JsonProperty("code") int code,
                                 @JsonProperty("message") String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public String status() {
        return status;
    }

    public String message() {
        return message;
    }

    public int code() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StartGuessingGameResp) obj;
        return Objects.equals(this.status, that.status) &&
                Objects.equals(this.message, that.message) &&
                this.code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message, code);
    }

    @Override
    public String toString() {
        return "StartGuessingGameResp[" +
                "status=" + status + ", " +
                "message=" + message + ", " +
                "code=" + code + ']';
    }


}
