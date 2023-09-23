package mutsa.common.repository.user;


import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import mutsa.common.customRepository.Querydsl4RepositorySupport;
import mutsa.common.domain.models.user.User;
import mutsa.common.dto.user.UserInfoDto;
import org.springframework.util.StringUtils;

import static mutsa.common.domain.models.user.QRole.role;
import static mutsa.common.domain.models.user.QUser.user;
import static mutsa.common.domain.models.user.QUserRole.userRole;

public class CustomUserRepositoryImpl extends Querydsl4RepositorySupport implements
        CustomUserRepository {

    public CustomUserRepositoryImpl() {
        super(User.class);
    }

    private static BooleanExpression eqUsername(String username) {
        return StringUtils.hasText(username) ? user.username.eq(username) : null;
    }

    @Override
    public UserInfoDto findUserInfo(String username) {
        return select(
                Projections.constructor(UserInfoDto.class,
                        user.username,
                        user.apiId,
                        user.email,
                        user.username,
                        user.imageUrl,
                        role.value,
                        user.address.zipcode,
                        user.address.city,
                        user.address.street))
                .from(user)
                .leftJoin(user.userRoles, userRole)
                .leftJoin(userRole.role, role)
                .where(eqUsername(username))
                .fetchOne();
    }
}
