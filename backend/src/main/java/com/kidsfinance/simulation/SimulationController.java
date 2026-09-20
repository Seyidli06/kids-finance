package com.kidsfinance.simulation;

import com.kidsfinance.simulation.dto.request.SimulationDecisionRequest;
import com.kidsfinance.simulation.dto.response.SimulationDecisionResponse;
import com.kidsfinance.simulation.dto.response.SimulationScenarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents/me/children")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @GetMapping("/{childId}/simulations")
    public List<SimulationScenarioResponse> getScenarios(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId = Long.parseLong(jwt.getSubject());

        return simulationService.getScenariosForParent(
                parentUserId,
                childId
        );
    }

    @PostMapping("/{childId}/simulations/{scenarioCode}/decisions")
    @ResponseStatus(HttpStatus.CREATED)
    public SimulationDecisionResponse makeDecision(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId,
            @PathVariable String scenarioCode,
            @Valid @RequestBody SimulationDecisionRequest request
    ) {

        Long parentUserId = Long.parseLong(jwt.getSubject());

        return simulationService.makeDecision(
                parentUserId,
                childId,
                scenarioCode,
                request
        );
    }

    @GetMapping("/{childId}/simulations/decisions")
    public List<SimulationDecisionResponse> getDecisions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId = Long.parseLong(jwt.getSubject());

        return simulationService.getDecisionsForParent(
                parentUserId,
                childId
        );
    }
}