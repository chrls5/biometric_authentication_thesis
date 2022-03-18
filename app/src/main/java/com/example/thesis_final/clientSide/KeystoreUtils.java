package com.example.thesis_final.clientSide;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.thesis_final.CurrentState;
import com.example.thesis_final.serverSide.Response;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

public class KeystoreUtils {

    private static final String TAG = "KeyStoreUtils";
    private static final String alias = "bio_auth_login";


    private static KeyStore loadKeystore(byte[] keystoreBytes)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks;
        ks = KeyStore.getInstance("AndroidKeystore");
        if (keystoreBytes != null) {
            ks.load(new ByteArrayInputStream(keystoreBytes), "android".toCharArray());
        } else {
            ks.load(null);
        }
        return ks;
    }

    ////////  REGISTER USAGE   //////////// these two methods can be moved to a common class
    public static KeyPair generateKeyPairFromPwd(String password) throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.removeProvider("BC");
        Security.addProvider(new BouncyCastleProvider());

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] seed = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));

        FixedSecureRandom random = new FixedSecureRandom(seed);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        keyGen.initialize(256, random);
        return keyGen.generateKeyPair();
    }

    private static X509Certificate generateCertificate(KeyPair keyPair)
            throws OperatorCreationException, CertificateException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException {
        String issuerString = "C=DE, O=datenkollektiv, OU=Planets Debug Certificate";
        // subjects name - the same as we are self signed.
        String subjectString = "C=DE, O=datenkollekitv, OU=Planets Debug Certificate";
        X500Name issuer = new X500Name(issuerString);
        BigInteger serial = BigInteger.ONE;
        Date notBefore = new Date();
        Date notAfter = new Date(System.currentTimeMillis() + (2 * 365 * 24 * 60 * 60 * 1000l));
        X500Name subject = new X500Name(subjectString);
        PublicKey publicKey = keyPair.getPublic();
        JcaX509v3CertificateBuilder v3Bldr = new JcaX509v3CertificateBuilder(issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                publicKey);
        X509CertificateHolder certHldr = v3Bldr
                .build(new JcaContentSignerBuilder("SHA1WITHECDSA").setProvider("BC").build(keyPair.getPrivate()));
        X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHldr);
        cert.checkValidity(new Date());
        cert.verify(keyPair.getPublic());
        return cert;
    }

    public static String getPubKeyFromKeystore() {
        try {
            KeyStore keystore = loadKeystore(null);
            Key key = keystore.getKey(alias, "password".toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keystore.getCertificate(alias);

                // Get public key
                PublicKey publicKey = cert.getPublicKey();

                return Base64.toBase64String(publicKey.getEncoded());
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException ioException) {
            ioException.printStackTrace();
        }

        return null;
    }

    public static void generateKeyPairAndStoreToKeystore(String password) {
        KeyStore ks;
        try {
            ks = loadKeystore(null);
            KeyPair keyPair = generateKeyPairFromPwd(password);
            X509Certificate certificate = generateCertificate(keyPair);
            ks.setKeyEntry(alias,
                    keyPair.getPrivate(),
                    null,
                    new X509Certificate[]{
                            certificate
                    });

            Log.e(TAG, Base64.toBase64String(keyPair.getPublic().getEncoded()));
            Log.e(TAG, Base64.toBase64String(keyPair.getPrivate().getEncoded()));
            CurrentState.setPubKeyLatest( Base64.toBase64String(keyPair.getPublic().getEncoded()));
            CurrentState.setPrivKeyLatest(Base64.toBase64String(keyPair.getPrivate().getEncoded()));

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | NoSuchProviderException | InvalidKeyException | SignatureException | OperatorCreationException e) {
            e.printStackTrace();
        }
    }


    ////////     LOGIN USAGE    ////////////
    private static PrivateKey getPrivateKey() throws KeyStoreException, UnrecoverableEntryException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore keyStore = loadKeystore(null);
        if (keyStore.containsAlias(alias)) {
            KeyStore.Entry entry = keyStore.getEntry(alias, null);
            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            return privateKey;
        }
        return null;
    }

    public static void signMessage(String msg, View v, BiometricPrompt.AuthenticationCallback authCallBack) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(getPrivateKey());
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setDeviceCredentialAllowed(false)
                    .setNegativeButtonText("Cancel?")
                    .setTitle("Signing")
                    .build();
            BiometricPrompt bp = new BiometricPrompt((FragmentActivity) v.getContext(), ContextCompat.getMainExecutor(v.getContext()), authCallBack);
            bp.authenticate(promptInfo, new BiometricPrompt.CryptoObject(signature));

        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableEntryException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }



    public static Response isSensorAvailable(Context context) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                BiometricManager biometricManager = BiometricManager.from(context);
                switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
                    case BiometricManager.BIOMETRIC_SUCCESS:
                        return new Response(true, "App can authenticate using biometrics.");
                    case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                        return new Response(false, "No biometric features available on this device.");
                    case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                        return new Response( false, "Biometric features are currently unavailable.");
                    case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                        // Prompts the user to create credentials that your app accepts.
                        return new Response( false, "No fingerprints detected. Enroll one.");
                }
            } else {
                return new Response(false,"Unsupported android version" );
            }
        } catch (Exception e) {
            System.out.println("Error detecting biometrics availability: " + e.getMessage());
        }
        return new Response(false,"Error detecting biometrics availability" );
    }


}