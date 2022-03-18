package com.example.thesis_final;

public class CurrentState {
    private static String username;
    private static String pubKeyLatest;
    private static String privKeyLatest;
    private static String signedDataLatest;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        CurrentState.username = username;
    }

    public static String getPubKeyLatest() {
        return pubKeyLatest;
    }

    public static void setPubKeyLatest(String pubKeyLatest) {
        CurrentState.pubKeyLatest = pubKeyLatest;
    }

    public static String getPrivKeyLatest() {
        return privKeyLatest;
    }

    public static void setPrivKeyLatest(String privKeyLatest) {
        CurrentState.privKeyLatest = privKeyLatest;
    }

    public static String getSignedDataLatest() {
        return signedDataLatest;
    }

    public static void setSignedDataLatest(String signedDataLatest) {
        CurrentState.signedDataLatest = signedDataLatest;
    }

    public static void clearState() {
        setPubKeyLatest("");
        setSignedDataLatest("");
        setPrivKeyLatest("");
        setUsername("");
    }
}
