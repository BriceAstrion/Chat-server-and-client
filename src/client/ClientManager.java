package client;

import com.fasterxml.jackson.core.*;
import org.slf4j.*;
import com.fasterxml.jackson.databind.*;
import org.json.*;
import shared.*;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;
import java.util.*;

import static client.ServerMessageHandler.*;
import static shared.Constant.*;



/**
 * The ClientManager class handles communication between the client and the server.
 * It manages connections, processes incoming messages, and provides methods for user interaction.
 */
public class ClientManager implements Runnable {

    private Socket socket;
    private BufferedReader is;
    private PrintWriter os;
    private String username;
    private volatile boolean loggedIn = true;
    private final ObjectMapper mapper = new ObjectMapper();
    ServerMessageHandler handler;
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);
    private KeyPair keyPair;
    private SecretKey sessionKey;
    private String encryptedMessage;
    private FileTransferResponse fileTransferResponse;
    private Socket fileTransferSocket;



    /**
     * Default constructor for ClientManager.
     * Initializes the connection to the server and generates a key pair for secure communication.
     */
    public ClientManager() {
        connect();
        handler = new ServerMessageHandler();
        generateKeyPair();
    }


    /**
     * Establishes a connection to the server.
     */
    public void connect() {

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os = new PrintWriter(socket.getOutputStream(), true);

            handleWelcomeMessage();

        } catch (IOException ex) {

            System.out.println("WE LOST CONNECTION TO SERVER, CLIENT WILL CLOSE");
            System.exit(0);

        }

    }


    /**
     * Runs the ClientManager in a separate thread to continuously listen for server messages.
     */
    @Override
    public void run() {

        while (loggedIn) {
            String serverData;

            try {
                serverData = is.readLine();

                if (serverData == null) {
                    // Handle the case where serverData is null
                    logger.warn("Server data is null");
                    break;
                }

                // Check the loggedIn status before processing messages
                if (!loggedIn) {
                    break;
                }

                String[] message = serverData.split(" ", 2);

                handlePingPong(message[0]);

                if (message.length > 1) {

                    if (messagesRequiresResponse(message[0])) {
                        handleMessagesThatNeedsResponseAfterReceiving(message[0], message[1]);
                    } else {
                        handler.handleServerMessage(message[0], message[1]);
                    }

                } else {
                    handler.handleServerMessage(message[0], null);
                }

            } catch (IOException e) {
                System.out.println("SORRY, WE LOST SUDDENLY THE CONNECTION TO SERVER. CLIENT WILL CLOSE....");
                System.exit(0);
            }

        }
    }


    protected String getErrorMessage(int code) {
        return getMessage(code);
    }


    /**
     * Handles server pings and responds with pongs.
     *
     * @param serverData The type of server message.
     */
    private void handlePingPong(String serverData) {
        if (serverData.equals(PING)) {
            os.println(PONG);
        }
    }


    /**
     * SEND TO SERVER
     **/


    /**
     * Broadcasts a message to all connected clients.
     *
     * @param berichten The message to be broadcast.
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void broadcastMessage(String berichten) throws JsonProcessingException {
        String jsonMessage = BROADCAST_REQ + " " + mapper.writeValueAsString(berichten);
        os.println(jsonMessage);
    }


    /**
     * Handles the welcome message received from the server upon connection.
     */
    private void handleWelcomeMessage() {


        try {
            String welcomeMessage = is.readLine();
            System.out.println("WELCOME TO SERVER 1337");
        } catch (IOException e) {
            logger.error("Error while handling welcome message");
        }
    }


    protected void loginRequest(String username) {
        os.println(LOGIN + " {\"username\":\"" + username + "\"}");
        this.username = username;
    }


    public void logout() {
        os.println(" Logged out ");
        System.exit(0);
    }



    /**
     * Sends a request to the server to list all online users.
     *
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void sendListUsersRequest() throws JsonProcessingException {
        ListOfUsers listUsersRequest = new ListOfUsers();

        String jsonMessage = LIST_USERS_REQ + " " + mapper.writeValueAsString(listUsersRequest);
        os.println(jsonMessage);
    }


    /**
     * Sends a private message to another user.
     *
     * @param recipient The recipient of the private message.
     * @param message   The content of the private message.
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void sendPrivateMessage(String recipient, String message) throws JsonProcessingException {
        PrivateMessageReq privateMessageRequest = new PrivateMessageReq();
        privateMessageRequest.setReceiver(recipient);
        privateMessageRequest.setMessage(message);

        String jsonMessage = PRIVATE_MESSAGE_REQ + " " + mapper.writeValueAsString(privateMessageRequest);
        os.println(jsonMessage);
    }


    /**
     * Sends a request to the server to start a guessing game.
     *
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void sendStartGameRequest() throws JsonProcessingException {
        StartGuessingGame startGameRequest = new StartGuessingGame();
        String jsonMessage = START_GAME_REQ + " " + mapper.writeValueAsString(startGameRequest);
        os.println(jsonMessage);
    }


    /**
     * Sends a request to the server to join an existing game.
     *
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void sendJoinGame() throws JsonProcessingException {
        JoinGame joinGameRequest = new JoinGame();
        String jsonMessage = JOIN_GAME_REQ + " " + mapper.writeValueAsString(joinGameRequest);
        os.println(jsonMessage);
    }


    /**
     * Sends the user's guesses to the server during a game.
     *
     * @param guess The user's guess.
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void sendGuesses(int guess) throws JsonProcessingException {
        GuessingGame guessNumber = new GuessingGame();
        guessNumber.setNumber(guess);

        String jsonMessage = GUESS_NUMBER_REQ + " " + mapper.writeValueAsString(guessNumber);
        os.println(jsonMessage);
    }


    public void logException(String message, Exception e) {
        logger.error(message, e);
    }


    /**
     * Sends a file.txt transfer request to another user.
     *
     * @param receiver The recipient of the file.txt.
     * @param filename The name of the file.txt.
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void sendFile(String receiver, String filename) throws JsonProcessingException {

        FileTransferRequest fileRequest = new FileTransferRequest();
        fileRequest.setFilename(filename);
        fileRequest.setReceiver(receiver);
        fileRequest.setSender(username);

        String jsonMessage = FILE_TRANSFER_REQUEST + " " + mapper.writeValueAsString(fileRequest);
        os.println(jsonMessage);
    }


    /**
     * Sends a response to a file.txt transfer request from another user.
     *
     * @param serverData The data received from the server.
     */
    public void sendFileResponse(String serverData) {

        ObjectMapper objectMapper = new ObjectMapper();

        FileTransferRequest fileTransferRequest;

        try {
            fileTransferRequest = objectMapper.readValue(serverData, FileTransferRequest.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("File Transfer request received from " + fileTransferRequest.getSender() + ": " + fileTransferRequest.getFilename());
        String sender = fileTransferRequest.getSender();

        fileTransferResponse = new FileTransferResponse();
        fileTransferResponse.setSender(sender);

        System.out.println("9- YES \n10- NO ");


    }


    /**
     * Accepts a pending file.txt transfer request.
     */
    public void acceptFile() {


        if (fileTransferResponse != null) {
            fileTransferResponse.setStatus("OK");
            UUID uuid = UUID.randomUUID();
            fileTransferResponse.setTransferId(uuid);
            System.out.println(fileTransferResponse.getTransferId().toString());

            try {
                String jsonMessage = FILE_TRANSFER_RESPONSE + " " + mapper.writeValueAsString(fileTransferResponse);
                os.println(jsonMessage);

                fileTransferSocket = new Socket("127.0.0.1", 1338);
                fileTransferSocket.getOutputStream().write("R".getBytes());
                fileTransferSocket.getOutputStream().write(uuid.toString().getBytes());
                fileTransferSocket.getOutputStream().flush();


                new Thread(() -> handleFileReceive(uuid.toString())).start();
                System.out.println("after the thread");

            } catch (IOException e ) {
                throw new RuntimeException(e);
            }

        } else {
            System.out.println("there is no pending file.txt requests");
        }

    }




    /**
     * Rejects a pending file.txt transfer request.
     */
    public void rejectFile() {

        if (fileTransferResponse != null) {
            fileTransferResponse.setStatus("NO");
            try {
                String jsonMessage = FILE_TRANSFER_RESPONSE + " " + mapper.writeValueAsString(fileTransferResponse);
                os.println(jsonMessage);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        } else {
            System.out.println("there is no pending file.txt requests");
        }
    }


    private void handleFileReceive(String uuid) {
        try {
            System.out.println("filetransfer socket : " + fileTransferSocket);
            if (fileTransferSocket != null && !fileTransferSocket.isClosed()) {
                InputStream inputStream = fileTransferSocket.getInputStream();
                String fileExtension = new String(inputStream.readNBytes(3));
                String checksum = new String(inputStream.readNBytes(32));
                File file = new File(uuid + "." + fileExtension);

                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    System.out.println("inside the try block");
                    byte[] fileBuffer = new byte[8192];
                    System.out.println("the file");
                    int bytesRead;
                    while ((bytesRead = inputStream.read(fileBuffer)) != -1) {

                        fileOutputStream.write(fileBuffer, 0, bytesRead);
                    }

                    System.out.println("file transfer is done !!!");
                    // Compare checksum received and checksum of the file.txt
                    boolean notCorrupted = compareChecksum(checksum, file);
                    if (notCorrupted) {
                        System.out.println("File received and saved: " + file.getName());
                    } else {
                        System.out.println("File was corrupted during transfer!");
                        file.delete();
                    }
                }
            } else {
                System.out.println("File transfer socket is not properly set up.");
            }
        } catch (IOException e) {
            System.err.println("Exception during file transfer handling: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (fileTransferSocket != null && !fileTransferSocket.isClosed()) {
                    fileTransferSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing file transfer socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private boolean compareChecksum(String receivedChecksum, File file) {
        String receivedFileChecksum;
        try {
            receivedFileChecksum = FileTransferHandler.calculateMD5Checksum(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return receivedChecksum.equals(receivedFileChecksum);
    }

    /**
     * Sends a secure message with encryption to another user.
     *
     * @param receiver The recipient of the secure message.
     * @param message  The content of the secure message.
     * @throws JsonProcessingException If there is an error processing the JSON.
     */
    public void sendEncryptedMessage(String receiver, String message) throws JsonProcessingException {
        EncryptedMessageRequest encryptedMessageRequest = new EncryptedMessageRequest();
        encryptedMessageRequest.setReceiver(receiver);
        encryptedMessageRequest.setSender(this.username);

        String jsonMessage = SECURE_MESSAGE_REQ + " " + mapper.writeValueAsString(encryptedMessageRequest);
        os.println(jsonMessage);
    }



    /**
     * RECEIVE FROM SERVER
     **/
    public Boolean login() throws IOException {
        String loginResponse = is.readLine();

        if (loginResponse.startsWith(LOGIN_RESP)) {
            JSONObject loginObject = new JSONObject(loginResponse.substring(LOGIN_RESP.length() + 1));
            String status = loginObject.getString("status");

            if (status.equals("OK")) {
                System.out.println("You have successfully logged in as " + username);
                return true;

            } else if (status.equals("ERROR")) {
                int code = loginObject.getInt("code");
                System.out.println("Login failed with error code " + code + ": " + getErrorMessage(code));
                return false;
            }
        }

        return false;
    }


    /**
     * Handles messages that require a response after receiving from the server.
     *
     * @param header     The type of server message.
     * @param serverData The data received from the server.
     */
    public void handleMessagesThatNeedsResponseAfterReceiving(String header, String serverData) {

        switch (header) {

            case FILE_TRANSFER_REQUEST -> sendFileResponse(serverData);
            case SECURE_MESSAGE_REQ -> handleSendingPublicKey(serverData);
            case SECURE_MESSAGE_RES -> handleSendingSessionKey(serverData);
            case SECURE_MESSAGE -> handleEncryptedMessage(serverData);
        }


    }

    public void handleSendingPublicKey(String serverData) {


        try {
            EncryptedMessageRequest message = mapper.readValue(serverData, EncryptedMessageRequest.class);
            String sender = message.getSender();


            EncryptedMessageResponse responsePublicKey = new EncryptedMessageResponse();
            responsePublicKey.setSender(sender);
            responsePublicKey.setPublicKey(getBase64PublicKey());
            responsePublicKey.setReceiver(this.username);


            String jsonMessage = SECURE_MESSAGE_RES + " " + mapper.writeValueAsString(responsePublicKey);

            os.println(jsonMessage);


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


    public void handleSendingSessionKey(String serverData) {
        try {

            EncryptedMessageResponse message = mapper.readValue(serverData, EncryptedMessageResponse.class);
            String sender = message.getSender();
            String receiver = message.getReceiver();
            String publicKey = message.getPublicKey();
            generateSessionKey();

            EncryptedMessage encryptedMessageObject = new EncryptedMessage();
            String encryptedSessionKey = encryptSessionKey(publicKey);
            String messageAfterEncryption = encryptMessage(encryptedMessage, sessionKey);

            encryptedMessageObject.setReceiver(receiver);
            encryptedMessageObject.setSender(this.username);
            encryptedMessageObject.setSessionKey(encryptedSessionKey);
            encryptedMessageObject.setEncryptedMessage(messageAfterEncryption);

            String jsonMessage = SECURE_MESSAGE + " " + mapper.writeValueAsString(encryptedMessageObject);

            os.println(jsonMessage);


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public void handleEncryptedMessage(String serverData) {
        try {
            EncryptedMessage message = mapper.readValue(serverData, EncryptedMessage.class);
            String sender = message.getSender();
            String encryptedSessionKey = message.getSessionKey();
            String messageEncrypted = message.getEncryptedMessage();

            String decryptedSessionKey = decryptSessionKey(encryptedSessionKey, keyPair.getPrivate());
            String decryptedMessage = decryptMessage(messageEncrypted, decryptedSessionKey);


            System.out.println(sender + " has sent a secure message : " + decryptedMessage);


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating key pair", e);
        }
    }


    private String getBase64PublicKey() {
        PublicKey publicKey = keyPair.getPublic();
        byte[] publicKeyBytes = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(publicKeyBytes);
    }


    private void generateSessionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            sessionKey = keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating session key", e);
        }
    }


    private String encryptSessionKey(String publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromString(publicKey));

            byte[] encryptedKey = cipher.doFinal(sessionKey.getEncoded());
            return Base64.getEncoder().encodeToString(encryptedKey);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Error encrypting session key", e);
        }
    }


    private PublicKey getPublicKeyFromString(String publicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error converting string to public key", e);
        }
    }


    public void sentEncryptedMessage(String message) {
        this.encryptedMessage = message;
    }


    private String encryptMessage(String message, SecretKey sessionKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey);

            byte[] iv = cipher.getIV();  // Get the initialization vector
            byte[] encryptedMessageBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(mergeArrays(iv, encryptedMessageBytes));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Error encrypting message", e);
        }
    }


    private byte[] mergeArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }


    // Decryption
    private String decryptSessionKey(String encryptedSessionKey, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedSessionKey);
            byte[] decryptedKeyBytes = cipher.doFinal(encryptedKeyBytes);

            SecretKey secretKey = new SecretKeySpec(decryptedKeyBytes, "AES");
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Error decrypting session key", e);
        }
    }

    private String decryptMessage(String encryptedMessage, String sessionKey) {
        try {
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(sessionKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // Extract the IV from the encrypted message
            byte[] encryptedMessageBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] iv = new byte[16];
            System.arraycopy(encryptedMessageBytes, 0, iv, 0, iv.length);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] decryptedMessageBytes = cipher.doFinal(encryptedMessageBytes, iv.length, encryptedMessageBytes.length - iv.length);
            return new String(decryptedMessageBytes);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error decrypting message", e);
        }
    }


    private Boolean messagesRequiresResponse(String header) {
        return header.equals(FILE_TRANSFER_REQUEST) || header.equals(SECURE_MESSAGE_REQ) || header.equals(SECURE_MESSAGE_RES) || header.equals(SECURE_MESSAGE);
    }


}
