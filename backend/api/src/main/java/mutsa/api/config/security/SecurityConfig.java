package mutsa.api.config.security;

import lombok.RequiredArgsConstructor;
import mutsa.api.config.security.filter.CustomAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAuthorizationFilter customAuthorizationFilter;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> httOAuth2AuthorizationRequestAuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .exceptionHandling(handle ->
                handle.authenticationEntryPoint(authenticationEntryPoint))
            .addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        httpSecurity.sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.authorizeHttpRequests(registry -> registry.anyRequest().permitAll());

        httpSecurity.oauth2Login(oAuth2LoginConfigurer ->
            oAuth2LoginConfigurer
                .authorizationEndpoint(
                    authorizationEndpointConfig ->
                        authorizationEndpointConfig.authorizationRequestRepository(
                                httOAuth2AuthorizationRequestAuthorizationRequestRepository)
                            .baseUri("/oauth2/authorization"))
                .redirectionEndpoint(redirectionEndpointConfig ->
                    redirectionEndpointConfig.baseUri("/login/oauth2/callback/**"))
                .userInfoEndpoint(userInfoEndpointConfig ->
                    userInfoEndpointConfig.userService(defaultOAuth2UserService))
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
        );

        httpSecurity.formLogin(login ->
            login.loginProcessingUrl("/api/auth/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)
                .usernameParameter("username")
                .passwordParameter("password")
        );

        return httpSecurity.build();
    }
}
