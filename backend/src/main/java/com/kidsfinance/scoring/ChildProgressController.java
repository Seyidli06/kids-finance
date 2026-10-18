package com.kidsfinance.scoring;

import com.kidsfinance.scoring.dto.response.ChildProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/children/me")
@RequiredArgsConstructor
public class ChildProgressController {

    private final ProgressService progressService;

    @GetMapping("/progress")
    public ChildProgressResponse getProgress(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long childUserId =
                Long.parseLong(jwt.getSubject());

        return progressService.getProgressForChild(
                childUserId
        );
    }
}