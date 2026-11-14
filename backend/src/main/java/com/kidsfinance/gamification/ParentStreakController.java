package com.kidsfinance.gamification;

import com.kidsfinance.gamification.dto.response.StreakResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/parents/me/children/{childId}/streak"
)
@RequiredArgsConstructor
public class ParentStreakController {

    private final StreakService streakService;

    @GetMapping
    public StreakResponse getStreak(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        return streakService.getForParent(
                Long.parseLong(jwt.getSubject()),
                childId
        );
    }
}