package shared;

public class EncryptedMessage implements Sendable{

      String receiver;
      String sessionKey;
      String encryptedMessage;
      String sender;


    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(String encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

}
