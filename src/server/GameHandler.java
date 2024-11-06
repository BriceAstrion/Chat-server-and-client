package server;

import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import shared.*;

import static server.ClientHandler.*;
import static shared.Constant.*;

public class GameHandler {
    private static ClientHandler clientHandler;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<ClientHandler, Long> playersTimestamp = new HashMap<>();
    private static GameState gameState = GameState.IDLE;
    private static int numberToGuess;
    private static Timer gameTimer = new Timer();
    private static long gameStartTime;
    private static final List<ClientHandler> clientsInGame = new ArrayList<>();

    public GameHandler(ClientHandler clientHandler) {
        GameHandler.clientHandler = clientHandler;
    }


    /**
     * Initiates the guessing game upon a client's request.
     * Validates the request and notifies connected clients about the game initiation.
     *
     * @param requester The ClientHandler representing the client requesting to start the game.
     */
    public static void startGuessingGame(ClientHandler requester) {
        // Validate the game initiation request and check the game state
        if (validateRequestForGame(requester.getUsername())) {
            // Notify the requester then inform other connected users about the ongoing game
            acknowledgeRequestForGame(requester);
            // Schedule a waiting time for 10 seconds to allow others to join
            startJoinTimer();
            // Add the requester to the game while waiting for others to join
            clientsInGame.add(requester);
        }
    }


    private static boolean validateRequestForGame(String requester) {
        if (!isUserLoggedIn(requester)) {
            sendGameErrorResponse(6000);
            return false;

        } else if (gameState != GameState.IDLE) {
            sendGameErrorResponse(6003);
            return false;
        }

        return true;

    }


    private static void sendGameErrorResponse(int code) {
        GameNotification gameNotification = new GameNotification();
        gameNotification.setStatus("ERROR");
        gameNotification.setCode(code);
        clientHandler.sendMessage(GAME_NOTIFICATION, gameNotification);
    }


    private static void acknowledgeRequestForGame(ClientHandler requester) {
        // Check if the requester is already in the game
        if (!clientsInGame.contains(requester)) {
            // Send an acknowledgment message
            StartGuessingGame gameResponse = new StartGuessingGame();
            gameResponse.setStatus("OK");
            requester.sendMessage(START_GAME_RESP, gameResponse);

            // Log user initiation
            System.out.println(requester.getUsername() + " --> " + START_GAME_REQ);

            // Send a game initiation message to users and log their join
            sendGameInitiationMessageToUsers(requester.getUsername());
        }
    }


    private static void sendGameInitiationMessageToUsers(String initiatorUsername) {
        for (ClientHandler client : ServerSetUp.getUsers()) {
            if (client != null && client.getUsername() != null && !client.getUsername().equals(initiatorUsername)) {
                GameNotification gameNotification = new GameNotification();
                gameNotification.setStatus("OK");
                gameNotification.setMessage("A guessing game has been initiated. Join now!");

                client.sendMessage(GAME_NOTIFICATION, gameNotification);
            }
        }
    }


    /**
     * Allows a client to join the ongoing guessing game.
     * Validates the request and adds the client to the list of participants.
     *
     * @param client The ClientHandler representing the client joining the game.
     */
    public static void joinGuessingGame(ClientHandler client) {
        // Check if the user is logged in
        if (!isUserLoggedIn(client.getUsername())) {
            sendErrorResponse(client, USER_NOT_LOGGED_IN);
            return;
        }

        // Check if a game has been requested
        if (gameState != GameState.REQUESTED) {
            sendErrorResponse(client, NO_RUNNING_GAME);
            return;
        }

        // Check if the user has already joined the game
        if (clientsInGame.contains(client)) {
            sendErrorResponse(client, USER_ALREADY_JOINED);
            return;
        }

        // Add the client to the list of participants in the game
        clientsInGame.add(client);

        // Notify the client that they have successfully joined the game
        GameNotification message = new GameNotification();
        message.setStatus("OK");
        client.sendMessage(JOIN_GAME_RESP, message);

        // Log user join request and message in the server console
        String joinRequestLog = client.getUsername() + " --> " + JOIN_GAME_REQ;
        String joinMessageLog = client.getUsername() + " has joined the game.";
        System.out.println(joinRequestLog);
        System.out.println(joinMessageLog);
    }


