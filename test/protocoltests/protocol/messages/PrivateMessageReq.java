package protocoltests.protocol.messages;

public record PrivateMessageReq(String receiver, String message) {

    @Override
    public String toString() {
        return "PrivateMessageReq[" +
                "receiver=" + receiver + ", " +
                "message=" + message + ']';
    }


}
