package com.kidsfinance.parent;

import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentChildResponse;
import com.kidsfinance.parent.dto.response.ParentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;
    private final ChildService childService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParentResponse createParent(
            @Valid @RequestBody CreateParentRequest request
    ) {
        return parentService.createParent(request);
    }

    @GetMapping("/me/children")
    public List<ParentChildResponse> getMyChildren(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Long userId = Long.parseLong(
                jwt.getSubject()
        );

        return parentService.getMyChildren(userId);
    }

    @PostMapping("/me/children")
    @ResponseStatus(HttpStatus.CREATED)
    public ChildResponse createMyChild(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateChildRequest request
    ) {

        Long userId = Long.parseLong(
                jwt.getSubject()
        );

        return childService.createChildForParent(
                userId,
                request
        );
    }
}