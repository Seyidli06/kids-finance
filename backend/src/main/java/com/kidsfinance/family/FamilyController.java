package com.kidsfinance.family;

import com.kidsfinance.family.dto.request.LinkParentChildRequest;
import com.kidsfinance.family.dto.response.ParentChildLinkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping("/links")
    @ResponseStatus(HttpStatus.CREATED)
    public ParentChildLinkResponse linkParentToChild(
            @Valid @RequestBody LinkParentChildRequest request
    ) {
        return familyService.linkParentToChild(request);
    }
}