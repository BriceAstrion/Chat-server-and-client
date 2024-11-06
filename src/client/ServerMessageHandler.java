package client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shared.*;

import java.io.*;
import java.util.*;

import java.util.function.*;


import static shared.Constant.*;


/**
 * The {@code ServerMessageHandler} class is responsible for handling incoming messages from the server.
 * It parses the messages, delegates to appropriate handlers, and performs necessary actions based on the message type.
 */
public class ServerMessageHandler {
    private final Map<String, Consumer<String>> messageHandlers = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);


    public ServerMessageHandler() {
        initializeMessageHandlers();
    }


    /**
     * Initializes message handlers for different message types.
     */
    private void initializeMessageHandlers() {

        messageHandlers.put(BROADCAST_RESP, this::handleBroadcastResponse);
        messageHandlers.put(BROADCAST, this::handleBroadcast);
        messageHandlers.put(DSCN, this::handleDisconnect);
        messageHandlers.put(PING, this::handlePing);
        messageHandlers.put(PONG_ERROR, this::handlePongError);
        messageHandlers.put(JOINED, this::handleJoined);
        messageHandlers.put(LEFT, this::handleLeft);
        messageHandlers.put(UNKNOWN_COMMAND, this::handleUnknownCommand);
        messageHandlers.put(PARSE_ERROR, this::handleParseError);
        messageHandlers.put(LIST_USERS_RESP, this::handleListOfUsersResponse);
        messageHandlers.put(PRIVATE_MESSAGE, this::handlePrivateMessage);
        messageHandlers.put(PRIVATE_MESSAGE_RESP, this::handlePrivateMessageResponse);
        messageHandlers.put(START_GAME_RESP, this::handleStartGameResponse);
        messageHandlers.put(GAME_NOTIFICATION, this::handleGameNotification);
        messageHandlers.put(JOIN_GAME_RESP, this::handleJoinGameResponse);
        messageHandlers.put(GUESS_NUMBER_RESP, this::handleGuessNumberResponse);
        messageHandlers.put(GAME_RESULTS, this::handleGameResults);
        messageHandlers.put(FILE_TRANSFER_RESPONSE, this::handleHandshakeResponse);

    }

    private void handlePing(String ping) { }


    /**
     * Processes an incoming server message by delegating to the appropriate handler based on the message header.
     *
     * @param header  The message header.
     * @param payload The message payload.
     */
    public void handleServerMessage(String header, String payload) {
        Consumer<String> handler = messageHandlers.get(header);

        if (handler != null) {
            handler.accept(payload);

        } else {
            System.out.println(header);
            handleUnexpectedMessage(header);
        }
    }


    private void handleBroadcastResponse(String serverData) {
        try {
            BroadcastResponse response = objectMapper.readValue(serverData, BroadcastResponse.class);

            if ("OK".equals(response.getStatus())) {
                System.out.println("Your message has been broadcast");

            } else if ("ERROR".equals(response.getStatus())) {
                int code = response.getCode();
                System.out.println("Broadcast failed with error code " + code + ": " + getErrorMessage(code));
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while broadcasting");
        }
    }


    private void handleBroadcast(String serverData) {
        try {
            Broadcast broadcast = objectMapper.readValue(serverData, Broadcast.class);

            String sender = broadcast.getUsername();
            String message = broadcast.getMessage();
            System.out.println(sender + ": " + message);

        } catch (Exception e) {
            System.out.println("Error while handling BROADCAST: " + e.getMessage());
        }
    }


    private void handleDisconnect(String serverData) {
        try {
            DCSN dcsnObject = objectMapper.readValue(serverData, DCSN.class);

            int reasonCode = dcsnObject.getCode();
            System.out.println("Server disconnected you with reason code " + reasonCode + ": " + getErrorMessage(reasonCode));
            System.exit(0);

        } catch (IOException e) {
            logger.error("An error occurred while disconnecting");
        }
    }


    private void handlePongError(String serverData) { }


    private void handleJoined(String serverData) {
        try {
            JoinedMessage joinedMessage = objectMapper.readValue(serverData, JoinedMessage.class);
            String newUser = joinedMessage.getUsername();
            System.out.println(newUser + " has joined the server");

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while joining the server");
        }
    }


    private void handleLeft(String serverData) {
        try {
            LeftMessage leftMessage = objectMapper.readValue(serverData, LeftMessage.class);
            String newUser = leftMessage.getUsername();
            System.out.println(newUser + " has left the server");

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while left");
        }
    }


    private void handleUnknownCommand(String serverData) {
        System.out.println("Server received an invalid message header");
    }


    private void handleParseError(String serverData) {
        System.out.println("Server received an invalid message body");
    }


    private void handleUnexpectedMessage(String serverData) {
        System.out.println("Server sent an unexpected message");
    }


    private void handleListOfUsersResponse(String serverData) {
        try {
            ListOfUsers response = objectMapper.readValue(serverData, ListOfUsers.class);

            if ("OK".equals(response.getStatus())) {
                StringBuilder userListMessage = new StringBuilder("--------------------\n");
                userListMessage.append(" Connected Users: \n");

                for (String user : response.getUsers()) {
                    userListMessage.append(" - ").append(user).append("\n");
                }

                userListMessage.append("--------------------");
                System.out.println(userListMessage);

            } else if ("ERROR".equals(response.getStatus())) {
                int code = response.getCode();
                System.out.println("List users request failed with error code " + code + ": " + getErrorMessage(code));
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while displaying the list of connected users");
        }
    }


    private void handlePrivateMessage(String serverData) {
        try {
            PrivateMessageResp privateMessageResponse = objectMapper.readValue(serverData, PrivateMessageResp.class);

            if ("OK".equals(privateMessageResponse.getStatus())) {
                // Extract the private message content
                PrivateMessage privateMessage = objectMapper.readValue(privateMessageResponse.getMessage(), PrivateMessage.class);
                System.out.println("You received a private message from " + privateMessage.getSender() + ": " + "\"" + privateMessage.getMessage() + "\"");

            } else if ("ERROR".equals(privateMessageResponse.getStatus())) {
                int code = privateMessageResponse.getCode();
                System.out.println("Private message failed with error code " + code + ": " + privateMessageResponse.getMessage());
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while displaying the private message");
        }
    }


    private void handlePrivateMessageResponse(String serverData) {
        try {
            PrivateMessageResp response = objectMapper.readValue(serverData, PrivateMessageResp.class);

            if ("OK".equals(response.getStatus())) {
                System.out.println("Private message sent!");

            } else if ("ERROR".equals(response.getStatus())) {
                int code = response.getCode();
                System.out.println("Private message failed with error code " + code + ": " + getErrorMessage(code));
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while sending the private message");
        }
    }


    private void handleStartGameResponse(String serverData) {
        try {
            StartGuessingGame startGameResponse = objectMapper.readValue(serverData, StartGuessingGame.class);

            if ("OK".equals(startGameResponse.getStatus())) {
                System.out.println("Game initiation successful. Waiting for players to join...");

            } else if ("ERROR".equals(startGameResponse.getStatus())) {
                int errorCode = startGameResponse.getCode();
                System.out.println("An error occurred while initiating the guessing game { " + errorCode + " }: " + getErrorMessage(errorCode));
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while starting the game");
        }
    }


    private void handleGameNotification(String serverData) {
        try {
            GameNotification gameNotification = objectMapper.readValue(serverData, GameNotification.class);

            String status = gameNotification.getStatus();
            String message = gameNotification.getMessage();

            if ("OK".equals(status)) {
                System.out.println(message);

            } else if ("ERROR".equals(status)) {
                int code = gameNotification.getCode();
                System.out.println("An error occurred while sending notification { " + code + " }: " + getErrorMessage(code));
            }

        } catch (JsonProcessingException e) {
            logger.error("Error processing game notification: " + e.getMessage(), e);
        }
    }


    private void handleJoinGameResponse(String serverData) {
        try {
            JoinGame joinGameResponse = objectMapper.readValue(serverData, JoinGame.class);

            if ("OK".equals(joinGameResponse.getStatus())) {
                System.out.println("You have joined the game.");

            } else if ("ERROR".equals(joinGameResponse.getStatus())) {
                int errorCode = joinGameResponse.getCode();
                System.out.println("Joining the game failed with error code " + errorCode + ": " + getErrorMessage(errorCode));
            }

        } catch (JsonProcessingException e) {
            logger.error("Error processing join guessing game.");
        }
    }


    // Modification based on Gerralt feedback
    private void handleGuessNumberResponse(String guessNumberResponse) {
        try {
            GuessingGame response = objectMapper.readValue(guessNumberResponse, GuessingGame.class);

            if ("OK".equals(response.getStatus())) {
                System.out.println("Your guess is valid");

            } else if ("TOO_LOW".equals(response.getStatus())) {
                System.out.println("Your guess is too low.");

            } else if ("TOO_HIGH".equals(response.getStatus())) {
                System.out.println("Your guess is too high.");

            } else if ("CORRECT".equals(response.getStatus())) {
                System.out.println("Congratulations! Your guess is correct!");

            } else if ("OUT_OF_RANGE".equals(response.getStatus())) {
                System.out.println("Your guess is out of range! Make a guess between 1 & 50.");

            } else if ("ERROR".equals(response.getStatus())) {
                int code = response.getCode();
                System.out.println("Guess failed with error code " + code + ": " + getErrorMessage(code));
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while handling guesses.");
        }
    }


    private void handleGameResults(String results) {
        try {
            GuessingGame gameResults = objectMapper.readValue(results, GuessingGame.class);

            if ("OK".equals(gameResults.getStatus())) {
                Map<String, String> resultsMap = gameResults.getResults();

                if (resultsMap.isEmpty()) {
                    System.out.println("No results to display.");
                    return;
                }

                System.out.println("-----------------------------------------");
                StringBuilder message = new StringBuilder("""
                        Guessing Game Results:

                        """);

                int position = 1;

                for (Map.Entry<String, String> entry : resultsMap.entrySet()) {
                    String username = entry.getKey();
                    String time = entry.getValue();
                    String positionStr = position + ". " + username + " (";

                    if (position == 1) {
                        positionStr += "winner, ";
                    }

                    positionStr += time + ")";
                    message.append(positionStr).append("\n");
                    position++;
                }

                System.out.println(message + "-----------------------------------------\n");
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while displaying the results.");
        }
    }



    /**
     * Handles a FILE_TRANSFER_RESPONSE message from the server.
     *
     * @param serverData The FILE_TRANSFER_RESPONSE message.
     */
    private void handleHandshakeResponse(String serverData){
        try {
            FileTransferResponse response = objectMapper.readValue(serverData, FileTransferResponse.class);


            if ("OK".equals(response.getStatus())) {
                System.out.println("The receiver accepted the file.txt");
                FileTransferHandler fileTransferHandler = new FileTransferHandler();
                UUID transferUUID = response.getTransferId();


                fileTransferHandler.FileTransferstart(transferUUID);

            } else if ("NO".equals(response.getStatus())) {
                System.out.println("The receiver declined the file.txt");

            } else if ("ERROR".equals(response.getStatus())) {
                int code = response.getCode();
                System.out.println("Private message failed with error code " + code + ": " + getErrorMessage(code));
            }

        } catch (JsonProcessingException e) {
            logger.error("An error occurred while processing");
        }
    }


    protected String getErrorMessage(int code) {
        return getMessage(code);
    }


    /**
     * Gets the message associated with the given code.
     *
     * @param code The code for which to get the message.
     * @return The message corresponding to the given code.
     */
    static String getMessage(int code) {
        return switch (code) {
            case USER_ALREADY_LOGGED_IN -> "User already logged in!";
            case USERNAME_INVALID_FORMAT_OR_LENGTH -> "Username has an invalid format or length!";
            case USER_CANNOT_LOGIN_TWICE -> "User cannot login twice!";
            case USER_NOT_LOGGED_IN -> "User is not logged in!";
            case PONG_TIMEOUT -> "Pong timeout!";
            case UNTERMINATED_MESSAGE -> "Unterminated message!";
            case PONG_WITHOUT_PING -> "Pong without ping";
            case YOUR_GUESS_IS_TOO_LOW -> "Too Low!";
            case YOUR_GUESS_IS_TOO_HIGH -> "Too High!";
            case NUMBER_OUT_OF_ALLOWED_RANGE -> "Out of allowed range!";
            case INVALID_USER_FORMAT -> "Invalid guess format!";
            case USER_IS_NOT_PARTICIPATING -> "User is not participating!";
            case GAME_TIMEOUT_WAS_REACHED -> "Timeout reached!";
            case NO_WINNER_ID_DETERMINED -> "No winner!";
            case NO_GAME_RESULTS_ARE_AVAILABLE -> "No result!";
            case SEND_TO_SELF_ERROR -> "Sending a private message to yourself is not allowed!";
            case EMPTY_MESSAGE_BODY_ERROR -> "Cannot send an empty private message!";
            case RECIPIENT_NOT_FOUND -> "Recipient not found!";
            case NO_RUNNING_GAME -> "There is actually no running game!";
            case USER_ALREADY_JOINED -> "You are already in the game!";
            case GAME_HAS_ALREADY_STARTED_CANNOT_JOIN -> "The game has already started, you cannot join anymore!";
            case INSUFFICIENT_PLAYERS_TO_START_THE_GAME -> "Not enough player to start the game!";
            default -> "Unknown error";
        };
    }

}
