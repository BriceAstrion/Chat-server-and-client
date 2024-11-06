package protocoltests.protocol.messages;

public record Broadcast(String username, String message) {

    @Override
    public String toString() {
        return "Broadcast[" +
                "username=" + username + ", " +
                "message=" + message + ']';
    }
}
