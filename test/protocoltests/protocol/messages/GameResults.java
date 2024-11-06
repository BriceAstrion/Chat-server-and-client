package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

public class GameResults {
    @JsonProperty("status")
    private String status;
    @JsonProperty("code")
    private int code;
    @JsonProperty("results")
    private Map<String, String> results;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map<String, String> getResults() {
        return results;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }

}
