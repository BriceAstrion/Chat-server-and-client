package shared;

public class EncryptedMessageRequest implements Sendable{
    String receiver ;

    String sender;



    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String sender) {
        this.receiver = sender;
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


}
