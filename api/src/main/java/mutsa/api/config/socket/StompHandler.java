package mutsa.api.config.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.api.util.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (!StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String token = Objects.requireNonNull(accessor.getFirstNativeHeader("Authorization"))
                    .substring(7);

            JwtTokenProvider.JWTInfo jwtInfo = null;
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                jwtInfo = jwtTokenProvider.decodeToken(token);
            } else {
                log.info("[소켓] 유효한 JWT토큰이 없습니다.");
            }

            // WebSocket 세션에 사용자 정보 저장
            accessor.getSessionAttributes().put("username", jwtInfo.getUsername());
        }
        return message;
    }
}
