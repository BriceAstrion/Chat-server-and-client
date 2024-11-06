package protocoltests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import protocoltests.protocol.messages.ListReq;
import protocoltests.protocol.messages.ListResp;
import protocoltests.protocol.messages.Login;
import protocoltests.protocol.messages.LoginResp;
import protocoltests.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;


public class ListOfUserTests {
    private static Properties props = new Properties();

    private Socket socketUser1;
    private BufferedReader inUser1;
    private PrintWriter outUser1;

    private final static int max_delta_allowed_ms = 100;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = ListOfUserTests.class.getResourceAsStream("testconfig.properties");
        props.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(props.getProperty("host"), Integer.parseInt(props.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
    }

    @Test
    void TC7_1_listConnectedUsersReturnsListOfUsers() throws IOException {
        receiveLineWithTimeout(inUser1); // WELCOME

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Login("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // OK

        // Send a list of connected users request
        outUser1.println(Utils.objectToMessage(new ListReq(0, "OK", "List users request")));
        outUser1.flush();

        String response = receiveLineWithTimeout(inUser1);
        ListResp listResp = Utils.messageToObject(response);
        assertNotNull(listResp);
        assertEquals("OK", listResp.status());
    }


    @Test
    void TC7_2_listConnectedUsersReturnsErrorWhenUserIsNotLoggedIn() throws IOException {
        receiveLineWithTimeout(inUser1); // WELCOME
        outUser1.println(Utils.objectToMessage(new ListReq(6000, "ERROR", "User not logged in")));
        outUser1.flush();
        String response = receiveLineWithTimeout(inUser1);
        LoginResp loginResp = Utils.messageToObject(response);
        assertNotNull(loginResp, "Should not be null");
        assertEquals(new LoginResp("ERROR",6000), loginResp);
    }


    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(max_delta_allowed_ms), reader::readLine);
    }

}
