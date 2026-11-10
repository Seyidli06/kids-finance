package com.kidsfinance.simulation;

import com.kidsfinance.simulation.dto.request.SimulationRequest;
import com.kidsfinance.simulation.dto.response.SimulationRunResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/children/me/simulations/runs"
)
@RequiredArgsConstructor
public class ChildSimulationController {

    private final SimulationService mainSimulationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SimulationRunResponse run(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody
            SimulationRequest request
    ) {

        return mainSimulationService.runForChild(
                getUserId(jwt),
                request
        );
    }

    @GetMapping
    public List<SimulationRunResponse> getRuns(
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