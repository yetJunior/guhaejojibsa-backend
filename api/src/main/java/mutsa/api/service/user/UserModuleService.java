package mutsa.api.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mutsa.common.domain.models.user.User;
import mutsa.common.exception.BusinessException;
import mutsa.common.exception.ErrorCode;
import mutsa.common.repository.cache.UserCacheRepository;
import mutsa.common.repository.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static mutsa.common.exception.ErrorCode.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserModuleService {
    private final UserRepository userRepository;
    private final UserCacheRepository userCacheRepository;

    public User getByApiId(String uuid) {
        return userRepository.findByApiId(uuid)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    public User getByUsername(String username) {
        if (userCacheRepository.getUser(username).isEmpty()) {
            User user = userRepository.findByUsername(username).orElseThrow(() ->
                    new BusinessException(USER_NOT_FOUND));

            //유저 정보가 없는 경우 다시 캐싱한다.
            userCacheRepository.setUser(user);
        }
        return userCacheRepository.getUser(username).get();
    }

    public Optional<User> getByEmail(String email) {
        return userRepository
                .findByEmail(email);
    }
}
