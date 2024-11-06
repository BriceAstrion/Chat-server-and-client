package shared;

import java.util.regex.Pattern;

public class Constant {
    public static final String VERSION = "1.5";
    public static final String SERVER_ADDRESS = "127.0.0.1";
    public static final int SERVER_PORT = 1337;
    public static final int FILE_TRANSFER_PORT = 1338;
    public static final int USER_ALREADY_LOGGED_IN = 5000;
    public static final int USERNAME_INVALID_FORMAT_OR_LENGTH = 5001;
    public static final int USER_CANNOT_LOGIN_TWICE = 5002;
    public static final int USER_NOT_LOGGED_IN = 6000;
    public static final int RECIPIENT_NOT_FOUND = 6001;
    public static final int INSUFFICIENT_PLAYERS_TO_START_THE_GAME = 6002;
    public static final int GAME_HAS_ALREADY_STARTED_CANNOT_JOIN = 6003;
    public static final int USER_ALREADY_JOINED = 6004;
    public static final int NO_RUNNING_GAME = 6005;
    public static final int NOT_A_PARTICIPANT = 6006;
    public static final int SEND_TO_SELF_ERROR = 6007;
    public static final int EMPTY_MESSAGE_BODY_ERROR = 6008;
    public static final int PONG_TIMEOUT = 7000;
    public static final int UNTERMINATED_MESSAGE = 7001;
    public static final int INVALID_USER_FORMAT = 7004;
    public static final int YOUR_GUESS_IS_TOO_LOW = 7005;
    public static final int YOUR_GUESS_IS_TOO_HIGH = 7006;
    public static final int NUMBER_OUT_OF_ALLOWED_RANGE = 7007;
    public static final int PONG_WITHOUT_PING = 8000;
    public static final int USER_IS_NOT_PARTICIPATING = 8001;
    public static final int GAME_TIMEOUT_WAS_REACHED = 9000;
    public static final int NO_WINNER_ID_DETERMINED = 9001;
    public static final int NO_GAME_RESULTS_ARE_AVAILABLE = 9002;
    public static final String BROADCAST = "BROADCAST";
    public static final String BROADCAST_REQ = "BROADCAST_REQ";
    public static final String DSCN = "DSCN";
    public static final String WELCOME = "WELCOME";
    public static final String LOGIN = "LOGIN";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String BYE = "BYE";
    public static final String LEFT = "LEFT";
    public static final String UNKNOWN_COMMAND = "UNKNOWN_COMMAND";
    public static final String PARSE_ERROR = "PARSE_ERROR";
    public static final String LOGIN_RESP = "LOGIN_RESP";
    public static final String BYE_RESP = "BYE_RESP";
    public static final String BROADCAST_RESP = "BROADCAST_RESP";
    public static final String PONG_ERROR = "PONG_ERROR";
    public static final String JOINED = "JOINED";
    public static final Pattern LINE = Pattern.compile("([^\n\r]*)(?:\r\n|\r|\n)", Pattern.CASE_INSENSITIVE);
    public static final Pattern USERNAME = Pattern.compile("^[A-Z0-9_]{3,14}$", Pattern.CASE_INSENSITIVE);
    public static final long PING_FREQ_MS = 10000;
    public static final int MAX_PENDING = 1024;
    public static final String LIST_USERS_RESP = "LIST_USERS_RESP";
    public static final String LIST_USERS_REQ= "LIST_USERS_REQ";
    public static final String PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
    public static final String PRIVATE_MESSAGE_RESP = "PRIVATE_MESSAGE_RESP";
    public static final String PRIVATE_MESSAGE_REQ = "PRIVATE_MESSAGE_REQ";
    public static final String START_GAME_REQ = "START_GAME_REQ";
    public static final String START_GAME_RESP = "START_GAME_RESP";
    public static final String GAME_NOTIFICATION = "GAME_NOTIFICATION";
    public static final String JOIN_GAME_REQ = "JOIN_GAME_REQ";
    public static final String JOIN_GAME_RESP = "JOIN_GAME_RESP";
    public static final String GUESS_NUMBER_REQ = "GUESS_NUMBER_REQ";
    public static final String GUESS_NUMBER_RESP = "GUESS_NUMBER_RESP";
    public static final String GAME_RESULTS = "GAME_RESULTS";
    public static final int LOWER_BOUND = 1;
    public static final int UPPER_BOUND = 50;
    public static final int JOINING_TIME_MS = 10000;
    public static final int GAME_TIMEOUT_MILLISECONDS = 120000;
    public static final String FILE_TRANSFER_REQUEST = "FILE_TRANSFER_REQUEST";
    public static final String FILE_TRANSFER_RESPONSE = "FILE_TRANSFER_RESPONSE";
    public static final long PONG_TIMEOUT_MS = 3000;

    //ENCRYPTED MESSAGE :
    public static final String SECURE_MESSAGE_REQ  = "SECURE_MESSAGE_REQ";
    public static final String SECURE_MESSAGE_RES  = "SECURE_MESSAGE_RES";
    public static final String SECURE_MESSAGE = "SECURE_MESSAGE";

}
