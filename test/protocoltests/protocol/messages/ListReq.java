package protocoltests.protocol.messages;

public record ListReq(int code, String status, String message) {

    @Override
    public String toString() {
        return "ListReq[" +
                "code=" + code + ", " +
                "status=" + status + ", " +
                "message=" + message + ']';
    }


}
