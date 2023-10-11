package mutsa.api.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import mutsa.common.domain.models.user.User;
import mutsa.common.domain.models.user.embedded.OAuth2Type;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpOAuth2UserDto {

    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String checkPassword;
    @NotEmpty
    private String nickname;
    @NotEmpty
    private String email;
    @NotEmpty
    private String oauthName;
    @NotEmpty
    private String picture;
    @NotEmpty
    private OAuth2Type oAuth2Type;


    public static User from(SignUpOAuth2UserDto dto) {
        return User.of(
                dto.getUsername(),
                dto.getPassword(),
                dto.getEmail(),
                dto.getOauthName(),
                dto.getOAuth2Type(),
                dto.getPicture(),
                dto.getNickname()
        );
    }
}

