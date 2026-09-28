package vn.iotstar;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import vn.iotstar.models.LoginResponse;
import vn.iotstar.models.LoginUserModel;
import vn.iotstar.models.RegisterUserModel;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.services.JwtService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSpringboot3ApplicationTests {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testNimbusJwtTokenLifecycle() {
        assertNotNull(jwtService, "JwtService bean should be injected");

        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());

        // 1. Generate token
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        System.out.println("Generated Nimbus JWT Token: " + token);

        // 2. Extract username
        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);

        // 3. Validate token
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid, "Token should be valid for the user");

        // 4. Verify invalid user
        UserDetails otherUser = new User("other@example.com", "password", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser), "Token should not be valid for different user");
    }

    @Test
    void testFullAuthenticationFlow() throws Exception {
        String uniqueEmail = "sinhvien_" + System.currentTimeMillis() + "@hcmute.edu.vn";

        // Bước 1: Signup
        RegisterUserModel registerUser = new RegisterUserModel(uniqueEmail, "123456", "Nguyen Huu Trung Demo");
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.fullName").value("Nguyen Huu Trung Demo"));

        // Bước 2: Login
        LoginUserModel loginUser = new LoginUserModel(uniqueEmail, "123456");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String token = loginResponse.getToken();
        assertNotNull(token);
        System.out.println("Received token from login: " + token);

        // Bước 3: Access /users/me with Bearer token
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.fullName").value("Nguyen Huu Trung Demo"));

        // Bước 4: Access /users/ with Bearer token
        mockMvc.perform(get("/users/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Bước 5: Truy cập không có token vào /users/me
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden());
    }
}
