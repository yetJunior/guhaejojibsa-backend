package mutsa.api.controller.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mutsa.api.config.security.CustomPrincipalDetails;
import mutsa.api.dto.user.Oauth2InfoUserDto;
import mutsa.api.dto.user.PasswordChangeDto;
import mutsa.api.service.user.UserService;
import mutsa.common.domain.models.user.embedded.Address;
import mutsa.common.dto.user.UserInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserInfoDto> findUserInfo(@AuthenticationPrincipal CustomPrincipalDetails user) {
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(userService.findUserInfo(user.getUsername()),
                HttpStatus.OK);
    }

    @PatchMapping("/password")
    public ResponseEntity changePassword(@AuthenticationPrincipal CustomPrincipalDetails user,
                                         @Validated @RequestBody PasswordChangeDto passwordChangeDto) {
        userService.changePassword(user, passwordChangeDto);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/oauth/signup")
    public ResponseEntity signUpOauthUser(@AuthenticationPrincipal CustomPrincipalDetails user,
                                          @Validated @RequestBody Oauth2InfoUserDto signupAuthUserDto) {
        userService.signupOauth(user, signupAuthUserDto);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/image")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity updateImageUrl(@AuthenticationPrincipal UserDetails user, @RequestBody String raw) {
        userService.updateImageUrl(user, raw);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/email")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity updateEmail(@AuthenticationPrincipal UserDetails user, @RequestBody String email) {
        userService.updateEmail(user, email);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/address")
    @PreAuthorize("hasAuthority('user.update')")
    public ResponseEntity updateAddress(@AuthenticationPrincipal UserDetails user, @RequestBody Address address) {
        userService.updateAddress(user, address);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
