package com.kidsfinance.simulation.main;

import com.kidsfinance.simulation.main.dto.response.MainSimulationRunResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/parents/me/children/{childId}/simulations/main/runs"
)
@RequiredArgsConstructor
public class ParentMainSimulationController {

    private final MainSimulationService mainSimulationService;

    @GetMapping
    public List<MainSimulationRunResponse> getRuns(
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