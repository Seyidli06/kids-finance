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
            new PostgreSQLContainer(
                    "postgres:17-alpine"
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParentService parentService;

    @Autowired
    private ChildService childService;

    @Autowired
    private AuthService authService;

    @Test
    void parentTokenShouldAccessParentApisButNotChildApis()
            throws Exception {

        ParentResponse parent =
                createParent(
                        "security_parent_one",
                        "security.parent.one@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "security_child_one"
                );

        String authorization =
                authorization(
                        parent.username(),
                        "Password123!"
                );

        // Parent APIs

        expectOk(
                "/api/parents/me/children",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/wallet",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/wallet/transactions",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/progress",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/simulations/runs",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/achievements",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/streak",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/dashboard",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/reports?period=WEEKLY",
                authorization
        );

        expectOk(
                "/api/parents/me/children/"
                        + child.id()
                        + "/reports?period=MONTHLY",
                authorization
        );

        // Parent must not access child self-service APIs.

        expectForbidden(
                "/api/children/me",
                authorization
        );

        expectForbidden(
                "/api/children/me/wallet",
                authorization
        );

        expectForbidden(
                "/api/children/me/progress",
                authorization
        );

        expectForbidden(
                "/api/children/me/challenges",
                authorization
        );

        expectForbidden(
                "/api/children/me/simulations/runs",
                authorization
        );

        expectForbidden(
                "/api/children/me/achievements",
                authorization
        );

        expectForbidden(
                "/api/children/me/streak",
                authorization
        );
    }

    @Test
    void childTokenShouldAccessChildApisButNotParentApis()
            throws Exception {

        ParentResponse parent =
                createParent(
                        "security_parent_two",
                        "security.parent.two@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "security_child_two"
                );

        String authorization =
                authorization(
                        child.username(),
                        "Password123!"
                );

        // Child APIs.

        expectOk(
                "/api/children/me",
                authorization
        );

        expectOk(
                "/api/children/me/wallet",
                authorization
        );

        expectOk(
                "/api/children/me/wallet/transactions",
                authorization
        );

        expectOk(
                "/api/children/me/progress",
                authorization
        );

        expectOk(
                "/api/children/me/challenges",
                authorization
        );

        expectOk(
                "/api/children/me/challenges/attempts",
                authorization
        );

        expectOk(
                "/api/children/me/simulations/runs",
                authorization
        );

        expectOk(
                "/api/children/me/achievements",
                authorization
        );

        expectOk(
                "/api/children/me/streak",
                authorization
        );

        // Child must not access parent APIs.

        expectForbidden(
                "/api/parents/me/children",
                authorization
        );

        expectForbidden(
                "/api/parents/me/children/"
                        + child.id()
                        + "/dashboard",
                authorization
        );

        expectForbidden(
                "/api/parents/me/children/"
                        + child.id()
                        + "/reports?period=WEEKLY",
                authorization
        );

        expectForbidden(
                "/api/parents/me/children/"
                        + child.id()
                        + "/progress",
                authorization
        );

        expectForbidden(
                "/api/parents/me/children/"
                        + child.id()
                        + "/achievements",
                authorization
        );
    }

    @Test
    void childTokenShouldBeAbleToRunSimulation()
            throws Exception {

        ParentResponse parent =
                createParent(
                        "security_parent_three",
                        "security.parent.three@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "security_child_three"
                );

        String authorization =
                authorization(
                        child.username(),
                        "Password123!"
                );

        mockMvc.perform(
                        post(
                                "/api/children/me/simulations/runs"
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
                                          "cash": 100,
                                          "saving": 200,
                                          "deposit": 200,
                                          "bond": 100,
                                          "investment": 150,
                                          "spending": 100,
                                          "business": 150
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                );

        expectOk(
                "/api/children/me/simulations/runs",
                authorization
        );

        expectOk(
                "/api/children/me/progress",
                authorization
        );

        expectOk(
                "/api/children/me/streak",
                authorization
        );

        expectOk(
                "/api/children/me/achievements",
                authorization
        );
    }

    @Test
    void childTokenShouldBeAbleToSubmitChallengeAnswer()
            throws Exception {

        ParentResponse parent =
                createParent(
                        "security_parent_four",
                        "security.parent.four@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "security_child_four"
                );

        String authorization =
                authorization(
                        child.username(),
                        "Password123!"
                );

        mockMvc.perform(
                        post(
                                "/api/children/me/challenges/"
                                        + "SAVING_01/answers"
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
                                          "optionCode": "SAVE_FIRST"
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isCreated()
                );

        expectOk(
                "/api/children/me/challenges/attempts",
                authorization
        );

        expectOk(
                "/api/children/me/progress",
                authorization
        );

        expectOk(
                "/api/children/me/streak",
                authorization
        );

        expectOk(
                "/api/children/me/achievements",
                authorization
        );
    }

    @Test
    void protectedEndpointsShouldRejectMissingToken()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/children/me"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        mockMvc.perform(
                        get(
                                "/api/children/me/simulations/runs"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        mockMvc.perform(
                        get(
                                "/api/children/me/achievements"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );

        mockMvc.perform(
                        get(
                                "/api/parents/me/children"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    private ParentResponse createParent(
            String username,
            String email
    ) {

        return parentService.createParent(
                new CreateParentRequest(
                        username,
                        "Password123!",
                        "Security Parent",
                        email
                )
        );
    }

    private ChildResponse createChild(
            ParentResponse parent,
            String username
    ) {

        return childService.createChildForParent(
                parent.userId(),
                new CreateChildRequest(
                        username,
                        "Password123!",
                        "Security Child",
                        (short) 12
                )
        );
    }

    private String authorization(
            String username,
            String password
    ) {

        LoginResponse login =
                authService.login(
                        new LoginRequest(
                                username,
                                password
                        )
                );

        return "Bearer "
                + login.accessToken();
    }

    private void expectOk(
            String uri,
            String authorization
    ) throws Exception {

        mockMvc.perform(
                        get(uri)
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    private void expectForbidden(
            String uri,
            String authorization
    ) throws Exception {

        mockMvc.perform(
                        get(uri)
                                .header(
                                        "Authorization",
                                        authorization
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }
}