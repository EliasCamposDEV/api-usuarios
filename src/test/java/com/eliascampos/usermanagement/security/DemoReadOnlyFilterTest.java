package com.eliascampos.usermanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoReadOnlyFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final DemoReadOnlyFilter filter = new DemoReadOnlyFilter(objectMapper);

    @Test
    void shouldAllowReadAndAuthRequests() throws Exception {
        assertPasses("GET", "/api/v1/users");
        assertPasses("GET", "/api/v1/users/abc");
        assertPasses("POST", "/auth/login");
        assertPasses("POST", "/auth/refresh");
        assertPasses("POST", "/auth/logout");
    }

    @Test
    void shouldBlockCreateWithProblemDetail() throws Exception {
        MockHttpServletResponse response = assertBlocked("POST", "/api/v1/users");

        assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains(DemoReadOnlyFilter.CODE), body);
        assertTrue(body.contains("Demo somente leitura"), body);
        assertTrue(body.contains("\"status\":403"), body);
        assertTrue(body.contains("\"instance\":\"/api/v1/users\""), body);
    }

    @Test
    void shouldBlockEveryWriteOnUsers() throws Exception {
        assertBlocked("PUT", "/api/v1/users/abc");
        assertBlocked("PATCH", "/api/v1/users/abc/status");
        assertBlocked("DELETE", "/api/v1/users/abc");
        assertBlocked("PUT", "/api/v1/me/password");
    }

    @Test
    void shouldIgnoreWritesOutsideTheApi() throws Exception {
        assertPasses("POST", "/actuator/health");
    }

    private void assertPasses(String method, String uri) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request(method, uri), new MockHttpServletResponse(), chain);
        assertNotNull(chain.getRequest(), method + " " + uri + " deveria passar pelo filtro");
    }

    private MockHttpServletResponse assertBlocked(String method, String uri) throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request(method, uri), response, chain);

        assertNull(chain.getRequest(), method + " " + uri + " deveria ser bloqueado");
        assertEquals(403, response.getStatus());
        return response;
    }

    private MockHttpServletRequest request(String method, String uri) {
        return new MockHttpServletRequest(method, uri);
    }
}
