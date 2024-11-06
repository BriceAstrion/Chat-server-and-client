package server;

import com.fasterxml.jackson.databind.*;
import shared.*;

import java.io.IOException;

import static server.ClientHandler.*;
import static shared.Constant.*;

public class PrivateMessageHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void handlePrivateMessage(ClientHandler sender, String message) {
        try {
            // Deserialize the private message request from the message payload
            PrivateMessageReq privateMessageRequest = objectMapper.readValue(message, PrivateMessageReq.class);

            // Extract information from the private message request
            String receiver = privateMessageRequest.getReceiver();
            String privateMessageContent = privateMessageRequest.getMessage();

            if (isUserLoggedIn(receiver)) {
                // Sender is trying to send a message to themselves
                if (sender.getUsername().equals(receiver)) {
                    PrivateMessageResp selfMessageError = new PrivateMessageResp();
                    selfMessageError.setStatus("ERROR");
                    selfMessageError.setCode(SEND_TO_SELF_ERROR);
                    selfMessageError.setMessage("Sender is not allowed to send messages to themselves");
                    sender.sendMessage(PRIVATE_MESSAGE_RESP, selfMessageError);

                    System.out.println(PRIVATE_MESSAGE + " {\"sender\": " + "\"" + sender.getUsername() + "\"" +
                            ", \"DM\": " + "\"" + privateMessageContent + "\"" + " }");
                    System.out.println(PRIVATE_MESSAGE_REQ + " {\"receiver\": " + "\"" +sender.getUsername()
                            + "\"" + " => \"NOT_ALLOWED\" }");

                    // Exit the method, don't proceed further
                    return;
                }

                // Empty message body
                if (privateMessageContent == null || privateMessageContent.trim().isEmpty()) {
                    // Send an error response if the message body is empty
                    PrivateMessageResp emptyMessageError = new PrivateMessageResp();
                    emptyMessageError.setStatus("ERROR");
                    emptyMessageError.setCode(EMPTY_MESSAGE_BODY_ERROR);
                    emptyMessageError.setMessage("Message body cannot be empty");
                    sender.sendMessage(PRIVATE_MESSAGE_RESP, emptyMessageError);

                    System.out.println(PRIVATE_MESSAGE + " {\"sender\": " + sender.getUsername() +
                            ", \"DM\": " + privateMessageContent + " }");
                    System.out.println(PRIVATE_MESSAGE_REQ + " {\"receiver\": " + "\"" + receiver +
                                    "\"" + ", \"DM\": \" \" => \"EMPTY_BODY_MESSAGE\" }");

                    // Exit the method, don't proceed further
                    return;
                }

                // Send the private message to the receiver
                ClientHandler receiverHandler = sender.getUserHandler(receiver);

                if (receiverHandler != null) {
                    // Create a private message object
                    PrivateMessage privateMessageObj = new PrivateMessage();
                    privateMessageObj.setSender(sender.getUsername());
                    privateMessageObj.setMessage(privateMessageContent);

                    // Create a response with the private message content
                    PrivateMessageResp privateMessageResponse = new PrivateMessageResp();
                    privateMessageResponse.setStatus("OK");
                    privateMessageResponse.setCode(0);
                    privateMessageResponse.setMessage(objectMapper.writeValueAsString(privateMessageObj));

                    // Send the private message content to the receiver
                    receiverHandler.sendMessage("PRIVATE_MESSAGE", privateMessageResponse);

                    // Log the private message
                    System.out.println(PRIVATE_MESSAGE + " {\"sender\": " + "\"" + sender.getUsername() + "\"" +
                            ", \"DM\": " + "\"" + privateMessageContent + "\"" + " }");
                    System.out.println(PRIVATE_MESSAGE_REQ + " {\"receiver\": " + "\"" + receiver + "\"" +
                            ", \"DM\": " + "\"" + privateMessageContent + "\"" + " }");

                    //Send a successful response to the sender
                    PrivateMessageResp response = new PrivateMessageResp();
                    response.setStatus("OK");
                    sender.sendMessage(PRIVATE_MESSAGE_RESP, response);
                }

            } else {
                // Recipient not found
                PrivateMessageResp response = new PrivateMessageResp();
                response.setStatus("ERROR");
                response.setCode(RECIPIENT_NOT_FOUND);
                response.setMessage("Recipient not found");
                sender.sendMessage(PRIVATE_MESSAGE_RESP, response);

                System.out.println(PRIVATE_MESSAGE + " {\"sender\": " + "\"" + sender.getUsername() + "\"" +
                        ", \"DM\": " + privateMessageContent + " }");
                System.out.println(PRIVATE_MESSAGE_REQ + " {\"receiver\": " + "\"" + receiver +
                        "\"" + " => \"NOT_FOUND\" }");
            }

        } catch (IOException e) {
            LOGGER.severe("Error while processing the private message: " + e.getMessage());
        }
    }
}
