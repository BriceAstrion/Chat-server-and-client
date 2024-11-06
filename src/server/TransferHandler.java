package server;


import java.net.Socket;


import static shared.Constant.*;


public class TransferHandler {

    private Socket senderSocket;
    private Socket receiverSocket;

    public TransferHandler(Socket senderSocket, Socket receiverSocket) {
        this.senderSocket = senderSocket;
        this.receiverSocket = receiverSocket;
    }





}
