package com.kidsfinance.child;

import com.kidsfinance.child.dto.response.ChildResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @GetMapping("/me")
    public ChildResponse getMe(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long childUserId =
                Long.parseLong(jwt.getSubject());

        return childService.getCurrentChild(
                childUserId
        );
    }
}