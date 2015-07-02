package net.coding.program.login.auth;

import android.net.Uri;
import android.util.Log;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by chenchao on 15/7/1.
 */
public class AuthInfo implements Serializable {

    public static final String LOCAL_TAG = "AuthInfo";
    private final String scheme;
    private final String path;
    private final String authority;
    private final String issuer;
    private final String secret;
    TotpClock clock;
    TotpCounter counter = new TotpCounter(PasscodeGenerator.INTERVAL);
    private String uriString;


    public AuthInfo(String uriString, TotpClock clock, TotpCounter totpCounter) {
        this.uriString = uriString;

        Uri uri = Uri.parse(uriString);
        scheme = uri.getScheme();
        path = uri.getPath();
        authority = uri.getAuthority();
        issuer = uri.getQueryParameter("issuer");
        secret = uri.getQueryParameter("secret");
        this.clock = clock;
        counter = totpCounter;
    }

    static Signer getSigningOracle(String secret) {
        try {
            byte[] keyBytes = decodeKey(secret);
            final Mac mac = Mac.getInstance("HMACSHA1");
            mac.init(new SecretKeySpec(keyBytes, ""));

            // Create a signer object out of the standard Java MAC implementation.
            return new Signer() {
                @Override
                public byte[] sign(byte[] data) {
                    return mac.doFinal(data);
                }
            };
        } catch (Base32String.DecodingException error) {
            Log.e(LOCAL_TAG, error.getMessage());
        } catch (NoSuchAlgorithmException error) {
            Log.e(LOCAL_TAG, error.getMessage());
        } catch (InvalidKeyException error) {
            Log.e(LOCAL_TAG, error.getMessage());
        }

        return null;
    }

    private static byte[] decodeKey(String secret) throws Base32String.DecodingException {
        return Base32String.decode(secret);
    }

    public String getUriString() {
        return uriString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthInfo authInfo = (AuthInfo) o;

        if (scheme != null ? !scheme.equals(authInfo.scheme) : authInfo.scheme != null)
            return false;
        if (path != null ? !path.equals(authInfo.path) : authInfo.path != null) return false;
        if (authority != null ? !authority.equals(authInfo.authority) : authInfo.authority != null)
            return false;
        return !(issuer != null ? !issuer.equals(authInfo.issuer) : authInfo.issuer != null);

    }

    @Override
    public int hashCode() {
        int result = scheme != null ? scheme.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (authority != null ? authority.hashCode() : 0);
        result = 31 * result + (issuer != null ? issuer.hashCode() : 0);
        return result;
    }

    public String getCode() {
        String code = "";
        try {
            Signer signer = getSigningOracle(secret);
            long state = counter.getValueAtTime(Utilities.millisToSeconds(clock.currentTimeMillis()));

            PasscodeGenerator pcg = new PasscodeGenerator(signer);
            code = pcg.generateResponseCode(state);
        } catch (Exception e) {
        }

        return code;
    }
}
