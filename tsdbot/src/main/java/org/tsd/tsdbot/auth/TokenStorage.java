package org.tsd.tsdbot.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.tsd.Constants;

import java.nio.charset.Charset;
import java.util.Date;

@Singleton
public class TokenStorage {

    private final byte[] signingBytes;

    @Inject
    public TokenStorage(@Named(Constants.Annotations.ENCRYPTION_KEY) String encryptionKey) {
        this.signingBytes = encryptionKey.getBytes(Charset.forName("UTF-8"));
    }

    public synchronized String putUser(User user) {
        return createJwt(user.getId(), user.getName(), "TSDHQ");
    }

    public synchronized String getUser(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(signingBytes)
                .parseClaimsJws(token)
                .getBody();
        return claims.getId();
    }

    private String createJwt(String id, String subject, String issuer) {
        SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;
        return Jwts.builder().setId(id)
                .setIssuedAt(new Date())
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(algorithm, signingBytes)
                .compact();
    }
}
