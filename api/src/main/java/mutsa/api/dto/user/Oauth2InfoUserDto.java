package mutsa.api.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//oauth2로 로그인 시에 추가 정보를 위한 DTO
public class Oauth2InfoUserDto {
    private String phoneNumber;
    private String zipcode;
    private String city;
    private String street;
}

