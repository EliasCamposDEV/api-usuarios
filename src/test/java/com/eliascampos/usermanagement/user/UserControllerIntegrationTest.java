package com.eliascampos.usermanagement.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@email.com";
    private static final String ADMIN_PASSWORD = "admin12345";
    private static final String DEFAULT_PASSWORD = "senha12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        userRepository.save(new User("Zeta Admin", ADMIN_EMAIL, UserRole.ADMIN,
                passwordEncoder.encode(ADMIN_PASSWORD)));
        adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void shouldCreateReadUpdateDeactivateAndDeleteAUser() throws Exception {
        MvcResult createdResult = perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Ana Souza",
                                "email", "ANA@Email.com",
                                "role", "USER",
                                "password", DEFAULT_PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andReturn();

        String id = readId(createdResult);

        perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Souza"))
                .andExpect(jsonPath("$.active").value(true));

        perform(put("/api/v1/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Ana Souza Lima",
                                "email", "ana.lima@email.com",
                                "role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Souza Lima"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        perform(patch("/api/v1/users/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("active", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isNoContent());

        perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldListUsersWithPaginationSearchAndFilters() throws Exception {
        createUser("Ana Souza", "ana@email.com", "USER");
        createUser("Bruno Lima", "bruno@email.com", "ADMIN");
        createUser("Carla Dias", "carla@email.com", "ADMIN");

        perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Ana Souza"));

        perform(get("/api/v1/users").param("search", "bruno"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("bruno@email.com"));

        perform(get("/api/v1/users").param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        perform(get("/api/v1/users").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() throws Exception {
        createUser("Ana Souza", "ana@email.com", "USER");

        perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Outra Ana",
                                "email", "ANA@EMAIL.COM",
                                "role", "USER",
                                "password", DEFAULT_PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void shouldValidateRequestBody() throws Exception {
        java.util.Map<String, Object> invalidBody = new java.util.HashMap<>();
        invalidBody.put("name", "");
        invalidBody.put("email", "email-invalido");
        invalidBody.put("role", null);

        perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalidBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.role").exists());
    }

    @Test
    void shouldReturnNotFoundForUnknownUser() throws Exception {
        perform(put("/api/v1/users/nao-existe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Usuario Teste",
                                "email", "teste@email.com",
                                "role", "USER"))))
                .andExpect(status().isNotFound());

        perform(delete("/api/v1/users/nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForUnknownPathAndMethodNotAllowedForWrongMethod() throws Exception {
        perform(get("/api/v1/caminho-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        perform(post("/api/v1/users/{id}", "algum-id"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    private void createUser(String name, String email, String role) throws Exception {
        perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name, "email", email, "role", role,
                                "password", DEFAULT_PASSWORD))))
                .andExpect(status().isCreated());
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return readTree(result).get("accessToken").asText();
    }

    private String readId(MvcResult result) throws Exception {
        String id = readTree(result).get("id").asText();
        assertThat(id).isNotBlank();
        return id;
    }

    private JsonNode readTree(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Map<String, ?> value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
