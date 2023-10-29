package mutsa.api.service.user;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.config.security.CustomPrincipalDetails;
import mutsa.api.dto.LoginResponseDto;
import mutsa.api.dto.auth.AccessTokenResponse;
import mutsa.api.dto.auth.LoginRequest;
import mutsa.api.dto.auth.TokenDto;
import mutsa.api.dto.user.*;
import mutsa.api.util.CookieUtil;
import mutsa.api.util.JwtTokenProvider;
import mutsa.api.util.JwtTokenProvider.JWTInfo;
import mutsa.common.domain.models.user.Role;
import mutsa.common.domain.models.user.RoleStatus;
import mutsa.common.domain.models.user.User;
import mutsa.common.domain.models.user.embedded.Address;
import mutsa.common.dto.user.UserInfoDto;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import mutsa.common.repository.cache.UserCacheRepository;
import mutsa.common.repository.redis.RefreshTokenRedisRepository;
import mutsa.common.repository.user.RoleRepository;
import mutsa.common.repository.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserModuleService userModuleService;
    private final UserRepository userRepository;
    private final UserCacheRepository userCacheRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public void signUp(SignUpUserDto signUpUserDto) {
        Optional<User> user = userRepository.findByUsername(signUpUserDto.getUsername());
        if (user.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATION_USER);
        }
        signUpUserDto.setPassword(bCryptPasswordEncoder.encode(signUpUserDto.getPassword()));

        User newUser = SignUpUserDto.from(signUpUserDto);
        Role role = roleRepository.findByRole(RoleStatus.ROLE_USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ROLE));

        newUser.setRole(role);

        userRepository.save(newUser);
    }

    @Transactional
    public void signUp(SignUpOAuth2UserDto signUpUserDto) {
        //추후에 oauth전용으로 따로 만들어서 관리해야함
        Optional<User> user = userRepository.findByUsername(signUpUserDto.getUsername());
        if (user.isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATION_USER);
        }
        signUpUserDto.setPassword(bCryptPasswordEncoder.encode(signUpUserDto.getPassword()));

        User newUser = SignUpOAuth2UserDto.from(signUpUserDto);
        Role role = roleRepository.findByRole(RoleStatus.ROLE_USER)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ROLE));

        newUser.setRole(role);
        userRepository.save(newUser);
    }


    public AccessTokenResponse validateRefreshTokenAndCreateAccessToken(
            String refreshToken,
            HttpServletRequest request
    ) {
        try {
            // JWT 토큰 검증 실패하면 JWTVerificationException 발생
            JWTInfo jwtInfo = jwtTokenProvider.decodeRefreshToken(refreshToken);

            // 저장된 리프레시 토큰 가져오기
            String storedRefresh = refreshTokenRedisRepository.getRefreshToken(jwtInfo.getUsername())
                    .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

            // 리프레시 토큰 비교
            if (!refreshToken.equals(storedRefresh)) {
                throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            }

            User user = fromJwtInfo(jwtInfo);

            TokenDto accessToken = jwtTokenProvider.createAccessToken(request,
                    customUserDetailsService.loadUserByUsername(user.getUsername()));

            log.info("Access Token : {}", accessToken);

            return AccessTokenResponse.builder()
                    .accessToken(accessToken.getToken())
                    .expiresTime(accessToken.getExpiredTime())
                    .build();

        } catch (JWTVerificationException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

    }

    @Transactional
    public void signupOauth(String currentUsername, Oauth2InfoUserDto signupAuthUserDto) {
        User user = userModuleService.getByUsername(currentUsername);
        Address address = Address.of(signupAuthUserDto.getZipcode(), signupAuthUserDto.getCity(), signupAuthUserDto.getStreet());
        user.updateAddress(address);
        user.setAvailable();
    }

    public UserInfoDto findUserInfo(String username) {
        log.info("findUserInfo {}", username);
        User byUsername = userModuleService.getByUsername(username);
        return UserInfoDto.fromEntity(byUsername);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        CustomPrincipalDetails user = (CustomPrincipalDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        if (user != null) {
            CookieUtil.removeCookie(request, response, CookieUtil.REFRESH_TOKEN);
            refreshTokenRedisRepository.removeRefreshToken(user.getUsername()); //저장소에서 삭제
        }
    }

    @Transactional
    public void changePassword(String currentUsername, PasswordChangeDto passwordChangeDto) {
        User findUser = findUsername(currentUsername);

        if (!bCryptPasswordEncoder.matches(passwordChangeDto.getPassword(), findUser.getPassword())) {
            throw new BusinessException(ErrorCode.DIFFERENT_PASSWORD);
        }

        if (passwordChangeDto.getPassword().equals(passwordChangeDto.getNewPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSOWRD);
        }

        findUser.updatePassword(bCryptPasswordEncoder.encode(passwordChangeDto.getNewPassword()));
    }

    @Transactional
    public LoginResponseDto login(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            LoginRequest loginDto
    ) throws IOException {
        CustomPrincipalDetails principalDetails = customUserDetailsService.loadUserByUsername(loginDto.getUsername());
        if (!bCryptPasswordEncoder.matches(loginDto.getPassword(), principalDetails.getPassword())) {
            throw new BusinessException(ErrorCode.DIFFERENT_PASSWORD);
        }

        //로그인 시에 유저 캐시화
        userCacheRepository.setUser(userModuleService.getByUsername(loginDto.getUsername()));

        TokenDto accessToken = jwtTokenProvider.createAccessToken(httpServletRequest, principalDetails);
        TokenDto refreshToken = jwtTokenProvider.createRefreshToken(httpServletRequest, loginDto.getUsername());
        refreshTokenRedisRepository.setRefreshToken(loginDto.getUsername(), refreshToken.getToken());

        CookieUtil.setCookie(httpServletResponse, JwtTokenProvider.REFRESH_TOKEN, refreshToken.getToken(), refreshToken.getExpiredTime());
        return new LoginResponseDto(principalDetails.getUsername(), accessToken.getToken(), accessToken.getExpiredTime());
    }

    public String refreshToken(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(jwtTokenProvider::isCookieNameRefreshToken)
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_IN_COOKIE));
    }

    @Transactional
    public void updateImageUrl(String currentUsername, ProfileChangeDto profileChangeDto) {
        User user = findUsername(currentUsername);

        user.updateImageUrl(profileChangeDto.getImageUrl().replace("\\", "").replace("\"", ""));
    }

    @Transactional
    public void updateEmail(String currentUsername, EmailChangeDto email) {
        User findUser = findUsername(currentUsername);
        findUser.updateEmail(email.getEmail());
    }

    @Transactional
    public void updateAddress(String currentUsername, Address address) {
        User findUser = findUsername(currentUsername);
        findUser.updateAddress(address);
    }

    public boolean isDuplicateEmail(String email) {
        //oauth 가 아닌 개인 계정으로 해당 이메일을 사용할떄
        Optional<User> byEmail = userModuleService.getByEmail(email);
        return byEmail.isPresent() && !byEmail.get().getIsOAuth2();
    }

    public boolean isNewOauth2User(String email) {
        //이미 중복된 유저 이메일이 있는 경우, oauth2 로그인을 한적이 있는 경우인데 정보가 추가로 필요한 경우
        Optional<User> byEmail = userModuleService.getByEmail(email);

        return byEmail.isEmpty();
    }

    public boolean isAvailableUser(String email) {
        //이미 중복된 유저 이메일이 있는 경우, oauth2 로그인을 한적이 있는 경우인데 정보가 추가로 필요한 경우
        Optional<User> byEmail = userModuleService.getByEmail(email);

        return byEmail.isEmpty() ||
                (byEmail.get().getIsOAuth2() && byEmail.get().getIsAvailable());
    }

    private User fromJwtInfo(JWTInfo jwtInfo) {
        return findUsername(jwtInfo.getUsername());
    }

    private User findUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}