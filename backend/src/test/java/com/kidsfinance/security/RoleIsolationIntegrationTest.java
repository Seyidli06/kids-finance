package com.kidsfinance.security;

import com.kidsfinance.auth.AuthService;
import com.kidsfinance.auth.dto.request.LoginRequest;
import com.kidsfinance.auth.dto.response.LoginResponse;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.parent.ParentService;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class RoleIsolationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParentService parentService;

    @Autowired
    private ChildService childService;

    @Autowired
    private AuthService authService;

    @Test
    void parentTokenShouldAccessParentApiButNotChildApi()
            throws Exception {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "role_parent_one",
                                "Password123!",
                                "Role Parent One",
                                "role.parent.one@example.com"
                        )
                );

        childService.createChildForParent(
                parent.userId(),
                new CreateChildRequest(
                        "role_child_one",
                        "Password123!",
                        "Role Child One",
                        (short) 12
                )
        );

        LoginResponse login =
                authService.login(
                        new LoginRequest(
                                "role_parent_one",
                                "Password123!"
                        )
                );

        String authorization =
                "Bearer " + login.accessToken();

        mockMvc.perform(
                        get("/api/parents/me/children")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/children/me")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        get("/api/children/me/simulations")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void childTokenShouldAccessChildApiButNotParentApi()
            throws Exception {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "role_parent_two",
                                "Password123!",
                                "Role Parent Two",
                                "role.parent.two@example.com"
                        )
                );

        ChildResponse child =
                childService.createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                "role_child_two",
                                "Password123!",
                                "Role Child Two",
                                (short) 12
                        )
                );

        LoginResponse login =
                authService.login(
                        new LoginRequest(
                                child.username(),
                                "Password123!"
                        )
                );

        String authorization =
                "Bearer " + login.accessToken();

        mockMvc.perform(
                        get("/api/children/me")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/children/me/wallet")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/children/me/progress")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/children/me/simulations")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/parents/me/children")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void childTokenShouldBeAbleToSubmitSimulationDecision()
            throws Exception {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "role_parent_three",
                                "Password123!",
                                "Role Parent Three",
                                "role.parent.three@example.com"
                        )
                );

        ChildResponse child =
                childService.createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                "role_child_three",
                                "Password123!",
                                "Role Child Three",
                                (short) 12
                        )
                );

        LoginResponse login =
                authService.login(
                        new LoginRequest(
                                child.username(),
                                "Password123!"
                        )
                );

        String authorization =
                "Bearer " + login.accessToken();

        mockMvc.perform(
                        post(
                                "/api/children/me/simulations/"
                                        + "SAVE_OR_SPEND_01/decisions"
                        )
                                .header(
                                        "Authorization",
                                        authorization
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "optionCode": "KEEP_SAVINGS"
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        get("/api/children/me/simulations/decisions")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/children/me/simulations/runs")
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointsShouldRejectMissingToken()
            throws Exception {

        mockMvc.perform(
                        get("/api/children/me")
                )
                .andExpect(status().isUnauthorized());

        mockMvc.perform(
                        get("/api/parents/me/children")
                )
                .andExpect(status().isUnauthorized());
    }
}