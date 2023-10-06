package mutsa.api.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.dto.user.SignUpUserDto;
import mutsa.api.service.user.CustomUserDetailsService;
import mutsa.api.service.user.UserService;
import mutsa.api.util.CookieUtil;
import mutsa.api.util.JwtTokenProvider;
import mutsa.common.domain.models.user.embedded.OAuth2Type;
import mutsa.common.repository.redis.RefreshTokenRedisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${frontendUrl}")
    private String frontendUrl;

    @Override
    // 인증 성공시 호출되는 메소드
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        boolean isNewUser = false;
        String provider = oAuth2User.getAttribute("provider");
        String email = oAuth2User.getAttribute("email");
        String nickname = oAuth2User.getAttribute("nickname");
        String username = email.split("@")[0];
        String authName = String.format("{%s}%s", provider, username);
        String picture = oAuth2User.getAttribute("picture");

        log.info("nickName : {}", nickname);
        log.info("oauthName : {} ", authName);

        //이미 해당 이메일로 회원가입한 유저가 있는데 oauth로그인이 아닌경우
        if (userService.existUserByEmail(email) && !userService.isOauthUser(email)) {
            log.warn("유저가 이미 해당 이메일로 가입한 이력이 존재합니다.");
            response.setContentType("text/html; charset=utf-8");
            PrintWriter out = response.getWriter();
            out.printf("<script>alert('이미 회원가입한 이메일 입니다.'); location.href='%s';</script>", frontendUrl);
            out.flush();
            return;
        }

        // 처음으로 소셜 로그인한 사용자를 데이터베이스에 등록
        if (!userService.existUserByEmail(email)) {
            isNewUser = true;
            //전화번호랑 ,도로
            userService.signUp(new SignUpUserDto(
                    username,
                    passwordEncoder.encode(email + "_" + provider),
                    passwordEncoder.encode(email + "_" + provider),
                    nickname,
                    email,
                    "",
                    "",
                    "",
                    ""
            ), authName, picture, OAuth2Type.valueOf(provider.toUpperCase()));
        }

        // 데이터베이스에서 사용자 회수
        CustomPrincipalDetails customPrincipalDetails = customUserDetailsService.loadUserByUsername(username);

        // JWT 생성
        String accessToken = jwtTokenProvider.createAccessToken(request, customPrincipalDetails);

        if (refreshTokenRedisRepository.getRefreshToken(username).isEmpty()) {
            String token = jwtTokenProvider.createRefreshToken(request, username);
            refreshTokenRedisRepository.setRefreshToken(username, token);
        }
        String refreshToken = refreshTokenRedisRepository.getRefreshToken(username).get();

        ResponseCookie cookie = CookieUtil.createCookie(refreshToken);

        response.setStatus(200);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());


        // 목적지 URL 설정
        // 우리 서비스의 Frontend 구성에 따라 유연하게 대처해야 한다.
        String targetUrl = String.format("%s/oauth2-redirect?token=%s&&isNewUser=%s", frontendUrl, accessToken, isNewUser);

        log.info("url : {}", targetUrl);
        // 실제 Redirect 응답 생성
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
