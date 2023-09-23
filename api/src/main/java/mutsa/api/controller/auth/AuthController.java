package mutsa.api.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.dto.LoginResponseDto;
import mutsa.api.dto.auth.AccessTokenResponse;
import mutsa.api.dto.auth.LoginRequest;
import mutsa.api.service.user.UserService;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    //인증이 필요하지 않은 서비스들

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(HttpServletRequest request, HttpServletResponse response, @Validated @RequestBody LoginRequest loginRequest) throws IOException {
        LoginResponseDto login = userService.login(request, response, loginRequest);
        return ResponseEntity.ok(login);
    }

    /**
     * 만료된 리프레시 토큰 재발급
     * @param request
     * @return
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<AccessTokenResponse> reIssuerAccessToken(
            HttpServletRequest request
    ) {
        if (request.getCookies() == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_IN_COOKIE);
        }

        String refreshToken = userService.refreshToken(request);
        AccessTokenResponse accessTokenResponse = userService.validateRefreshTokenAndCreateAccessToken(refreshToken, request);
        log.info("{}",accessTokenResponse.getAccessToken());
        return ResponseEntity.ok(accessTokenResponse);
    }

}
