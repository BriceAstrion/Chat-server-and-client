package server;

import java.io.*;
import java.net.*;
import java.util.*;

import static shared.Constant.*;

public class ServerSetUp {
    private static final Set<ClientHandler> users = new HashSet<>();
    private static final Set<ClientHandler> usersFileTransfer = new HashSet<>();


    public ServerSetUp() {
    }

    public void startServer() {
        startMainServer();
        startFileTransferServer();
    }

    private void startMainServer() {
        Thread mainServerThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(SERVER_PORT, MAX_PENDING, InetAddress.getByName(SERVER_ADDRESS));
                System.out.println("Starting server version " + VERSION + " is running on port " + SERVER_PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandler.start();
                    addUsers(clientHandler);
                }
            } catch (IOException e) {
                handleServerStartupError("main", e);
            }
        });
        mainServerThread.start();
    }

    private void startFileTransferServer() {
        Thread fileTransferThread = new Thread(() -> {
            try {
                ServerSocket fileTransferSocket = new ServerSocket(FILE_TRANSFER_PORT, MAX_PENDING, InetAddress.getByName(SERVER_ADDRESS));
                System.out.println("File transfer server is running on port " + FILE_TRANSFER_PORT);

                Socket senderSocket = null;
                Socket receiverSocket = null;

                while (true) {

                    Socket fileTransferClientSocket = fileTransferSocket.accept();
                    System.out.println("hello");


                    // Determine if it's the sender or the receiver based on the first byte
                    InputStream inputStream = fileTransferClientSocket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream);
                    byte[] buffer = new byte[1];
                    dataInputStream.readFully(buffer, 0, 1);
                    char firstByte = (char) buffer[0];
                    System.out.println(firstByte);

                    if (firstByte == 'S') {
                        // The connecting client is the sender
                        if (senderSocket == null) {
                            senderSocket = fileTransferClientSocket;
                            System.out.println("Sender connected");

                        } else {
                            // If a sender is already connected, reject this connection or handle appropriately
                            System.out.println("Another sender attempted to connect. Rejecting connection.");
                            fileTransferClientSocket.close();
                        }
                    } else if (firstByte == 'R') {
                        // The connecting client is the receiver
                        if (receiverSocket == null) {
                            receiverSocket = fileTransferClientSocket;
                            System.out.println("Receiver connected");
                            System.out.println(receiverSocket);
                        } else {
                            // If a receiver is already connected, reject this connection or handle appropriately
                            System.out.println("Another receiver attempted to connect. Rejecting connection.");
                            fileTransferClientSocket.close();
                        }
                    } else {
                        // Invalid first byte, handle appropriately
                        System.err.println("Invalid first byte received: " + firstByte);
                        fileTransferClientSocket.close();
                    }

                    // Once both sender and receiver are connected, start data transfer
                    if (senderSocket != null && receiverSocket != null) {
                        handleDataTransfer(senderSocket, receiverSocket);
                        // Reset sender and receiver sockets for next transfer
                        senderSocket = null;
                        receiverSocket = null;
                    }
                }
            } catch (IOException e) {
                handleServerStartupError("file.txt transfer", e);
            }
        });
        fileTransferThread.start();
    }

    private void handleServerStartupError(String serverType, IOException e) {
        System.err.println("Error occurred while starting the " + serverType + " server: " + e.getMessage());
        e.printStackTrace(System.err);
    }


    public static Set<ClientHandler> getUsers() {
        return users;
    }

    public static synchronized void addUsers(ClientHandler user) {
        users.add(user);
    }

    public static void removeUser(ClientHandler user) {
        users.remove(user);
    }

    public static boolean containsUser(String username) {

        for (ClientHandler clientHandler : users) {
            if (clientHandler != null && clientHandler.getUsername() != null && clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public static ClientHandler getUserByUsername(String username) {
        for (ClientHandler clientHandler : users) {
            if (clientHandler != null && clientHandler.getUsername() != null && clientHandler.getUsername().equals(username)) {
                return clientHandler;
            }
        }
        return null;
    }

    private void handleDataTransfer(Socket senderSocket, Socket receiverSocket) {
        try {
            // Reading from the sender
            InputStream senderInputStream = senderSocket.getInputStream();
            DataInputStream senderDataInputStream = new DataInputStream(senderInputStream);

            // Read the data from the sender
            byte[] senderDataBytes = new byte[36];
            senderDataInputStream.readFully(senderDataBytes);
            String senderData = new String(senderDataBytes);

            // Sending data to the receiver
            OutputStream receiverOutputStream = receiverSocket.getOutputStream();
            DataOutputStream receiverDataOutputStream = new DataOutputStream(receiverOutputStream);


            // Write the data to the receiver

            // Reading and writing file content
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = senderInputStream.read(buffer)) != -1) {
                receiverOutputStream.write(buffer, 0, bytesRead);
            }

            receiverDataOutputStream.flush();
            receiverDataOutputStream.close();
            receiverOutputStream.close();

            System.out.println("Data transferred from sender to receiver: " + senderData);
        } catch (IOException e) {
            // Handle any IO exceptions
            e.printStackTrace();
        }

    }
}

