package com.example.thesis_final;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;

/**
 * Generates a keypair from password along and also a certificate.
 * This is no related to the Android Keystore because they key is generated
 * from a third party library - bouncy castle.
 */
public class KeyPairGeneration {

    public static KeyPair generateKeyPairFromPwd(String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Security.removeProvider("BC");
        Security.addProvider(new BouncyCastleProvider());

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] seed = digest.digest(
                password.getBytes(StandardCharsets.UTF_8));

        FixedSecureRandom random = new FixedSecureRandom(seed);


        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec("secp256r1");
        keyGen.initialize(ecGenParameterSpec, random);
        return keyGen.generateKeyPair();
    }

    public static X509Certificate generateCertificate(KeyPair keyPair)
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
}
