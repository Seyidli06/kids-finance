package com.kidsfinance.scoring;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.scoring.dto.response.ChildProgressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ParentChildLinkRepository parentChildLinkRepository;
    private final ChildProfileRepository childProfileRepository;
    private final SkillScoringService skillScoringService;

    @Transactional(readOnly = true)
    public ChildProgressResponse getProgressForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        boolean belongsToParent =
                parentChildLinkRepository
                        .existsByParent_User_IdAndChild_Id(
                                parentUserId,
                                childProfileId
                        );

        if (!belongsToParent) {
            throw new ResourceNotFoundException(
                    "Child not found for authenticated parent"
            );
        }

        ChildProfile child =
                childProfileRepository
                        .findById(childProfileId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Child not found for authenticated parent"
                                )
                        );

        return toProgressResponse(child);
    }

    @Transactional(readOnly = true)
    public ChildProgressResponse getProgressForChild(
            Long childUserId
    ) {

        ChildProfile child =
                childProfileRepository
                        .findByUser_Id(childUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Child profile not found for authenticated user"
                                )
                        );

        return toProgressResponse(child);
    }

    private ChildProgressResponse toProgressResponse(
            ChildProfile child
    ) {

        int xpPerLevel =
                ScoringService.XP_PER_LEVEL;

        int xpInCurrentLevel =
                child.getXp() % xpPerLevel;

        int xpToNextLevel =
                xpPerLevel - xpInCurrentLevel;

        var skillScores =
                skillScoringService
                        .getScoresForChild(
                                child.getId()
                        );

        int overallScore =
                (int) Math.round(
                        skillScores.stream()
                                .mapToInt(
                                        score ->
                                                score.score()
                                )
                                .average()
                                .orElse(0)
                );

        ChildProgressResponse childProgressResponse = new ChildProgressResponse(
                child.getId(),
                child.getDisplayName(),
                child.getXp(),
                child.getLevel(),
                xpInCurrentLevel,
                xpPerLevel,
                xpToNextLevel,
                overallScore,
                skillScores
        );
        return childProgressResponse;

    }
}
