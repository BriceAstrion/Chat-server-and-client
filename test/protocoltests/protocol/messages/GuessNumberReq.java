package protocoltests.protocol.messages;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

public final class GuessNumberReq {
    @JsonProperty("guess")
    private int guess;

    public GuessNumberReq() {}

    public GuessNumberReq(int guess) {
        this.guess = guess;
    }

    public int getGuess() {
        return guess;
    }

    public void setGuess(int guess) {
        this.guess = guess;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GuessNumberReq) obj;
        return this.guess == that.guess;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guess);
    }

    @Override
    public String toString() {
        return "GuessNumberReq[" +
                "guess=" + guess + ']';
    }
}
