package mutsa.api.dto.auth;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class TokenDto {
    private String token;
    private int expiredTime;
}
