package mutsa.api.config.oauth2;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public abstract class OAuth2UserInfo {

    protected Map<String, Object> attributes;

    public abstract String getProvider();
    public abstract String getId();
    public abstract String getEmail();
    public abstract String getName();
    public abstract String getImageUrl();

}