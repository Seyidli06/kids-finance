package com.kidsfinance.gamification;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.gamification.dto.response.StreakResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class StreakService {

    private static final ZoneId APP_ZONE =
            ZoneId.of("Asia/Baku");

    private final ChildStreakRepository childStreakRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void initializeForChild(
            ChildProfile child
    ) {

        if (childStreakRepository.existsByChild_Id(
                child.getId()
        )) {
            return;
        }

        childStreakRepository.save(
                ChildStreak.builder()
                        .child(child)
                        .currentStreak(0)
                        .longestStreak(0)
                        .build()
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordActivity(
            ChildProfile child
    ) {

        recordActivity(
                child,
                LocalDate.now(APP_ZONE)
        );
    }

    void recordActivity(
            ChildProfile child,
            LocalDate activityDate
    ) {

        ChildStreak streak =
                childStreakRepository
                        .findByChildIdForUpdate(
                                child.getId()
                        )
                        .orElseGet(() ->
                                childStreakRepository.save(
                                        ChildStreak.builder()
                                                .child(child)
                                                .currentStreak(0)
                                                .longestStreak(0)
                                                .build()
                                )
                        );

        LocalDate lastActivity =
                streak.getLastActivityDate();

        if (lastActivity == null) {
            streak.setCurrentStreak(1);
            streak.setLongestStreak(1);
            streak.setLastActivityDate(activityDate);
            return;
        }

        // Same day does not increase streak.
        if (activityDate.equals(lastActivity)) {
            return;
        }

        // Ignore old/out-of-order activity.
        if (activityDate.isBefore(lastActivity)) {
            return;
        }

        // Consecutive day.
        if (activityDate.equals(
                lastActivity.plusDays(1)
        )) {

            int newCurrent =
                    streak.getCurrentStreak() + 1;

            streak.setCurrentStreak(newCurrent);

            streak.setLongestStreak(
                    Math.max(
                            streak.getLongestStreak(),
                            newCurrent
                    )
            );

            streak.setLastActivityDate(activityDate);
            return;
        }

        // At least one day was missed.
        streak.setCurrentStreak(1);

        streak.setLongestStreak(
                Math.max(
                        streak.getLongestStreak(),
                        1
                )
        );

        streak.setLastActivityDate(activityDate);
    }

    @Transactional(readOnly = true)
    public StreakResponse getForChild(
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

        return getResponse(
                child.getId()
        );
    }

    @Transactional(readOnly = true)
    public StreakResponse getForParent(
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

        return getResponse(
                childProfileId
        );
    }

    private StreakResponse getResponse(
            Long childProfileId
    ) {

        ChildStreak streak =
                childStreakRepository
                        .findByChild_Id(childProfileId)
                        .orElse(null);

        if (streak == null) {
            return new StreakResponse(
                    0,
                    0,
                    null,
                    false
            );
        }

        LocalDate today =
                LocalDate.now(APP_ZONE);

        LocalDate lastActivity =
                streak.getLastActivityDate();

        boolean activeToday =
                today.equals(lastActivity);

        int current =
                calculateEffectiveCurrentStreak(
                        streak,
                        today
                );

        return new StreakResponse(
                current,
                streak.getLongestStreak(),
                lastActivity,
                activeToday
        );
    }

    private int calculateEffectiveCurrentStreak(
            ChildStreak streak,
            LocalDate today
    ) {

        LocalDate lastActivity =
                streak.getLastActivityDate();

        if (lastActivity == null) {
            return 0;
        }

        if (lastActivity.equals(today)
                || lastActivity.equals(
                today.minusDays(1)
        )) {

            return streak.getCurrentStreak();
        }

        return 0;
    }
}