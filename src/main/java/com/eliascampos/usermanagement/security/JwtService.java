package com.eliascampos.usermanagement.security;

import com.eliascampos.usermanagement.auth.InvalidRefreshTokenException;
import com.eliascampos.usermanagement.auth.dto.TokenResponse;
import com.eliascampos.usermanagement.user.User;
import com.eliascampos.usermanagement.user.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    public static final String TOKEN_TYPE_CLAIM = "type";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";

    private static final String ROLES_CLAIM = "roles";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshTokenDecoder;
    private final UserRepository userRepository;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtService(JwtEncoder jwtEncoder,
                      @Qualifier("refreshTokenDecoder") JwtDecoder refreshTokenDecoder,
                      UserRepository userRepository,
                      @Value("${app.security.jwt.issuer}") String issuer,
                      @Value("${app.security.jwt.ttl-seconds}") long accessTtlSeconds,
                      @Value("${app.security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenDecoder = refreshTokenDecoder;
        this.userRepository = userRepository;
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public TokenResponse issue(User user) {
        Instant now = Instant.now();
        return new TokenResponse(
                encode(user, ACCESS_TOKEN_TYPE, now.plusSeconds(accessTtlSeconds)),
                "Bearer",
                accessTtlSeconds,
                encode(user, REFRESH_TOKEN_TYPE, now.plusSeconds(refreshTtlSeconds)),
                refreshTtlSeconds);
    }

    public TokenResponse refresh(String refreshToken) {
        Jwt jwt;
        try {
            jwt = refreshTokenDecoder.decode(refreshToken);
        } catch (JwtException ex) {
            throw new InvalidRefreshTokenException();
        }

        // O dono do token precisa continuar existindo e ativo.
        return userRepository.findByEmail(jwt.getSubject())
                .filter(User::isActive)
                .map(this::issue)
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private String encode(User user, String type, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getEmail())
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .claim(TOKEN_TYPE_CLAIM, type)
                .claim(ROLES_CLAIM, List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
