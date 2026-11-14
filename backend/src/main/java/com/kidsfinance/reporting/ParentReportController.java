package com.kidsfinance.reporting;

import com.kidsfinance.reporting.dto.response.ParentReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/parents/me/children/{childId}/reports"
)
@RequiredArgsConstructor
public class ParentReportController {

    private final ParentReportService parentReportService;

    @GetMapping
    public ParentReportResponse getReport(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId,
            @RequestParam ReportPeriod period
    ) {

        Long parentUserId =
                Long.parseLong(
                        jwt.getSubject()
                );

        return parentReportService.getReport(
                parentUserId,
                childId,
                period
        );
    }
}