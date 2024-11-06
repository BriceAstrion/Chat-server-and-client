package protocoltests.protocol.messages;

import java.util.Objects;

public final class PongError {
    private int code;

    private String status;

    public PongError() {}

    public PongError(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int code() {
        return code;
    }

    public String status() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PongError) obj;
        return this.code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "PongError[" +
                "code=" + code + ']';
    }
}
