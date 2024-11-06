package shared;

import java.util.UUID;

public class FileTransferResponse implements Response{

    int code =0;
    String status;

    String sender;
    UUID transferId;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }
}
