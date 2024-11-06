package shared;

import java.util.*;

public class ListOfUsers implements Response {

    public int code;

    public String status,
            message,
            userListMessage;
    public List<String> users;



    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getUsers() { return users; }

    public void setUsers(List<String> users) { this.users = users; }

    public String getUserListMessage() {
        return userListMessage;
    }

    public void setUserListMessage(String userListMessage) {
        this.userListMessage = userListMessage;
    }
}
