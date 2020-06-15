package com.thingverse.api.annotations;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.security.Principal;
import java.util.Collection;

/**
 * `
 *
 * @author Arun Patra
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Collection<GrantedAuthority> authorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList(customUser.role());
        // this is epic
        Principal principal = () -> ("" + customUser.userName());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal,
                null,
                authorities);
        context.setAuthentication(auth);
        return context;
    }
}