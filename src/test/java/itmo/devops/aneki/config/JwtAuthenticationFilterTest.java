package itmo.devops.aneki.config;

import itmo.devops.aneki.error.ApiException;
import itmo.devops.aneki.service.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, "access");

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilterAuthPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldFilterProtectedPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/jokes");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void doFilterSetsAuthenticationForValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/jokes");
        request.setCookies(new Cookie("access", "token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();
        when(jwtService.extractUserId("token")).thenReturn("user-id");

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user-id");
    }

    @Test
    void doFilterReturnsUnauthorizedOnInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/jokes");
        request.setCookies(new Cookie("access", "bad"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();
        when(jwtService.extractUserId("bad"))
                .thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterSkipsWhenNoCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/jokes");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
