package com.kidsfinance.simulation.main;

import com.kidsfinance.simulation.main.dto.request.MainSimulationRequest;
import com.kidsfinance.simulation.main.dto.response.MainSimulationRunResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/children/me/simulations/main/runs"
)
@RequiredArgsConstructor
public class ChildMainSimulationController {

    private final MainSimulationService mainSimulationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MainSimulationRunResponse run(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            MainSimulationRequest request
    ) {

        return mainSimulationService.runForChild(
                getUserId(jwt),
                request
        );
    }

    @GetMapping
    public List<MainSimulationRunResponse> getRuns(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return mainSimulationService
                .getRunsForChild(
                        getUserId(jwt)
                );
    }

    private Long getUserId(Jwt jwt) {
        return Long.parseLong(
                jwt.getSubject()
        );
    }
}