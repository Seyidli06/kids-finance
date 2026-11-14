package com.kidsfinance.gamification;

import com.kidsfinance.gamification.dto.response.StreakResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/children/me/streak")
@RequiredArgsConstructor
public class ChildStreakController {

    private final StreakService streakService;

    @GetMapping
    public StreakResponse getStreak(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return streakService.getForChild(
                Long.parseLong(jwt.getSubject())
        );
    }
}