package com.colavite.gestor_investimento.controller;

import com.colavite.gestor_investimento.entity.Role;
import com.colavite.gestor_investimento.entity.Usuario;
import com.colavite.gestor_investimento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtEncoder jwtEncoder;

    @BeforeEach
    void clearUsers() {
        usuarios.deleteAll();
    }

    @Test
    void registerCreatesNormalizedUserWithoutHash() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" Ana \",\"username\":\" Ana.User \",\"email\":\" ANA@EXAMPLE.COM \",\"password\":\"senha-segura\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ana.user"))
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        Usuario user = usuarios.findByUsername("ana.user").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo("senha-segura");
        assertThat(passwordEncoder.matches("senha-segura", user.getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsDuplicatesAndRolePayload() throws Exception {
        usuarios.saveAndFlush(new Usuario("Ana", "ana", "ana@example.test", passwordEncoder.encode("senha"), Role.USER));
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outra\",\"username\":\"ANA\",\"email\":\"outra@example.test\",\"password\":\"senha\"}"))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outra\",\"username\":\"outra\",\"email\":\"outra@example.test\",\"password\":\"senha\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginAndProtectedEndpointsRespectRoles() throws Exception {
        Usuario user = usuarios.saveAndFlush(new Usuario("Ana", "ana", "ana@example.test", passwordEncoder.encode("senha"), Role.USER));
        String userToken = login("ana", "senha");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(user.getId()));
        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + userToken)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/metrics").header("Authorization", "Bearer " + userToken)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/users")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/carteiras")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanReadNonSecretAdministrativeDataAndInvalidTokensAreUnauthorized() throws Exception {
        usuarios.saveAndFlush(new Usuario("Admin", "admin", "admin@example.test", passwordEncoder.encode("senha"), Role.ADMIN));
        String token = login("admin", "senha");

        mockMvc.perform(get("/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].password").doesNotExist());
        mockMvc.perform(get("/admin/metrics").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer invalid.token.value")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"missing\",\"password\":\"senha\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredAndSignatureAlteredJwt() throws Exception {
        Usuario user = usuarios.saveAndFlush(new Usuario("Ana", "ana", "ana@example.com", passwordEncoder.encode("senha"), Role.USER));
        Instant now = Instant.now();
        String expired = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder().subject(user.getId().toString()).claim("username", "ana").claim("role", "USER")
                        .issuedAt(now.minusSeconds(120)).expiresAt(now.minusSeconds(60)).build())).getTokenValue();
        String valid = login("ana", "senha");
        String[] tokenParts = valid.split("\\.");
        String signature = tokenParts[2];
        String altered = tokenParts[0] + "." + tokenParts[1] + "."
                + (signature.startsWith("A") ? "B" : "A") + signature.substring(1);

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + expired)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + altered)).andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("accessToken").asString();
    }
}
