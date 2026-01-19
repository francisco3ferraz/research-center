package pt.ipleiria.dei.ei.estg.researchcenter.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TokenIssuer {
    public static final byte[] SECRET_KEY =
            "veRysup3rstr0nginv1ncible5ecretkeY@researchcenter.dae.ipleiria".getBytes();
    protected static final String ALGORITHM = "HMACSHA384";
    public static final long EXPIRY_MINS = 60L;

    public static String issue(String username) {
        LocalDateTime expiryPeriod = LocalDateTime.now().plusMinutes(EXPIRY_MINS);
        Date expirationDateTime = Date.from(
                expiryPeriod.atZone(ZoneId.systemDefault()).toInstant()
        );

        Key key = Keys.hmacShaKeyFor(SECRET_KEY);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationDateTime)
                .signWith(key, SignatureAlgorithm.HS384)
                .compact();
    }
}