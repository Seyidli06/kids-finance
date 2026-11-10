package com.kidsfinance.gamification;

import com.kidsfinance.gamification.dto.response.AchievementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        "/api/children/me/achievements"
)
@RequiredArgsConstructor
public class ChildAchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public List<AchievementResponse> getAchievements(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long childUserId =
                Long.parseLong(
                        jwt.getSubject()
                );

        return achievementService
                .getAchievementsForChild(
                        childUserId
                );
    }
}