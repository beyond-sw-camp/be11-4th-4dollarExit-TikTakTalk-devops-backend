package com.TTT.TTT.Common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
//  AccessToken 관련 설정값
    private final String secretKey;
    private final int expiration;
    private final Key SECRET_KEY;
//  RefreshToken 관련 설정값
    private final int expirationRt;
    private final String secretKeyRt;
    private final Key SECRET_KEY_RT;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey, @Value("${jwt.expiration}") int expiration,
                            @Value("${jwt.expirationRt}")int expirationRt, @Value("${jwt.secretKeyRt}")String secretKeyRt) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());

        this.expirationRt = expirationRt;
        this.secretKeyRt = secretKeyRt;
        SECRET_KEY_RT = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String loginId, String role){
        Claims claims = Jwts.claims().setSubject(loginId);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expiration*60*1000L))
                .signWith(SECRET_KEY)
                .compact();
        return token;
    }

    public String createRefreshToken(String loginId, String role){
        Claims claims = Jwts.claims().setSubject(loginId);
        claims.put("role", role);
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expirationRt*60*1000L))
                .signWith(SECRET_KEY_RT)
                .compact();
        return refreshToken;

    }
}
