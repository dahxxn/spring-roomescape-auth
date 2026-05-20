package roomescape.auth.token;

import static io.jsonwebtoken.security.Keys.hmacShaKeyFor;
import static java.nio.charset.StandardCharsets.UTF_8;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.sql.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.common.exception.UnauthorizedException;

@Component
public class TokenProvider {
    private final SecretKey secretKey;
    private final long expirationTime;

    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long expirationTime
    ) {
        this.secretKey = hmacShaKeyFor(secretKey.getBytes(UTF_8));
        this.expirationTime = expirationTime;
    }

    public String createToken(Long memberId){
        Date now = new Date(System.currentTimeMillis());
        Date expiredAt = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(now)
                .expiration(expiredAt)
                .signWith(secretKey)
                .compact();
    }

    public Long extractMemberId(String token){
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    private Claims parseClaims(String token){
        try{
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch(RuntimeException exception){
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }
}
