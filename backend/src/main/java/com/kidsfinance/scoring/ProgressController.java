package com.kidsfinance.scoring;

import com.kidsfinance.scoring.dto.response.ChildProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parents/me/children")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/{childId}/progress")
    public ChildProgressResponse getProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId = Long.parseLong(jwt.getSubject());

        return progressService.getProgressForParent(
                parentUserId,
                childId
        );
    }
}