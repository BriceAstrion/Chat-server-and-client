package client;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static shared.Constant.*;


/**
 * The `Client` class represents a client in the messaging system.
 * It handles user input, communicates with the server, and manages various operations such as sending messages,
 * starting a guessing game, joining games, and file transfers.
 */
public class Client implements Runnable {

    private final Scanner s;
    private String username;
    private ClientManager clientManager ;
    private Boolean isLoggedIn = false;


    public Client() {
        clientManager = new ClientManager();
        s = new Scanner(System.in);
        username = "";

        Thread clientInputThread = new Thread(this);
        clientInputThread.start();
    }


    /**
     * The main execution thread for the client.
     * Handles user input, communicates with the server, and manages client operations.
     */
    @Override
    public void run() {
        try {
            while (!isLoggedIn) {
                System.out.print("Enter your username: ");
                username = s.nextLine();
                clientManager.loginRequest(username);
                isLoggedIn = clientManager.login();
            }

            Thread serverInputThread = new Thread(clientManager);
            serverInputThread.start();

            while (true) {
                Menu.showMenu();
                int userChoice = s.nextInt();
                handleUserChoice(userChoice);
            }
        } catch (SocketException e) {
            handleServerDisconnection();
        } catch (IOException ex) {
            logException("Error occurred while running ", ex);
        } finally {
            s.close();
        }
    }


    /**
     * Handles the user's choice of action based on the provided menu option.
     * @param userChoice The menu option chosen by the user.
     * @throws IOException If an I/O error occurs.
     */
    private void handleUserChoice(int userChoice) throws IOException {
        switch (userChoice) {
            case 0 -> clientManager.logout();
            case 1 -> clientManager.broadcastMessage(getUserInput());
            case 2 -> showHelp();
            case 3 -> requestListOfUsers();
            case 4 -> sendPrivateMessage();
            case 5 -> startGuessingGame();
            case 6 -> joinGuessingGame();
            case 7 -> sendGuess();
            case 8 -> handleHandshakeSending();
            case 9 -> clientManager.acceptFile();
            case 10 -> clientManager.rejectFile();
            case 11 -> sendEncryptedMessage();
            default -> System.out.println("Invalid choice. Please choose a valid option.");
        }
    }


    private void handleServerDisconnection() {
        System.out.println("Disconnected from the Server.");
        clientManager.logout();
        System.exit(0);
    }


    public static void main(String[] args) {
        Client client = new Client();
    }

    public void showHelp() {
        System.out.println("Available commands:");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("1." + " {\"message\":\"<message>\"}: Send a broadcast message to all connected clients");
        System.out.println("3." + " Request a list of all connected clients");
        System.out.println("4." + " {\"recipient\":\"<username>\", \"message\":\"<message>\"}: Send a private message to a specific client");
        System.out.println("5. Start a guessing game");
        System.out.println("6. Join an existing guessing game");
        System.out.println("7." + " <number>: Make a guess during a guessing game");
        System.out.println("8." + " {\"recipient\":\"<username>\", \"filename\":\"<filename>\"}: Send a file.txt to another user");
        System.out.println("9." + " {\"status\":\"OK\", \"transferId\":\"<UUID>\"}: Accept a pending file.txt transfer request");
        System.out.println("10." + " {\"status\":\"NO\", \"transferId\":\"<UUID>\"}: Reject a pending file.txt transfer request");
        System.out.println("11." + " {\"recipient\":\"<username>\", \"message\":\"<message>\"}: Send an encrypted message to another client");
        System.out.println("Logout: Log out and disconnect from the server");
        System.out.println("Help: Display this help menu");
        System.out.println();
    }

    public String getUserInput() {
        s.nextLine();
        System.out.println("Write you message : ");
        return s.nextLine();
    }

    public void requestListOfUsers() throws JsonProcessingException { clientManager.sendListUsersRequest(); }

    public void sendPrivateMessage() throws JsonProcessingException {
        System.out.println("Enter the receiver's username: ");
        String receiver = s.next();
        s.nextLine();
        System.out.print("Enter your private message: ");
        String message = s.nextLine();
        clientManager.sendPrivateMessage(receiver, message);
    }

    public void startGuessingGame() throws IOException { clientManager.sendStartGameRequest(); }

    private void logException(String message, Exception e) {
        clientManager.logException(message, e);
    }

    private void joinGuessingGame() throws JsonProcessingException { clientManager.sendJoinGame(); }

    private void sendGuess() throws JsonProcessingException {
        System.out.println("Enter your guess: ");
        int guess = s.nextInt();
        clientManager.sendGuesses(guess);
    }

    private void handleHandshakeSending(){
        System.out.println("Enter the receiver's username: ");
        String receiver = s.next();
        s.nextLine();
        System.out.print("Enter the filename: ");
        String filename = s.nextLine();

        try {
            clientManager.sendFile(receiver, filename);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendEncryptedMessage() throws JsonProcessingException {
        System.out.println("Extra Secure Message ***********************************");
        System.out.println("Enter the receiver's username: ");
        String receiver = s.next();
        s.nextLine();
        System.out.print("Enter your private message: ");
        String message = s.nextLine();
        clientManager.sendEncryptedMessage(receiver, message);
        clientManager.sentEncryptedMessage(message);
    }


}
