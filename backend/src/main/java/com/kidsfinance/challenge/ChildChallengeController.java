package com.kidsfinance.challenge;

import com.kidsfinance.challenge.dto.request.ChallengeAnswerRequest;
import com.kidsfinance.challenge.dto.response.ChallengeAttemptResponse;
import com.kidsfinance.challenge.dto.response.ChallengeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children/me/challenges")
@RequiredArgsConstructor
public class ChildChallengeController {

    private final ChallengeService challengeService;

    @GetMapping
    public List<ChallengeResponse> getChallenges(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return challengeService
                .getChallengesForChild(
                        getUserId(jwt)
                );
    }

    @PostMapping("/{challengeCode}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeAttemptResponse submitAnswer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String challengeCode,
            @Valid @RequestBody
            ChallengeAnswerRequest request
    ) {

        return challengeService
                .submitAnswerForChild(
                        getUserId(jwt),
                        challengeCode,
                        request
                );
    }

    @GetMapping("/attempts")
    public List<ChallengeAttemptResponse> getAttempts(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return challengeService
                .getAttemptsForChild(
                        getUserId(jwt)
                );
    }

    private Long getUserId(Jwt jwt) {
        return Long.parseLong(
                jwt.getSubject()
        );
    }
}