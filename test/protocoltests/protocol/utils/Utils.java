package protocoltests.protocol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import protocoltests.protocol.messages.*;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<Class<?>, String> objToNameMapping = new HashMap<>();

    static {
        objToNameMapping.put(Login.class, "LOGIN");
        objToNameMapping.put(LoginResp.class, "LOGIN_RESP");
        objToNameMapping.put(BroadcastReq.class, "BROADCAST_REQ");
        objToNameMapping.put(BroadcastResp.class, "BROADCAST_RESP");
        objToNameMapping.put(Broadcast.class, "BROADCAST");
        objToNameMapping.put(Joined.class, "JOINED");
        objToNameMapping.put(ParseError.class, "PARSE_ERROR");
        objToNameMapping.put(Pong.class, "PONG");
        objToNameMapping.put(PongError.class, "PONG_ERROR");
        objToNameMapping.put(Welcome.class, "WELCOME");
        objToNameMapping.put(Ping.class, "PING");
        objToNameMapping.put(ListReq.class, "LIST_USERS_REQ");
        objToNameMapping.put(ListResp.class, "LIST_USERS_RESP");
        objToNameMapping.put(PrivateMessage.class, "PRIVATE");
        objToNameMapping.put(PrivateMessageReq.class, "PRIVATE_MESSAGE_REQ");
        objToNameMapping.put(PrivateMessageResp.class, "PRIVATE_MESSAGE_RESP");
        objToNameMapping.put(StartGuessingGameReq.class, "START_GAME_REQ");
        objToNameMapping.put(StartGuessingGameResp.class, "START_GAME_RESP");
        objToNameMapping.put(JoinGameReq.class, "JOIN_GAME_REQ");
        objToNameMapping.put(JoinGameResp.class, "JOIN_GAME_RESP");
        objToNameMapping.put(GuessNumberReq.class, "GUESS_NUMBER_REQ");
        objToNameMapping.put(GuessNumberResp.class, "GUESS_NUMBER_RESP");
        objToNameMapping.put(GameResults.class, "GAME_RESULTS");
        objToNameMapping.put(GameNotification.class, "GAME_NOTIFICATION");
    }

    public static String objectToMessage(Object object) throws JsonProcessingException {
        Class<?> clazz = object.getClass();
        String header = objToNameMapping.get(clazz);
        if (header == null) {
            throw new RuntimeException("Cannot convert this class to a message");
        }
        String body = mapper.writeValueAsString(object);
        return header + " " + body;
    }

    public static <T> T messageToObject(String message) throws JsonProcessingException {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        String[] parts = message.split(" ", 2);
        if (parts.length > 2 || parts.length == 0) {
            throw new RuntimeException("Invalid message");
        }
        String header = parts[0];
        String body = "{}";
        if (parts.length == 2) {
            body = parts[1];
        }
        Class<?> clazz = getClass(header);
        Object obj = mapper.readValue(body, clazz);
        return (T) clazz.cast(obj);
    }

    private static Class<?> getClass(String header) {
        return objToNameMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find class belonging to header " + header));
    }
}
