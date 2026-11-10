package com.kidsfinance.gamification;

import com.kidsfinance.gamification.dto.response.AchievementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/parents/me/children/{childId}/achievements"
)
@RequiredArgsConstructor
public class ParentAchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public List<AchievementResponse> getAchievements(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId =
                Long.parseLong(
                        jwt.getSubject()
                );

        return achievementService
                .getAchievementsForParent(
                        parentUserId,
                        childId
                );
    }
}