package mutsa.api.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.config.security.CustomPrincipalDetails;
import mutsa.api.util.JwtTokenProvider;
import mutsa.api.util.JwtTokenProvider.JWTInfo;
import mutsa.common.domain.models.user.User;
import mutsa.common.exception.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private static final String BEARER = "Bearer ";

    private final List<String> EXCLUDE_URL_LIST =
            List.of("/api/auth/token/refresh",
                    "/api/auth/login",
                    "/ws"
            );

    private final HashSet<String> EXCLUDE_URL = new HashSet<>(EXCLUDE_URL_LIST);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        log.info("필터 타는지 확인 여부  : {}", request.getRequestURL());
        return EXCLUDE_URL.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            token = getToken(authorizationHeader);
        }


        JWTInfo jwtInfo = null;
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            jwtInfo = jwtTokenProvider.decodeToken(token);
            SecurityContextHolder.getContext().setAuthentication(getAuthenticationToken(jwtInfo));
        } else {
            log.info("유효한 JWT토큰이 없습니다.");
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthenticationToken(JWTInfo jwtInfo) {
        return new UsernamePasswordAuthenticationToken(
                CustomPrincipalDetails.of(
                        User.of(
                                jwtInfo.getUsername(),
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        null
                ),
                null,
                Arrays.stream(jwtInfo.getAuthorities())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
    }

    private static void getAccessTokenExpired(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(errorResponse.getStatus());
        Map<String, String> body = new HashMap<>();
        body.put("message", errorResponse.getMessage());
        body.put("status", Integer.toString(errorResponse.getStatus()));
        body.put("code", errorResponse.getCode());
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

    private String getToken(String tokenHeader) {
        return tokenHeader.substring(BEARER.length());
    }

}
