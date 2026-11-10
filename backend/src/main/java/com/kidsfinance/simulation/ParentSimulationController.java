package com.kidsfinance.simulation;

import com.kidsfinance.simulation.dto.response.SimulationRunResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/parents/me/children/{childId}/simulations/runs"
)
@RequiredArgsConstructor
public class ParentSimulationController {

    private final SimulationService mainSimulationService;

    @GetMapping
    public List<SimulationRunResponse> getRuns(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId =
                Long.parseLong(
                        jwt.getSubject()
                );

        return mainSimulationService
                .getRunsForParent(
                        parentUserId,
                        childId
                );
    }
}