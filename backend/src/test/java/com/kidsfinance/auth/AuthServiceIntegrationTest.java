package com.kidsfinance.auth;

import com.kidsfinance.auth.dto.request.LoginRequest;
import com.kidsfinance.auth.dto.response.LoginResponse;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.parent.ParentService;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import com.kidsfinance.user.User;
import com.kidsfinance.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AuthServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AuthService authService;

    @Autowired
    private ParentService parentService;

    @Autowired
    private ChildService childService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldIssueCorrectRoleClaimsForParentAndChild() {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "auth_parent",
                                "Password123!",
                                "Auth Parent",
                                "auth.parent@example.com"
                        )
                );

        ChildResponse child =
                childService.createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                "auth_child",
                                "Password123!",
                                "Auth Child",
                                (short) 12
                        )
                );

        LoginResponse parentLogin =
                authService.login(
                        new LoginRequest(
                                "auth_parent",
                                "Password123!"
                        )
                );

        LoginResponse childLogin =
                authService.login(
                        new LoginRequest(
                                "auth_child",
                                "Password123!"
                        )
                );

        assertThat(parentLogin.tokenType())
                .isEqualTo("Bearer");

        assertThat(childLogin.tokenType())
                .isEqualTo("Bearer");

        assertThat(parentLogin.expiresIn())
                .isEqualTo(900);

        assertThat(childLogin.expiresIn())
                .isEqualTo(900);

        Jwt parentJwt =
                jwtDecoder.decode(
                        parentLogin.accessToken()
                );

        Jwt childJwt =
                jwtDecoder.decode(
                        childLogin.accessToken()
                );

        assertThat(parentJwt.getSubject())
                .isEqualTo(
                        parent.userId().toString()
                );

        assertThat(
                parentJwt.getClaimAsString("role")
        ).isEqualTo("PARENT");

        assertThat(childJwt.getSubject())
                .isEqualTo(
                        child.userId().toString()
                );

        assertThat(
                childJwt.getClaimAsString("role")
        ).isEqualTo("CHILD");

        assertThat(
                parentJwt.getClaimAsString("iss")
        ).isEqualTo(
                "kids-finance-api"
        );

        assertThat(
                childJwt.getClaimAsString("iss")
        ).isEqualTo(
                "kids-finance-api"
        );
    }

    @Test
    void shouldRejectUnknownUser() {

        assertUnauthorized(() ->
                authService.login(
                        new LoginRequest(
                                "unknown_user",
                                "Password123!"
                        )
                )
        );
    }

    @Test
    void shouldRejectWrongPassword() {

        parentService.createParent(
                new CreateParentRequest(
                        "wrong_password_parent",
                        "Password123!",
                        "Wrong Password Parent",
                        "wrong.password@example.com"
                )
        );

        assertUnauthorized(() ->
                authService.login(
                        new LoginRequest(
                                "wrong_password_parent",
                                "DefinitelyWrongPassword"
                        )
                )
        );
    }

    @Test
    void shouldRejectInactiveUser() {

        parentService.createParent(
                new CreateParentRequest(
                        "inactive_parent",
                        "Password123!",
                        "Inactive Parent",
                        "inactive.parent@example.com"
                )
        );

        User user =
                userRepository
                        .findByUsername(
                                "inactive_parent"
                        )
                        .orElseThrow();

        user.setActive(false);

        userRepository.saveAndFlush(user);

        assertUnauthorized(() ->
                authService.login(
                        new LoginRequest(
                                "inactive_parent",
                                "Password123!"
                        )
                )
        );
    }

    private void assertUnauthorized(
            Runnable loginAttempt
    ) {

        assertThatThrownBy(loginAttempt::run)
                .isInstanceOf(
                        ResponseStatusException.class
                )
                .satisfies(exception -> {

                    ResponseStatusException responseStatusException =
                            (ResponseStatusException) exception;

                    assertThat(
                            responseStatusException.getStatusCode()
                    ).isEqualTo(
                            HttpStatus.UNAUTHORIZED
                    );

                    assertThat(
                            responseStatusException.getReason()
                    ).isEqualTo(
                            "Invalid username or password"
                    );
                });
    }
}