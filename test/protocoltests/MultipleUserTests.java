package protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocoltests.protocol.messages.*;
import protocoltests.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class MultipleUserTests {

    private static Properties props = new Properties();

    private Socket socketUser1, socketUser2;
    private BufferedReader inUser1, inUser2;
    private PrintWriter outUser1, outUser2;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = MultipleUserTests.class.getResourceAsStream("testconfig.properties");
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
    void TC3_1_joinedIsReceivedByOtherUserWhenUserConnects() throws JsonProcessingException {
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

        //JOINED is received by user1 when user2 connects
        String resIdent = receiveLineWithTimeout(inUser1);
        Joined joined = Utils.messageToObject(resIdent);

        assertEquals(new Joined("user2", 0, "OK"),joined);
    }

    @Test
    void TC3_2_broadcastMessageIsReceivedByOtherConnectedClients() throws JsonProcessingException {
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

        // Send BROADCAST from user 1
        outUser1.println(Utils.objectToMessage(new BroadcastReq("messagefromuser1")));
        outUser1.flush();
        String fromUser1 = receiveLineWithTimeout(inUser1);
        BroadcastResp broadcastResp1 = Utils.messageToObject(fromUser1);
        assertEquals("OK", broadcastResp1.status());

        String fromUser2 = receiveLineWithTimeout(inUser2);
        Broadcast broadcast2 = Utils.messageToObject(fromUser2);
        assertEquals(new Broadcast("user1", "{\"message\":\"messagefromuser1\"}"), broadcast2);

        // Send BROADCAST from user 2
        outUser2.println(Utils.objectToMessage(new BroadcastReq("messagefromuser2")));
        outUser2.flush();
        fromUser2 = receiveLineWithTimeout(inUser2);
        BroadcastResp broadcastResp2 = Utils.messageToObject(fromUser2);
        assertEquals("OK", broadcastResp2.status());

        // Receive the broadcast message from user 2
        fromUser1 = receiveLineWithTimeout(inUser1);
        Broadcast broadcast1 = Utils.messageToObject(fromUser1);
        assertEquals(new Broadcast("user2", "{\"message\":\"messagefromuser2\"}"), broadcast1);
    }


    @Test
    void TC3_3_loginMessageWithAlreadyConnectedUsernameReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //welcome message
        receiveLineWithTimeout(inUser2); //welcome message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect using the same username
        outUser2.println(Utils.objectToMessage(new Login("user1")));
        outUser2.flush();
        String resUser2 = receiveLineWithTimeout(inUser2);
        LoginResp loginResp = Utils.messageToObject(resUser2);
        assertEquals(new LoginResp("ERROR", 5000), loginResp);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }

}
