package mutsa.api.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mutsa.api.dto.user.EmailChangeDto;
import mutsa.api.dto.user.Oauth2InfoUserDto;
import mutsa.api.dto.user.PasswordChangeDto;
import mutsa.api.dto.user.ProfileChangeDto;
import mutsa.api.service.user.UserService;
import mutsa.api.util.SecurityUtil;
import mutsa.common.domain.models.user.embedded.Address;
import mutsa.common.dto.user.UserInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> findUserInfo() {
        return new ResponseEntity<>(userService.findUserInfo(SecurityUtil.getCurrentUsername()),
                HttpStatus.OK);
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@Validated @RequestBody PasswordChangeDto passwordChangeDto) {
        userService.changePassword(SecurityUtil.getCurrentUsername(), passwordChangeDto);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/oauth/signup")
    public ResponseEntity<?> signUpOauthUser(@Validated @RequestBody Oauth2InfoUserDto signupAuthUserDto) {
        userService.signupOauth(SecurityUtil.getCurrentUsername(), signupAuthUserDto);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/image")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<?> updateImageUrl(@RequestBody ProfileChangeDto profileChangeDto) {
        userService.updateImageUrl(SecurityUtil.getCurrentUsername(), profileChangeDto);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/email")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<?> updateEmail(@RequestBody EmailChangeDto email) {
        userService.updateEmail(SecurityUtil.getCurrentUsername(), email);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/address")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity<?> updateAddress(@RequestBody Address address) {
        userService.updateAddress(SecurityUtil.getCurrentUsername(), address);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
