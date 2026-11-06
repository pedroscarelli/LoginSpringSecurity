package com.login.user.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.login.user.config.TokenServiceConfig;

@Service
public class TokenService {

    private final Algorithm algorithm;

    private final long jwtExpirationInSeconds;

    private final String jwtIssuer;

    public TokenService(TokenServiceConfig tokenServiceConfig) {
        this.algorithm = Algorithm.HMAC256(tokenServiceConfig.jwtSecret());
        this.jwtExpirationInSeconds = tokenServiceConfig.jwtExpirationInSeconds();
        this.jwtIssuer = tokenServiceConfig.jwtIssuer();
    }

    public String generateToken(UserDetails user) {
        var roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);

        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(user.getUsername())
            .withExpiresAt(Instant.now().plusSeconds(jwtExpirationInSeconds))
            .withArrayClaim("roles", roles)
            .sign(algorithm);
    }

    /** Returns the token subject (the user e-mail), or empty when the token is invalid or expired. */
    public Optional<String> validateToken(String token) {
        try {
            var subject = JWT.require(algorithm)
                .withIssuer(jwtIssuer)
                .build()
                .verify(token)
                .getSubject();

            return Optional.ofNullable(subject);
        } catch (JWTVerificationException exception) {
            return Optional.empty();
        }
    }
}
