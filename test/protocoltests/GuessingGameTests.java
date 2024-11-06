package protocoltests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocoltests.protocol.messages.*;
import protocoltests.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;
import static protocoltests.protocol.utils.Utils.messageToObject;

public class GuessingGameTests {
    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;
    private static final int LOWER_BOUND = 1;
    private static final int UPPER_BOUND = 50;
    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = GuessingGameTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
    }

    @Test
    void TC9_1_userInitiatesGameAndAnotherUserSuccessfullyJoins() throws IOException {
        startGuessingGame();
    }

    @Test
    void TC9_2_userJoinGuessingGameWithoutLoggingIn() throws IOException {
        receiveLineWithTimeout(inUser2); //WELCOME

        // User 2 joins the guessing game without logging in
        outUser2.println(Utils.objectToMessage(new JoinGameReq("OK", 0)));
        outUser2.flush();

        String response = receiveLineWithTimeout(inUser2);
        GameNotification gameNotification = messageToObject(response);
        assertNotNull(gameNotification);
        assertEquals("ERROR", gameNotification.status());
        assertEquals(6000, gameNotification.code());
    }

    @Test
    void TC9_3_userJoinGuessingGameAndReturnGameAlreadyStartedCannotJoin() throws IOException {
        receiveLineWithTimeout(inUser2); //WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // User 1 initiates the guessing game
        String message = "";
        outUser1.println(Utils.objectToMessage(new StartGuessingGameReq("OK", message, 0)));
        outUser1.flush();

        // User 2 tries to join the guessing game, but it's already started
        outUser2.println(Utils.objectToMessage(new JoinGameReq("OK", 0)));
        outUser2.flush();

        String response = receiveLineWithTimeout(inUser2);
        GameNotification gameNotification = messageToObject(response);
        assertNotNull(gameNotification);
        assertEquals("ERROR", gameNotification.status());
        assertEquals(6003, gameNotification.code());
    }

    @Test
    void TC9_4_userJoinGuessingGameAndReturnNoRunningGame() throws IOException {
        receiveLineWithTimeout(inUser2); //WELCOME

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // User 2 tries to join the guessing game, but there's no running game
        outUser2.println(Utils.objectToMessage(new JoinGameReq("OK", 0)));
        outUser2.flush();

        String response = receiveLineWithTimeout(inUser2);
        GameNotification gameNotification = messageToObject(response);
        assertNotNull(gameNotification);
        assertEquals("ERROR", gameNotification.status());
        assertEquals(6005, gameNotification.code());
    }

    private void startGuessingGame() throws IOException {
        receiveLineWithTimeout(inUser1); //WELCOME
        receiveLineWithTimeout(inUser2); //WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // User 1 initiates the guessing game
        String message = "";
        outUser1.println(Utils.objectToMessage(new StartGuessingGameReq("OK", message, 0)));
        outUser1.flush();

        // User 2 joins the guessing game
        outUser2.println(Utils.objectToMessage(new JoinGameReq("OK", 0)));
        outUser2.flush();

        receiveLineWithTimeout(inUser1); //OK
        receiveLineWithTimeout(inUser2); //JOINED
    }

    private static int generateRandomNumber() {
        Random random = new Random();
        return random.nextInt(UPPER_BOUND - LOWER_BOUND + 1) + LOWER_BOUND;
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }
}
