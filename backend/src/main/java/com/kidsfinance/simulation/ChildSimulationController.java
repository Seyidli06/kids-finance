package com.kidsfinance.simulation;

import com.kidsfinance.simulation.dto.request.SimulationDecisionRequest;
import com.kidsfinance.simulation.dto.response.SimulationDecisionResponse;
import com.kidsfinance.simulation.dto.response.SimulationRunResponse;
import com.kidsfinance.simulation.dto.response.SimulationScenarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children/me/simulations")
@RequiredArgsConstructor
public class ChildSimulationController {

    private final SimulationService simulationService;

    @GetMapping
    public List<SimulationScenarioResponse> getScenarios(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return simulationService
                .getScenariosForChild(
                        getUserId(jwt)
                );
    }

    @PostMapping("/{scenarioCode}/decisions")
    @ResponseStatus(HttpStatus.CREATED)
    public SimulationDecisionResponse makeDecision(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String scenarioCode,
            @Valid @RequestBody SimulationDecisionRequest request
    ) {

        return simulationService
                .makeDecisionForChild(
                        getUserId(jwt),
                        scenarioCode,
                        request
                );
    }

    @GetMapping("/decisions")
    public List<SimulationDecisionResponse> getDecisions(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return simulationService
                .getDecisionsForChild(
                        getUserId(jwt)
                );
    }

    @GetMapping("/runs")
    public List<SimulationRunResponse> getRuns(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return simulationService
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