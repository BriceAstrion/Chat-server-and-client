package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import protocoltests.protocol.messages.*;
import protocoltests.protocol.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

public class PrivateMessageTests {
    private static Properties props = new Properties();
    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;
    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = PrivateMessageTests.class.getResourceAsStream("testconfig.properties");
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
    void TC8_1_userSendsPrivateMessageSuccessfullyAndReturnsOK() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // WELCOME
        receiveLineWithTimeout(inUser2); // WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); // OK

        receiveLineWithTimeout(inUser1); // JOINED

        // Send a private message from user1 to user2
        outUser1.println(Utils.objectToMessage(new PrivateMessageReq("user2", "Hello")));
        outUser1.flush();

        // Receive response from server
        String response = receiveLineWithTimeout(inUser1);
        PrivateMessageResp messageResp = Utils.messageToObject(response);
        assertNotNull(messageResp);
        assertEquals("OK", messageResp.getStatus());
    }

    @Test
    void TC8_2_userSendsPrivateMessageAndReturnsNoUserFound() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // WELCOME
        receiveLineWithTimeout(inUser2); // WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // OK

        // Send a private message from user1 to non-existing user
        outUser1.println(Utils.objectToMessage(new PrivateMessageReq("user3", "Hello")));
        outUser1.flush();

        String response = receiveLineWithTimeout(inUser1);
        PrivateMessageResp messageResp = Utils.messageToObject(response);
        assertNotNull(messageResp);
        assertEquals("ERROR", messageResp.getStatus());
        assertEquals(6001, messageResp.getCode());
    }

    @Test
    void TC8_3_userSendsPrivateMessageAndReturnsEmptyBodyMessage() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // WELCOME
        receiveLineWithTimeout(inUser2); // WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); // OK

        receiveLineWithTimeout(inUser1); // JOINED

        // Send a private message from user1 to user2 with an empty message body
        outUser1.println(Utils.objectToMessage(new PrivateMessageReq("user2", "")));
        outUser1.flush();

        String response = receiveLineWithTimeout(inUser1);
        PrivateMessageResp messageResp = Utils.messageToObject(response);
        assertNotNull(messageResp);
        assertEquals("ERROR", messageResp.getStatus());
        assertEquals(6008, messageResp.getCode());
    }

    @Test
    void TC8_4_userSendsPrivateMessageAndReturnsCannotMessageYourself() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); // WELCOME
        receiveLineWithTimeout(inUser2); // WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Login("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); // OK

        receiveLineWithTimeout(inUser1); // JOINED

        // Send a private message from user1 to themselves
        outUser1.println(Utils.objectToMessage(new PrivateMessageReq("user1", "Hello")));
        outUser1.flush();

        String response = receiveLineWithTimeout(inUser1);
        PrivateMessageResp messageResp = Utils.messageToObject(response);
        assertNotNull(messageResp);
        assertEquals("ERROR", messageResp.getStatus());
        assertEquals(6007, messageResp.getCode());

    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }

}
