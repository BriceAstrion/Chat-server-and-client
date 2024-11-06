package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JoinGameReq(String status, int code) {

    @Override
    public String toString() {
        return "JoinGameReq[" +
                "status=" + status + ", " +
                "code=" + code + ']';
    }


}
