package mutsa.common.dto.user;

import lombok.*;
import mutsa.common.domain.models.user.User;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

import static mutsa.common.constants.ImageConstants.DEFAULT_AVATAR_IMAGE;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserInfoDto {

    private String username;
    private String apiId;
    private String email;
    private String nickname;
    private String image_url;
    private Set<String> role;
    private String zipcode;
    private String city;
    private String street;

    public static UserInfoDto fromEntity(User user) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.username = user.getUsername();
        userInfoDto.apiId = user.getApiId();
        userInfoDto.email = user.getEmail();
        userInfoDto.nickname = user.getUsername();
        userInfoDto.image_url = getImage(user.getImageUrl());
        userInfoDto.role = user.getRole()
                .stream().map(o -> o.getRole().name())
                .collect(Collectors.toSet());

        if (user.getAddress() != null) {
            userInfoDto.zipcode = user.getAddress().getZipcode();
            userInfoDto.city = user.getAddress().getCity();
            userInfoDto.street = user.getAddress().getStreet();
        }

        return userInfoDto;
    }

    private static String getImage(String imageUrl) {
        if (StringUtils.hasText(imageUrl)) {
            return imageUrl;
        }
        return DEFAULT_AVATAR_IMAGE;
    }
}