package com.portfolio.erp.infrastructure.out.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.portfolio.erp.application.config.JwtProperties;
import com.portfolio.erp.domain.model.Role;
import com.portfolio.erp.domain.model.User;
import com.portfolio.erp.domain.ports.out.TokenIssuerPort;

@Component
public class JwtTokenIssuer implements TokenIssuerPort {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public JwtTokenIssuer(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    @Override
    public IssuedToken issue(User user) {
        Instant now = Instant.now();
        Duration ttl = properties.accessTokenTtl();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("fullName", user.fullName())
                .claim("roles", user.getRoles().stream().map(Role::name).sorted().toList())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, ttl.toSeconds());
    }
}
