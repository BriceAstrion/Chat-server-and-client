package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)

public final class LoginResp {
    private final String status;
    private final int code;

    @JsonCreator
    public LoginResp(@JsonProperty("status") String status, @JsonProperty("code") int code) {
        this.status = status;
        this.code = code;
    }

    @Override
    public String toString() {
        return "LoginResp[" +
                "status=" + status + ", " +
                "code=" + code + ']';
    }

    public String status() {
        return status;
    }

    public int code() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LoginResp) obj;
        return Objects.equals(this.status, that.status) &&
                this.code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, code);
    }

}
