package com.example.thesis_final.serverSide;


/**
 * Works as an API connecting server side and client side.
 * Client side will only use (has access) on these functions.
 * These functions will make the calls to the server side functions
 */
public class UsersService {

    public static UsersRegistered dbConn;

    public static Response addNewUser(String username, String pubKey) {

        if (username != null && pubKey != null) {
            if (dbConn.isUserRegistered(username))
                return new Response(false, "User is already registered!");  //TODO false or true?
            else {
                dbConn.addUser(new User(username, pubKey));
                return new Response(true, "User added with biometrics associated.");
            }
        } else {
            return new Response(false, "Username or password is not set.");
        }
    }

    public static Response verifyUserUsingBiometrics(String username, byte[] digest) {
        if (username != null && digest != null) {
            if (dbConn.isUserRegistered(username)) {
                if (dbConn.verifyUsingBiometrics(username, digest)) {
                    return new Response(true, "User successfully logged in using biometrics!");
                } else return new Response(false, "User couldn't be verified.");
            } else return new Response(false, "User is not registered.");
        } else return new Response(false, "Usermame or the key is not set.");
    }


    public static Response verifyUserUsingPassword(String username, String password) {
        if (username != null && password != null) {
            if (dbConn.isUserRegistered(username)) {
                if (dbConn.verifyUsingPassword(username, password))
                    return new Response(true, "User successfully logged in using password!");
                else return new Response(false, "User couldn't be verified.");
            } else return new Response(false, "User is not registered.");
        } else return new Response(false, "Usermame or the password is not set.");
    }

    public static Response addBiometricsToUser(String username, String pubKey) {
        if (username != null && pubKey != null) {
            if (dbConn.isUserRegistered(username)) {
                if (dbConn.addPubKeyToUser(username, pubKey))
                    return new Response(true, "Biometrics successfully associated!");
                else return new Response(false, "User was not found.");
            } else return new Response(false, "User is not registered.");
        } else return new Response(false, "Something was not set.");
    }


}
