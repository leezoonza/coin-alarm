package com.zoonza.coinalarm.member.internal.adapter.out.security;

import com.zoonza.coinalarm.member.internal.application.dto.IssuedAccessToken;
import com.zoonza.coinalarm.member.internal.application.port.out.AccessTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenAdapter implements AccessTokenPort {
    private final JwtEncoder jwtEncoder;
    private final AuthenticationProperties properties;

    @Override
    public IssuedAccessToken issue(Long memberId, Instant issuedAt) {
        Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(memberId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new IssuedAccessToken(token, expiresAt);
    }
}
