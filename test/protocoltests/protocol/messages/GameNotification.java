package protocoltests.protocol.messages;

import java.util.Objects;

public final class GameNotification {
    private int code;
    private String message;
    private String status;

    public GameNotification() {}

    public GameNotification(int code, String message, String status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String toString() {
        return "GameNotification{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }

    public String status() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GameNotification) obj;
        return this.code == that.code &&
                Objects.equals(this.message, that.message) &&
                Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, status);
    }

}

