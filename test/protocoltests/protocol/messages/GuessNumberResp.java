package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.*;

import java.util.Objects;

public final class GuessNumberResp {
    @JsonProperty("status")
    private final String status;
    @JsonProperty("message")
    private final String message;

    public GuessNumberResp(String status, String message) {
        this.status = status;
        this.message = message;
    }


    public String status() {
        return status;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return "GuessNumberResp[" +
                "status=" + status + ", " +
                "message=" + message + ']';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GuessNumberResp) obj;
        return Objects.equals(this.status, that.status) &&
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, message);
    }


}
