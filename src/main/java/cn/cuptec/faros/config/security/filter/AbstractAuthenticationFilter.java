package cn.cuptec.faros.config.security.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    @Getter
    @Setter
    protected boolean postOnly = true;
    @Getter
    @Setter
    protected AuthenticationEventPublisher eventPublisher;
    @Getter
    @Setter
    protected AuthenticationEntryPoint authenticationEntryPoint;

    protected AbstractAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    protected Authentication authenticate(AbstractAuthenticationToken authenticationToken, HttpServletRequest request, HttpServletResponse response){
        Authentication authentication = null;
        try {
            authentication = this.getAuthenticationManager().authenticate(authenticationToken);
            logger.debug("Authentication success: " + authenticationToken);
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authenticationToken);

        } catch (Exception failed) {
            failed.printStackTrace();
            SecurityContextHolder.clearContext();
            logger.debug("Authentication request failed: " + failed);

            eventPublisher.publishAuthenticationFailure(new BadCredentialsException(failed.getMessage(), failed),
                    new PreAuthenticatedAuthenticationToken("access-token", "N/A"));
            try {
                authenticationEntryPoint.commence(request, response,
                        new UsernameNotFoundException(failed.getMessage(), failed));
            } catch (Exception e) {
                logger.error("authenticationEntryPoint handle error:{}", failed);
            }
        }
        return authentication;
    }

    protected void setDetails(HttpServletRequest request, AbstractAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

}
