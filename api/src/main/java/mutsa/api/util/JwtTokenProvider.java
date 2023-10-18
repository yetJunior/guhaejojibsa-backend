package mutsa.api.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.config.jwt.JwtConfig;
import mutsa.api.config.security.CustomPrincipalDetails;
import mutsa.api.dto.auth.TokenDto;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private static final String BEARER = "Bearer ";
    public static final String REFRESH_TOKEN = "refresh_token";
    private static final String AUTHORITIES = "authorities";
    private final JwtConfig jwtConfig;

    public TokenDto createAccessToken(HttpServletRequest request, CustomPrincipalDetails details) {
        Instant expiresAt = Instant.now().plusSeconds(Long.parseLong(jwtConfig.getAccessTokenExpire()));
        String accessToken = BEARER + JWT.create()
                .withSubject(details.getUsername())
                .withIssuer(request.getRequestURI())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(expiresAt))
                .withClaim(AUTHORITIES, getAuthorities(details.getAuthorities())) //이게 없으면 에러가 발생한다(CustomAuthorizationFilter)
                .sign(jwtConfig.getEncodedSecretKey());
        return new TokenDto(accessToken, expiresAt.getNano());
    }

    public TokenDto createRefreshToken(HttpServletRequest request, String subject) {
        Instant expiresAt = Instant.now().plusSeconds(Long.parseLong(jwtConfig.getRefreshTokenExpire()));
        String refreshToken = JWT.create()
                .withSubject(subject)
                .withIssuer(request.getRequestURI())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(expiresAt))
                .sign(jwtConfig.getEncodedSecretKey());
        return new TokenDto(refreshToken, expiresAt.getNano());
    }


    /**
     * 1. 토큰이 정상적인지 검증(위조, 만료 여부) 2. Access Token인지 Refresh Token인지 구분
     *
     * @param token
     * @return
     * @throws JWTVerificationException
     */
    public JWTInfo decodeToken(String token)
            throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(jwtConfig.getEncodedSecretKey()).build();

        DecodedJWT decodedJWT = verifier.verify(token);
        String username = decodedJWT.getSubject();

        String[] authoritiesJWT = decodedJWT.getClaim(AUTHORITIES)
                .asArray(String.class);

        if (authoritiesJWT != null) {
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            Arrays.stream(authoritiesJWT).forEach(authority -> {
                authorities.add(new SimpleGrantedAuthority(authority));
            });
        }

        return JWTInfo.builder()
                .username(username)
                .authorities(authoritiesJWT)
                .build();
    }

    public JWTInfo decodeRefreshToken(String refreshToken) {
        JWTVerifier verifier = JWT.require(jwtConfig.getEncodedSecretKey()).build();

        DecodedJWT decodedJWT = verifier.verify(refreshToken);
        String username = decodedJWT.getSubject();

        return JWTInfo.builder()
                .username(username)
                .build();
    }

    public boolean isCookieNameRefreshToken(Cookie cookie) {
        return JwtTokenProvider.REFRESH_TOKEN.equals(cookie.getName());
    }

    private static List<String> getAuthorities(Set<SimpleGrantedAuthority> authoritySet) {
        return authoritySet.stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(jwtConfig.getEncodedSecretKey()).build().verify(token);
            return true;
        } catch (SignatureException e) { // 유효하지 않은 JWT 서명
            log.info("not valid jwt signature");
        } catch (MalformedJwtException e) { // 유효하지 않은 JWT
            log.info("not valid jwt");
        } catch (ExpiredJwtException e) { // 만료된 JWT
            log.info("expired jwt");
        } catch (UnsupportedJwtException e) { // 지원하지 않는 JWT
            log.info("unsupported jwt");
        } catch (IllegalArgumentException e) { // 빈값
            log.info("empty jwt");
        }
        return false;
    }

    @Getter
    @Builder
    @ToString
    public static class JWTInfo {
        private final String username;
        private final String[] authorities;
    }
}
