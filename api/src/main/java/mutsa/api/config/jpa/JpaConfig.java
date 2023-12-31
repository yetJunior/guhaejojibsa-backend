package mutsa.api.config.jpa;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@Component
@EnableJpaRepositories(
        basePackages = "mutsa.common.repository"
)
public class JpaConfig {
}
