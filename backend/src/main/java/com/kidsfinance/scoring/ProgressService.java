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

    @Transactional(readOnly = true)
    public ChildProgressResponse getProgressForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        boolean belongsToParent = parentChildLinkRepository
                .existsByParent_User_IdAndChild_Id(
                        parentUserId,
                        childProfileId
                );

        if (!belongsToParent) {
            throw new ResourceNotFoundException(
                    "Child not found for authenticated parent"
            );
        }

        ChildProfile child = childProfileRepository
                .findById(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Child not found for authenticated parent"
                        )
                );

        int xpPerLevel = ScoringService.XP_PER_LEVEL;

        int xpInCurrentLevel = child.getXp() % xpPerLevel;

        int xpToNextLevel = xpPerLevel - xpInCurrentLevel;

        return new ChildProgressResponse(
                child.getId(),
                child.getDisplayName(),
                child.getXp(),
                child.getLevel(),
                xpInCurrentLevel,
                xpPerLevel,
                xpToNextLevel
        );
    }
}