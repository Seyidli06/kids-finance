package com.kidsfinance.scoring;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.scoring.dto.response.SkillScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillScoringService {

    private final ChildSkillScoreRepository childSkillScoreRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void initializeScoresForChild(
            ChildProfile child
    ) {

        for (SkillType skillType : SkillType.values()) {

            ChildSkillScore skillScore =
                    ChildSkillScore.builder()
                            .child(child)
                            .skillType(skillType)
                            .score(0)
                            .activityCount(0)
                            .build();

            childSkillScoreRepository.save(
                    skillScore
            );
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public int recordActivity(
            ChildProfile child,
            SkillType skillType,
            int resultScore
    ) {

        if (resultScore < 0 || resultScore > 100) {
            throw new IllegalArgumentException(
                    "Activity score must be between 0 and 100"
            );
        }

        ChildSkillScore skillScore =
                childSkillScoreRepository
                        .findForUpdate(
                                child.getId(),
                                skillType
                        )
                        .orElseGet(() ->
                                childSkillScoreRepository.save(
                                        ChildSkillScore.builder()
                                                .child(child)
                                                .skillType(skillType)
                                                .score(0)
                                                .activityCount(0)
                                                .build()
                                )
                        );

        int oldCount =
                skillScore.getActivityCount();

        int newCount =
                Math.addExact(
                        oldCount,
                        1
                );

        long weightedTotal =
                (long) skillScore.getScore()
                        * oldCount
                        + resultScore;

        int newScore =
                (int) Math.round(
                        (double) weightedTotal
                                / newCount
                );

        skillScore.setScore(newScore);
        skillScore.setActivityCount(newCount);

        return newScore;
    }

    @Transactional(readOnly = true)
    public List<SkillScoreResponse> getScoresForChild(
            Long childProfileId
    ) {

        Map<SkillType, ChildSkillScore> existing =
                childSkillScoreRepository
                        .findAllByChild_Id(
                                childProfileId
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        ChildSkillScore::getSkillType,
                                        Function.identity()
                                )
                        );

        return Arrays.stream(
                        SkillType.values()
                )
                .map(skillType -> {

                    ChildSkillScore score =
                            existing.get(
                                    skillType
                            );

                    if (score == null) {
                        return new SkillScoreResponse(
                                skillType,
                                0,
                                0
                        );
                    }

                    return new SkillScoreResponse(
                            score.getSkillType(),
                            score.getScore(),
                            score.getActivityCount()
                    );
                })
                .toList();
    }
}