    private static void sendErrorResponse(ClientHandler client, int errorCode) {
        GameNotification errorMessage = new GameNotification();
        errorMessage.setStatus("ERROR");
        errorMessage.setCode(errorCode);
        client.sendMessage(GAME_NOTIFICATION, errorMessage);
    }


    /**
     * Starts a timer to wait for additional players to join the game.
     * Checks the number of players and either start the game or reset it if insufficient participants.
     */
    private static void startJoinTimer() {
        gameState = GameState.REQUESTED;

        Timer joiningTimer = new Timer();
        joiningTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (clientsInGame.size() >= 2) {
                    checkPlayersAndStartRounds();
                } else {
                    notifyRequesterGameCanceled();
                    resetGuessingGame(false);
                }
            }
        }, JOINING_TIME_MS);
    }


    private static void notifyRequesterGameCanceled() {
        ClientHandler requester = clientsInGame.get(0);
        GameNotification notification = new GameNotification();
        notification.setStatus("ERROR");
        notification.setCode(INSUFFICIENT_PLAYERS_TO_START_THE_GAME);
        requester.sendMessage(GAME_NOTIFICATION, notification);
    }

    private static void checkPlayersAndStartRounds() {
        if (clientsInGame.size() >= 2) {
            startPlayRounds();
        } else {
            resetGuessingGame(true);
        }
    }


    /**
     * Initiates the game rounds, selects the target number, and starts the guessing timer.
     * Sets the game state to RUNNING and notifies participants about the start of the game.
     */
    private static void startPlayRounds() {
        gameState = GameState.RUNNING;
        sendPlayerList();
        numberToGuess = generateRandomNumber();
        startGuessingGameTimer();
    }


    private static void sendPlayerList() {
        List<String> playersInGame = clientsInGame.stream()
                .map(ClientHandler::getUsername)
                .toList();

        sendIndividualJoinNotifications();
        sendGameAnnouncement(playersInGame);
        sendUserList(playersInGame);
        sendGameDetailsNotifications();
    }


    private static void sendIndividualJoinNotifications() {
        for (ClientHandler client : clientsInGame) {
            JoinGame individualNotification = new JoinGame();
            individualNotification.setStatus("OK");
            client.sendMessage(JOIN_GAME_RESP, individualNotification);
        }
    }


    private static void sendGameAnnouncement(List<String> playersInGame) {
        // Send a broadcast message to announce the game and list of participants
        GameNotification gameNotification = new GameNotification();
        gameNotification.setStatus("OK");

        // Format the message with a dashed border
        StringBuilder message = new StringBuilder("--------------------\n");
        message.append("The game has started with the following participants: \n");

        for (String player : playersInGame) {
            message.append("- ").append(player).append("\n");
        }

        message.append("--------------------");

        gameNotification.setMessage(message.toString());

        // Send the formatted message to all clients
        for (ClientHandler client : clientsInGame) {
            client.sendMessage(GAME_NOTIFICATION, gameNotification);
        }
    }


    private static void sendUserList(List<String> playersInGame) {
        // Send a list of users to each client to let them see with whom they are playing
        ListOfUsers listUsersResponse = new ListOfUsers();
        listUsersResponse.setUsers(playersInGame);

        for (ClientHandler client : clientsInGame) {
            client.sendMessage(LIST_USERS_RESP, listUsersResponse);
        }
    }


    private static void sendGameDetailsNotifications() {
        GameNotification gameDetailsNotification = new GameNotification();
        gameDetailsNotification.setStatus("OK");
        gameDetailsNotification.setMessage("You can now make a guess between " + LOWER_BOUND + " & " + UPPER_BOUND + ". Good Luck!");

        for (ClientHandler client : clientsInGame) {
            client.sendMessage(GAME_NOTIFICATION, gameDetailsNotification);
        }
    }


    private static int generateRandomNumber() {
        Random random = new Random();
        return random.nextInt(UPPER_BOUND - LOWER_BOUND + 1) + LOWER_BOUND;
    }


    private static void startGuessingGameTimer() {
        gameStartTime = System.currentTimeMillis();
        gameTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                gameState = GameState.INFORM_USERS;
                try {
                    publishResults("TIMEOUT");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        gameTimer.schedule(task, GAME_TIMEOUT_MILLISECONDS);
    }


    /**
     * Resets the game state to IDLE and notifies all clients about the insufficient players to start the game.
     * Clears the list of participants.
     */
    private static void resetGuessingGame(boolean timeoutReached) {
        gameState = GameState.IDLE;

        // Construct the appropriate notification based on the cancellation reason
        GameNotification resetNotification = new GameNotification();

        resetNotification.setCode(INSUFFICIENT_PLAYERS_TO_START_THE_GAME);
        System.out.println(GAME_NOTIFICATION + " {GAME CANCELLED => PLAYERS < 2}");

        if (timeoutReached) {
            resetNotification.setCode(GAME_TIMEOUT_WAS_REACHED);
            System.out.println(GAME_NOTIFICATION + " {TIMEOUT == 2MIN }");
        }

        // Send the notification to all clients
        for (ClientHandler client : ServerSetUp.getUsers()) {
            client.sendMessage(GAME_NOTIFICATION, resetNotification);
        }

        // Clear the list of participants
        clientsInGame.clear();
    }


    /**
     * Processes incoming guesses from the client.
     * Sends appropriate responses to the client based on the guess result.
     *
     * @param payload The payload containing the client's guess.
     * @param guesser The ClientHandler representing the guessing client.
     */
    public static void checkClientGuess(String payload, ClientHandler guesser) {
        // Validate the guesser and ensure they are eligible to make a guess
        if (!validateGuess(guesser)) {
            return;
        }

        GuessingGame guess;

        try {
            guess = objectMapper.readValue(payload, GuessingGame.class);

            // Log the received guess in the server console
            logReceivedGuess(guesser, guess);

            // Check if the guess is within the allowed range; if not, send an error response
            if (isGuessOutOfRange(guess, guesser)) {
                return;
            }

            // Process the client's guess and send appropriate responses
            processClientGuess(guess, guesser);

        } catch (JsonProcessingException e) {
            sendNumberOutOfRangeError(guesser);
        }
    }

    private static void logReceivedGuess(ClientHandler guesser, GuessingGame guess) {
        System.out.println(guesser.getUsername() + " --> " + GUESS_NUMBER_REQ + " {" +
                "\"number\": " + guess.getNumber() + "}");
    }

    private static void processClientGuess(GuessingGame guess, ClientHandler guesser) {
        // Create a response object for the client's guess
        GuessingGame guessResp = new GuessingGame();
        guessResp.setResult(Integer.compare(guess.getNumber(), numberToGuess));

        // Log the server's response to the client's guess
        logServerResponse(guesser, guessResp);

        // Provide additional information based on the comparison of the guess with the target number
        sendGuessStatus(guess, guessResp, guesser);

        // If the guess is correct, record the timestamp and check for game completion
        handleCorrectGuess(guessResp, guesser);
    }

    private static void logServerResponse(ClientHandler guesser, GuessingGame guessResp) {
        System.out.println(guesser.getUsername() + " <-- " + GUESS_NUMBER_RESP + " {" +
                "\"result\": " + guessResp.getResult() + "}");
    }

    private static void sendGuessStatus(GuessingGame guess, GuessingGame guessResp, ClientHandler guesser) {
        GameNotification guessNum = new GameNotification();
        guessNum.setStatus("OK");

        if (guess.getNumber() < numberToGuess) {
            guessNum.setMessage("Guessed Number: " + guess.getNumber());
            guessResp.setStatus("TOO_LOW");

        } else if (guess.getNumber() > numberToGuess) {
            guessNum.setMessage("Guessed Number: " + guess.getNumber());
            guessResp.setStatus("TOO_HIGH");

        } else {
            guessNum.setMessage("Guessed Number: " + guess.getNumber());
            guessResp.setStatus("CORRECT");
        }

        guesser.sendMessage(GAME_NOTIFICATION, guessNum);
        guesser.sendMessage(GUESS_NUMBER_RESP, guessResp);
    }

    private static void handleCorrectGuess(GuessingGame guessResp, ClientHandler guesser) {
        if (guessResp.getResult() == 0) {
            recordTimeStamp(guesser);
            checkForCompletion();
        }
    }


    private static void sendNumberOutOfRangeError(ClientHandler guesser) {
        GuessingGame errorResponse = new GuessingGame();
        errorResponse.setCode(NUMBER_OUT_OF_ALLOWED_RANGE);
        guesser.sendMessage(GUESS_NUMBER_RESP, errorResponse);
    }


    private static boolean isGuessOutOfRange(GuessingGame guess, ClientHandler guesser) {
        int guessNumber = guess.getNumber();

        if (guessNumber < LOWER_BOUND || guessNumber > UPPER_BOUND) {
            GuessingGame errorResponse = new GuessingGame();
            errorResponse.setStatus("OUT_OF_RANGE");
            guesser.sendMessage(GUESS_NUMBER_RESP, errorResponse);

            return true;
        }

        return false;
    }


    private static boolean validateGuess(ClientHandler guesser) {
        if (!isUserLoggedIn(guesser.getUsername())) {
            sendErrorResponse(guesser, USER_NOT_LOGGED_IN);
            return false;
        }

        if (gameState != GameState.RUNNING) {
            sendErrorResponse(guesser, NO_RUNNING_GAME);
            return false;
        }

        if (!clientsInGame.contains(guesser)) {
            sendErrorResponse(guesser, NOT_A_PARTICIPANT);
            return false;
        }

        return true;
    }


    private static void recordTimeStamp(ClientHandler guesser) {
        long timeTaken = System.currentTimeMillis() - gameStartTime;
        playersTimestamp.put(guesser, timeTaken);
    }


    private static void checkForCompletion() {
        if (playersTimestamp.size() == clientsInGame.size()) {
            gameState = GameState.INFORM_USERS;
            gameTimer.cancel();
            try {
                publishResults("ALL_GUESSED_CORRECTLY");
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Publishes the results of the guessing game, including players' times and sorts the leaderboard.
     * Constructs a GameResultsResponse and sends it to all clients.
     * Clears game data and resets the game state to IDLE.
     *
     * @param result The result string indicating the reason for game completion (e.g., TIMEOUT, ALL_GUESSED_CORRECTLY).
     */
    private static void publishResults(String result) throws JsonProcessingException {
        // Map to store players' usernames and their corresponding times
        Map<String, String> results = new HashMap<>();

        // Populate the result map with usernames and times (default to "-timed out-" for non-participants.)
        for (ClientHandler client : clientsInGame) {
            String time = "-timed out-";

            if (playersTimestamp.containsKey(client)) {
                time = playersTimestamp.get(client).toString() + " ms";
            }

            results.put(client.getUsername(), time);
        }

        // Sort the result leaderboard based on time and username
        results = sortLeaderboard(results);

        // Log the game results in the server console
        for (Map.Entry<String, String> entry : results.entrySet()) {
            System.out.println(entry.getKey() + " <-- " + GAME_RESULTS + " " + objectMapper.writeValueAsString(results));
        }

        // Construct a GameResultsResponse object
        GuessingGame gameResultsResponse = new GuessingGame();
        gameResultsResponse.setStatus("OK");
        gameResultsResponse.setResults(results);

        // Send the results to all clients
        for (ClientHandler client : clientsInGame) {
            client.sendMessage(GAME_RESULTS, gameResultsResponse);
        }

        // Clear data and reset game state
        clientsInGame.clear();
        playersTimestamp.clear();
        gameState = GameState.IDLE;
    }


    /**
     * Sorts the leaderboard map based on player times and usernames.
     * Players who timed out are placed at the end, followed by players with shorter times.
     * Players who didn't participate are sorted based on their usernames.
     *
     * @param map The unsorted leaderboard map containing players' usernames and times.
     * @return A sorted LinkedHashMap based on the specified criteria.
     */
    private static Map<String, String> sortLeaderboard(Map<String, String> map) {
        List<Map.Entry<String, String>> entries = new ArrayList<>(map.entrySet());

        // Comparator for sorting entries based on timeout status, time, and then username
        Comparator<Map.Entry<String, String>> comparator = Comparator
                .comparing((Map.Entry<String, String> entry) -> "-timed out-".equals(entry.getValue()))
                .thenComparing(Map.Entry::getValue, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Map.Entry::getKey);

        // Sort the entries using the comparator
        entries.sort(comparator);

        // Construct a new LinkedHashMap to store the sorted entries
        Map<String, String> sortedMap = new LinkedHashMap<>();
        entries.forEach(entry -> sortedMap.put(entry.getKey(), entry.getValue()));

        return sortedMap;
    }


}
