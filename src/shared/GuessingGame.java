package shared;

import java.util.Map;

public class GuessingGame implements Response {

    public String message, status;

    public int code, result, guess;
    private Map<String, String> results;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setResult(int results) {this.result = results;}

    public int getResult() { return result;}

    public int getNumber() {
        return guess;
    }

    public void setNumber(int guess) { this.guess = guess;}

    public Map<String, String> getResults() {
        return results;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }

}
