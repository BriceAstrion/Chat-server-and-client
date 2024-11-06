package server;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import shared.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

import static shared.Constant.*;




/**
 * Handles communication with a single client.
 */
public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private Timer pingTimer = new Timer();
    private Timer pongTimer = new Timer();
    private volatile boolean pongReceived = false;
    private volatile boolean pingSent = false;
    private String username;
    private PrintWriter writer;
    private BufferedReader reader;
    static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();



    /**
     * Constructs a ClientHandler with the given client socket.
     *
     * @param clientSocket The client socket to handle.
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        GameHandler gameHandler = new GameHandler(this);
    }


    /**
     * Entry point for the client handler thread.
     */
    @Override
    public void run() {
        initialization();
    }


    /**
     * Initializes the client handler by setting up IO streams and sending a welcome message.
     */
    private void initialization() {
        try {
            setUpIOStreams();
            sendWelcomeMessage();
            processMessages();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Sets up input and output streams for communication with the client.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void setUpIOStreams() throws IOException {
        InputStream is = clientSocket.getInputStream();
        OutputStream os = clientSocket.getOutputStream();
        writer = new PrintWriter(os, true);
        reader = new BufferedReader(new InputStreamReader(is));
    }


    /**
     * Sends a welcome message to the client upon connection.
     */
    private void sendWelcomeMessage() {
        WelcomeMessage welcomeMessage = new WelcomeMessage();
        welcomeMessage.setMessage("Welcome to the server " + VERSION);
        sendMessage(WELCOME, welcomeMessage);
    }


    void sendMessage(String responseType, Sendable response) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);

            if (!responseType.isEmpty()) {
                // Prepend the response type to the JSON message
                jsonResponse = responseType + " " + jsonResponse;
            }

            writer.println(jsonResponse);

        } catch (IOException e) {
            LOGGER.severe("Error while processing: " + e.getMessage());
        }
    }


    /**
     * Processes incoming messages from the client.
     */

    private void processMessages() throws JsonProcessingException {
        try {
            String message;

            while ((message = reader.readLine()) != null) {
                String[] parts = message.split(" ", 2);
                handleMessage(parts);
            }

            ServerSetUp.removeUser(this);
            closeConnection();

        } catch (IOException e) {
            handleIOException(e);
        }
    }

    private void handleIOException(IOException e) {
        ServerSetUp.removeUser(this);
        closeConnection();
        System.err.println("Socket status " + e.getMessage());
    }


    /**
     * Handles a specific message type based on its header.
     */

    private void handleMessage(String[] parts) throws JsonProcessingException {
        if (parts.length < 1) {
            sendParseError();
            return;
        }

        String messageType = parts[0];
        String payload = parts.length > 1 ? parts[1] : "{}";

        // Process the message based on its type
        switch (messageType) {
            case LOGIN -> loginUser(payload);
            case BROADCAST_REQ -> broadcastMessage(payload);
            case PING -> handlePing();
            case PONG -> handlePong();
            case BYE -> handleBye();
            case LIST_USERS_REQ -> listConnectedUsers();
            case PRIVATE_MESSAGE_REQ -> PrivateMessageHandler.handlePrivateMessage(this, payload);
            case START_GAME_REQ -> GameHandler.startGuessingGame(this);
            case JOIN_GAME_REQ -> GameHandler.joinGuessingGame(this);
            case GUESS_NUMBER_REQ -> GameHandler.checkClientGuess(payload, this);
            case FILE_TRANSFER_REQUEST -> handleFileTransfer(payload);
            case FILE_TRANSFER_RESPONSE -> handleHandshakeResponse(payload);
            case SECURE_MESSAGE_REQ -> handleEncryptedMessageReq(payload);
            case SECURE_MESSAGE_RES -> handleEncryptedMessageResponse(payload);
            case SECURE_MESSAGE -> handleEncryptedMessage(payload);
            default -> LOGGER.warning("Unknown message type: " + messageType);
        }
    }

    private void sendParseError() {
        ParseError parseError = new ParseError();
        sendMessage(PARSE_ERROR, parseError);
    }


    /**
     * Handles the LOGIN message type, attempting to log in the client.
     *
     * @param message The LOGIN message payload.
     *
     **/

    private void loginUser(String message) {
        try {
            if (message.isEmpty()) {
                sendLoginResponse("ERROR", USERNAME_INVALID_FORMAT_OR_LENGTH);
                return;
            }

            LoginRequest loginRequest = objectMapper.readValue(message, LoginRequest.class);
            String username = Objects.requireNonNull(loginRequest.getUsername());

            if (this.username != null) {
                // Cannot log in twice
                sendLoginResponse("ERROR", USER_CANNOT_LOGIN_TWICE);
            } else if (!isValidUsername(username)) {
                // User must be syntactically valid
                sendLoginResponse("ERROR", USERNAME_INVALID_FORMAT_OR_LENGTH);
            } else if (isUsernameInUse(username)) {
                // Username already used by another client
                sendLoginResponse("ERROR", USER_ALREADY_LOGGED_IN);
            } else {
                // Successful login
                synchronized (this) { // Ensure thread safety
                    if (this.username == null) {
                        this.username = username;
                        sendLoginResponse("OK", 0);
                        System.out.println(username + " has joined.");
                        ServerSetUp.addUsers(this);

                        // Notify other users about the new user joining
                        notifyOtherUsers(username);

                        // Start the heartbeat thread
                        Thread heartBeat = new Thread(this::startHeartBeat);
                        heartBeat.start();
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.severe("Error while logging in: " + e.getMessage());
        }
    }


    private void sendLoginResponse(String status, int code) {
        Login loginResponse = new Login();
        loginResponse.setStatus(status);
        loginResponse.setCode(code);
        sendMessage(LOGIN_RESP, loginResponse);
    }


    /**
     * Notifies other users about the new user joining.
     *
     * @param newUser The username of the newly joined user.
     */
    private static void notifyOtherUsers(String newUser) {
        for (ClientHandler client : ServerSetUp.getUsers()) {
            String username = client.getUsername();
            if (username != null && !username.equals(newUser)) {
                client.sendJoinedMessage(newUser);
            }
        }
    }


    /**
     * Sends a JOINED message to the client.
     *
     */
    private void sendJoinedMessage(String newUser) {
        JoinedMessage joinedMessage = new JoinedMessage();
        joinedMessage.setUsername(newUser);
        sendMessage(JOINED, joinedMessage);
    }


    /**
     * Initiates a PING message to check the client's connectivity.
     */
    private void handlePing() {
        pingSent = true;
        pongReceived = false;
        sendMessageNoPayload(PING);
    }



    /**
     * Starts the heartbeat mechanism to periodically send PING messages to the client.
     */
    private void startHeartBeat() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOGGER.severe("Error while heart beating in: " + e.getMessage());
        }

        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isUserLoggedIn(username)) {
                    handlePing();
                } else {
                    pingTimer.cancel();
                    closeConnection();
                }
            }
        }, 0, PING_FREQ_MS);
    }


    /**
     * Sends a message without a payload to the client.
     *
     * @param header The message header.
     */
    private void sendMessageNoPayload(String header) {
        System.out.println(this.username + " <-- " + header);
        writer.println(header);
        writer.flush();
    }


    /**
     * Handles the PONG message type indicating successful response to a PING.
     */
    private void handlePong() {
        if (!pingSent) { sendPongError();}
        pingSent = false;

        System.out.println(this.username + " --> PONG");
        pongReceived = true;
        pongTimer = new Timer();
        pongTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!pongReceived) {
                    pongTimer.cancel();
                    closeConnection();

                }
            }
        },0, PONG_TIMEOUT_MS);
    }

    private void sendPongError() {
        PongError pongError = new PongError();
        pongError.setCode(PONG_WITHOUT_PING);
        sendMessage(PONG_ERROR, pongError);
    }

    /**
     * Broadcasts a message to all connected clients except the sender.
     *
     * @param message The message to broadcast.
     */
    private void broadcastMessage(String message) {
        System.out.println("Message sent from " + this.username + ": " + message);

        BroadcastResponse response = new BroadcastResponse();
        response.setStatus("OK");
        response.setCode(0);

        for (ClientHandler client : ServerSetUp.getUsers()) {
            if (!client.equals(this)) {
                client.sendMessageToEveryone(message, this);
            }
        }

        sendMessage(BROADCAST_RESP, response);
    }


    /**
     * Sends a broadcast message to all connected clients.
     *
     * @param message The message to broadcast.
     * @param client  The sender of the message.
     */
    private void sendMessageToEveryone(String message, ClientHandler client) {
        Broadcast broadcast = new Broadcast();
        broadcast.setUsername(client.username);
        broadcast.setMessage(message);

        try {
            String jsonResponse = objectMapper.writeValueAsString(broadcast);
            jsonResponse = BROADCAST + " " + jsonResponse;
            System.out.println(jsonResponse);
            writer.println(jsonResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Closes the connection for the current client.
     */
    private void closeConnection() {

        ServerSetUp.removeUser(this);

        try {
            // Close the socket, input stream, and output stream
            clientSocket.close();
            reader.close();
            writer.close();



        } catch (IOException e) {
            LOGGER.severe("Failed to close the connection");
        }
    }


    /**
     * Handles the BYE message type, indicating the client's intention to disconnect.
     */
    private void handleBye() {
        ByeResponse byeResponse = new ByeResponse();
        sendMessage(BYE_RESP, byeResponse);

        try {
            clientSocket.close();
        } catch (IOException e) {
            LOGGER.severe("Error while closing: " + e.getMessage());
        }
    }


    /**
     * Lists the usernames of all connected clients.
     */
    private void listConnectedUsers() {
        if (isUserLoggedIn(username)) {

            System.out.println(username + " --> " + LIST_USERS_REQ);

            // Get the list of connected usernames
            List<String> connectedUsers = ServerSetUp.getUsers().stream()
                    .map(ClientHandler::getUsername)
                    .collect(Collectors.toList());

            ListOfUsers listUsersResponse = new ListOfUsers();
            listUsersResponse.setStatus("OK");
            listUsersResponse.setUsers(connectedUsers);
            sendMessage(LIST_USERS_RESP, listUsersResponse);
            System.out.println(username + " <-- " + LIST_USERS_RESP);

        } else {

            // If the user is not logged in, send a LoginResp message
            Login loginResponse = new Login();
            loginResponse.setStatus("ERROR");
            loginResponse.setCode(USER_NOT_LOGGED_IN);
            sendMessage(LOGIN_RESP, loginResponse);
        }
    }


    /**
     * Handles file.txt transfer requests between clients.
     *
     * @param message The file.txt transfer request payload.
     */
    private void handleFileTransfer(String message) {
        try {
            FileTransferRequest fileTransferRequest = objectMapper.readValue(message, FileTransferRequest.class);

            // Extract information from the file.txt transfer request
            String receiver = fileTransferRequest.getReceiver();
            String filename = fileTransferRequest.getFilename();

            if (isUserLoggedIn(receiver)) {
                // Log the routing of the file.txt transfer request
                System.out.println("Routing file.txt transfer request of " + filename + " from : " +
                        this.username + " to " + receiver);

                // Send the file.txt transfer request to the receiver
                ClientHandler receiverHandler = getUserHandler(receiver);
                if (receiverHandler != null) {
                    receiverHandler.sendMessage(FILE_TRANSFER_REQUEST, fileTransferRequest);
                }

            } else {
                PrivateMessage response = new PrivateMessage();
                response.setStatus("ERROR");
                response.setMessage("Recipient not found");
                sendMessage(PRIVATE_MESSAGE_RESP, response);
            }

        } catch (IOException e) {
            LOGGER.severe("Error while sending the file.txt: " + e.getMessage());
        }
    }


    /**
     * Handles the response to a file.txt transfer request, completing the handshake.
     *
     * @param message The file.txt transfer response payload.
     */
    private void handleHandshakeResponse(String message) {
        try {
            FileTransferResponse handshakeResponse = objectMapper.readValue(message, FileTransferResponse.class);

            // Extract information from the handshake response
            String sender = handshakeResponse.getSender();
            String status = handshakeResponse.getStatus();

            if (isUserLoggedIn(sender)) {
                // Send the file.txt transfer response to the sender to complete the handshake
                ClientHandler receiverHandler = getUserHandler(sender);

                if (receiverHandler != null) {
                    receiverHandler.sendMessage(FILE_TRANSFER_RESPONSE, handshakeResponse);
                }

            } else {
                // Send an error response if the recipient is not found
                PrivateMessage response = new PrivateMessage();
                response.setStatus("ERROR");
                response.setCode(RECIPIENT_NOT_FOUND);
                response.setMessage("Recipient not found");
                sendMessage(FILE_TRANSFER_RESPONSE, response);
            }

        } catch (IOException e) {
            LOGGER.severe("Error while sending the file.txt:" + e.getMessage());
        }
    }


    /**
     * Handles encrypted message requests between clients.
     *
     * @param payload The encrypted message request payload.
     */
    private void handleEncryptedMessageReq(String payload) {
        try {
            EncryptedMessageRequest encryptedMessageRequest = objectMapper.readValue(payload, EncryptedMessageRequest.class);

            String receiver = encryptedMessageRequest.getReceiver();

            if (isUserLoggedIn(receiver)) {
                EncryptedMessageResponse response = new EncryptedMessageResponse();

                // Send the private message to the receiver
                ClientHandler receiverHandler = getUserHandler(receiver);

                if (receiverHandler != null) {
                    response.setSender(this.username);

                    // Send the private message content
                    receiverHandler.sendMessage(SECURE_MESSAGE_REQ, encryptedMessageRequest);
                }

            } else {

                System.out.println("Private Message Error: " + this.username + " -> " + receiver + " Recipient not found");
            }

        } catch (IOException e) {
            LOGGER.severe("Error while processing the private message: " + e.getMessage());
        }
    }


    /**
     * Handles the response to an encrypted message request, exchanging public keys.
     *
     * @param payload The encrypted message response payload.
     */
    private void handleEncryptedMessageResponse(String payload) {
        try {
            EncryptedMessageResponse response = objectMapper.readValue(payload, EncryptedMessageResponse.class);
            String sender = response.getSender();
            String receiver = response.getReceiver();
            String publicKey = response.getPublicKey();


            ClientHandler senderHandler = getUserHandler(sender);


            if (sender!= null && senderHandler!=null && publicKey!=null) {
                senderHandler.sendMessage(SECURE_MESSAGE_RES,response);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Handles the exchange of encrypted messages between clients.
     *
     * @param payload The encrypted message payload.
     */
    private void handleEncryptedMessage(String payload) {
        try {
            EncryptedMessage message = objectMapper.readValue(payload, EncryptedMessage.class);

            String encryptedMessage = message.getEncryptedMessage();
            String sessionKey = message.getSessionKey();
            String receiver = message.getReceiver();


            ClientHandler receiverHandler = getUserHandler(receiver);
            if (receiverHandler != null && sessionKey != null ){
                receiverHandler.sendMessage(SECURE_MESSAGE, message);
            }

            System.out.println(this.username + " -> " + receiver +
                    " { DM: " + encryptedMessage + "}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }


    /**
     * Checks if a user with the given username is currently logged in.
     *
     * @param username The username to check.
     * @return True if the user is logged in, false otherwise.
     */
    static boolean isUserLoggedIn(String username) {

        if (username != null){
            return ServerSetUp.containsUser(username);
        }
        return false;
    }


    /**
     * Gets the handler for a user with the given username.
     *
     * @param username The username to search for.
     * @return The handler for the user, or null if not found.
     */
    ClientHandler getUserHandler(String username) {
        return ServerSetUp.getUsers().stream()
                .filter(client -> client.username.equals(username))
                .findFirst().orElse(null);
    }


    /**
     * Checks if the given username is a valid format.
     *
     * @param username The username to check.
     * @return True if the username is valid, false otherwise.
     */
    private boolean isValidUsername(String username) {
        return USERNAME.matcher(username).matches();
    }


    /**
     * Checks if the given username is already in use.
     *
     * @param username The username to check.
     * @return True if the username is in use, false otherwise.
     */
    private boolean isUsernameInUse(String username) { return ServerSetUp.containsUser(username); }


    /**
     * Gets the username of the current client.
     *
     * @return The username.
     */
    public String getUsername() { return username; }

    @Override
    public String toString() {
        return username;
    }
}
