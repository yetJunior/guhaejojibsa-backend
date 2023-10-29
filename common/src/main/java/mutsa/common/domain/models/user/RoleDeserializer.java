package mutsa.common.domain.models.user;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;

@Slf4j
public class RoleDeserializer extends JsonDeserializer<Role> {
    @Override
    public Role deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.readValueAsTree();
        log.info("role 생성 부분 {}", node);

        // Role 객체 생성 및 필드 설정
        HashSet<Role> set = new HashSet<>();
        Role role = new Role();
        if (node.isArray()) {
            JsonNode roleArray = node.get(0);
            role = Role.of(RoleStatus.valueOf(roleArray.get("role").asText()));
            set.add(role);
        }

        return role;
    }
}
