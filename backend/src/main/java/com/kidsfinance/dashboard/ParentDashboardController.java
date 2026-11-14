package com.kidsfinance.dashboard;

import com.kidsfinance.dashboard.dto.response.ParentDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/parents/me/children/{childId}/dashboard"
)
@RequiredArgsConstructor
public class ParentDashboardController {

    private final ParentDashboardService parentDashboardService;

    @GetMapping
    public ParentDashboardResponse getDashboard(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId =
                Long.parseLong(
                        jwt.getSubject()
                );

        return parentDashboardService
                .getDashboard(
                        parentUserId,
                        childId
                );
    }
}