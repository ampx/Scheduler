package scheduler.controller.auth.apikey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${apiKeyValue}")
    private String apiKeyValue;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        ApiKeyAuthFilter apiKeyAuthFilter = new ApiKeyAuthFilter();
        apiKeyAuthFilter.setAuthenticationManager(new AuthenticationManager(){

            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String requestProvidedKey = (String)authentication.getPrincipal();

                if (!apiKeyValue.equals(requestProvidedKey)){
                    throw new BadCredentialsException("invalid api key - provide api key in header " +
                            "with schedulerKey name");
                }
                authentication.setAuthenticated(true);
                return authentication;
            }
        });
        httpSecurity.
                antMatcher("/scheduler/**").
                csrf().disable().
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().addFilter(apiKeyAuthFilter).authorizeRequests().anyRequest().authenticated();
    }
}
