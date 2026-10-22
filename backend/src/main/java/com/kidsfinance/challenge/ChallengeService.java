package com.kidsfinance.challenge;

import com.kidsfinance.challenge.dto.request.ChallengeAnswerRequest;
import com.kidsfinance.challenge.dto.response.ChallengeAttemptResponse;
import com.kidsfinance.challenge.dto.response.ChallengeOptionResponse;
import com.kidsfinance.challenge.dto.response.ChallengeResponse;
import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.scoring.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeOptionRepository challengeOptionRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ScoringService scoringService;

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getChallengesForChild(
            Long childUserId
    ) {

        ChildProfile child =
                getChildByUserId(childUserId);

        return challengeRepository
                .findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(challenge ->
                        toChallengeResponse(
                                child.getId(),
                                challenge
                        )
                )
                .toList();
    }

    @Transactional
    public ChallengeAttemptResponse submitAnswerForChild(
            Long childUserId,
            String challengeCode,
            ChallengeAnswerRequest request
    ) {

        ChildProfile resolvedChild =
                getChildByUserId(childUserId);

        ChildProfile child =
                childProfileRepository
                        .findByIdForUpdate(
                                resolvedChild.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Child profile not found"
                                )
                        );

        Challenge challenge =
                challengeRepository
                        .findByCodeAndActiveTrue(
                                challengeCode
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Challenge not found"
                                )
                        );

        ChallengeOption selectedOption =
                challengeOptionRepository
                        .findByChallenge_IdAndCodeAndActiveTrue(
                                challenge.getId(),
                                request.optionCode()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Challenge option not found"
                                )
                        );

        boolean correct =
                selectedOption.isCorrect();

        int xpAwarded = 0;

        if (correct) {

            boolean alreadyCompleted =
                    challengeAttemptRepository
                            .existsByChild_IdAndChallenge_IdAndCorrectTrue(
                                    child.getId(),
                                    challenge.getId()
                            );

            if (!alreadyCompleted) {
                xpAwarded =
                        scoringService
                                .awardChallengeCompletionXp(
                                        child,
                                        challenge.getXpReward()
                                );
            }
        }

        ChallengeAttempt attempt =
                ChallengeAttempt.builder()
                        .child(child)
                        .challenge(challenge)
                        .selectedOption(selectedOption)
                        .correct(correct)
                        .xpAwarded(xpAwarded)
                        .build();

        ChallengeAttempt saved =
                challengeAttemptRepository.save(
                        attempt
                );

        return toAttemptResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ChallengeAttemptResponse>
    getAttemptsForChild(
            Long childUserId
    ) {

        ChildProfile child =
                getChildByUserId(childUserId);

        return challengeAttemptRepository
                .findAllByChild_IdOrderByCreatedAtDescIdDesc(
                        child.getId()
                )
                .stream()
                .map(this::toAttemptResponse)
                .toList();
    }

    private ChildProfile getChildByUserId(
            Long childUserId
    ) {

        return childProfileRepository
                .findByUser_Id(childUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Child profile not found for authenticated user"
                        )
                );
    }

    private ChallengeResponse toChallengeResponse(
            Long childProfileId,
            Challenge challenge
    ) {

        boolean completed =
                challengeAttemptRepository
                        .existsByChild_IdAndChallenge_IdAndCorrectTrue(
                                childProfileId,
                                challenge.getId()
                        );

        List<ChallengeOptionResponse> options =
                challengeOptionRepository
                        .findAllByChallenge_IdAndActiveTrueOrderByDisplayOrderAsc(
                                challenge.getId()
                        )
                        .stream()
                        .map(option ->
                                new ChallengeOptionResponse(
                                        option.getCode(),
                                        option.getText()
                                )
                        )
                        .toList();

        return new ChallengeResponse(
                challenge.getCode(),
                challenge.getLearningTopic().getCode(),
                challenge.getTitle(),
                challenge.getPrompt(),
                challenge.getXpReward(),
                completed,
                options
        );
    }

    private ChallengeAttemptResponse toAttemptResponse(
            ChallengeAttempt attempt
    ) {

        return new ChallengeAttemptResponse(
                attempt.getId(),
                attempt.getChallenge().getCode(),
                attempt.getSelectedOption().getCode(),
                attempt.isCorrect(),
                attempt.getChallenge().getExplanation(),
                attempt.getXpAwarded(),
                attempt.getCreatedAt()
        );
    }
}