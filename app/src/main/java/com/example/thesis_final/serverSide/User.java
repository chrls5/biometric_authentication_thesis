package com.example.thesis_final.serverSide;

import java.util.Objects;

/**
 * A representation of a user entity. Can also be used for json to class if needed in future.
 */
class User {
    private String username;
    private String pubKey;

    public String getUsername() {
        return username;
    }

    public User(String username,  String pubKey) {
        this.username = username;
        this.pubKey = pubKey;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, pubKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }


    public String getPubKey() {
        return pubKey;
    }

    void setPubKey(String pubKey){
        this.pubKey = pubKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", pubKey='" + pubKey + '\'' +
                '}';
    }
}
