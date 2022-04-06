package com.example.thesis_final.serverSide;

import static com.example.thesis_final.KeyPairGeneration.generateKeyPairFromPwd;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.thesis_final.CurrentState;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Server side class which should run on server side (e.g. php)
 */
public class UsersService {


    private static final String TAG = "USERS_REGISTERED";

    private List<User> usersRegistered;

    public UsersService() {
        retrieveFromDB();
    }

    public void retrieveFromDB() {
        usersRegistered = new ArrayList<>();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getChildren() != null &&
                        dataSnapshot.getChildren().iterator().hasNext()) {

                    dataSnapshot.getChildren().forEach(x -> {
                                if (x.child("pubKey").getValue() == null)
                                    return;

                                usersRegistered.add(
                                        new User(
                                                x.getKey(),
                                                x.child("pubKey").getValue().toString()));
                            }
                    );
                    Log.e(TAG, String.valueOf(usersRegistered.get(2)));
                } else
                    Log.e(TAG, "not exists");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    void addUser(User e) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = mDatabase.child("users");
        usersRef.child(e.getUsername()).child("pubKey").setValue(e.getPubKey());
        usersRegistered.add(e); //for local sync otherwise call retrieveFromDB
    }

    boolean isUserRegistered(String username) {
        return usersRegistered.contains(new User(username, ""));
    }

    User getUserByUsername(String username) {
        int positionIfExists = usersRegistered.indexOf(new User(username, ""));
        if (positionIfExists == -1) {
            Log.e(TAG, "User does not exists");
            return null;
        }
        User currentUser = usersRegistered.get(positionIfExists);
        return currentUser;
    }

    boolean verifyUsingPassword(String username, String password) {

        int positionIfExists = usersRegistered.indexOf(new User(username, ""));
        if (positionIfExists == -1) {
            Log.e(TAG, "User does not exists");
            return false;
        }

        String pubKeyUser = usersRegistered.get(positionIfExists).getPubKey();

        try {
            if (pubKeyUser.equals(Base64.toBase64String(generateKeyPairFromPwd(password).getPublic().getEncoded())))
                return true;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
        }

        return false;
    }

    boolean verifyUsingBiometrics(String username, byte[] signedData) {

        User currentUser = getUserByUsername(username);

        boolean authenticated = verifySignedMessageUsingPukKey(convertStringToPubKey(currentUser.getPubKey()), signedData);
        if (authenticated) {
            CurrentState.setPubKeyLatest(currentUser.getPubKey());
            String signedString = Base64.toBase64String(signedData);
            signedString = signedString.replaceAll("\r", "").replaceAll("\n", "");
            CurrentState.setSignedDataLatest(signedString);
        }
        return authenticated;
    }

    private boolean verifySignedMessageUsingPukKey(PublicKey pubKey, byte[] signedData) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA");
            sig.initVerify(pubKey);
            sig.update("something".getBytes(StandardCharsets.UTF_8));
            boolean authenticated = sig.verify(signedData);
            return authenticated;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PublicKey convertStringToPubKey(String key) {
        try {
            byte[] byteKey = Base64.decode(key.getBytes());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(X509publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    boolean addPubKeyToUser(String username, String pubKey) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = mDatabase.child("users");

        usersRef.child(username).child("pubKey").setValue(pubKey);

        //Locally sync otherwise call retrievefromdb
        User curr = getUserByUsername(username);
        if (curr == null)
            return false;
        curr.setPubKey(pubKey);
        return true;
    }

}


