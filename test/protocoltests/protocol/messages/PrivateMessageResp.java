package protocoltests.protocol.messages;

import java.util.Objects;

public final class PrivateMessageResp {
    private int code;
    private String status, message;

    public PrivateMessageResp() {}

    public PrivateMessageResp(int code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    @Override
    public String toString() {
        return "PrivateMessageResp{" +
                "code=" + code +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public int code() {
        return code;
    }

    public String status() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateMessageResp that = (PrivateMessageResp) o;
        return code == that.code && Objects.equals(status, that.status) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, status, message);
    }

    public String getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
