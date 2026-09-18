package com.kidsfinance.parent;

import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParentResponse createParent(
            @Valid @RequestBody CreateParentRequest request
    ) {
        return parentService.createParent(request);
    }
}