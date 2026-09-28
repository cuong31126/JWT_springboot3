package vn.iotstar.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("nimbusJwtService")
@Primary
@ConditionalOnProperty(name = "security.jwt.provider", havingValue = "nimbus", matchIfMissing = true)
public class NimbusJwtService implements JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private byte[] getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            return padded;
        }
        return keyBytes;
    }

    @Override
    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse token: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issuer("vn.iotstar")
                    .issueTime(new Date(System.currentTimeMillis()))
                    .expirationTime(new Date(System.currentTimeMillis() + jwtExpiration));

            if (extraClaims != null) {
                for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
                    builder.claim(entry.getKey(), entry.getValue());
                }
            }

            JWTClaimsSet claimsSet = builder.build();
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);

            JWSSigner signer = new MACSigner(getSigningKey());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error signing JWT token with Nimbus: " + e.getMessage(), e);
        }
    }

    @Override
    public long getExpirationTime() {
        return jwtExpiration;
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(getSigningKey());

            if (!signedJWT.verify(verifier)) {
                return false;
            }

            Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                return false;
            }

            String username = signedJWT.getJWTClaimsSet().getSubject();
            return (username != null && username.equals(userDetails.getUsername()));
        } catch (Exception e) {
            return false;
        }
    }
}
