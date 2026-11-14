package com.kidsfinance.reporting;

import com.kidsfinance.challenge.ChallengeAttempt;
import com.kidsfinance.challenge.ChallengeAttemptRepository;
import com.kidsfinance.dashboard.ParentDashboardService;
import com.kidsfinance.dashboard.dto.response.ParentDashboardResponse;
import com.kidsfinance.gamification.ChildAchievementRepository;
import com.kidsfinance.reporting.dto.response.DailyActivityResponse;
import com.kidsfinance.reporting.dto.response.ParentReportResponse;
import com.kidsfinance.simulation.SimulationRun;
import com.kidsfinance.simulation.SimulationRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentReportService {

    private static final ZoneId APP_ZONE =
            ZoneId.of("Asia/Baku");

    private final ParentDashboardService parentDashboardService;

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final SimulationRunRepository simulationRunRepository;
    private final ChildAchievementRepository childAchievementRepository;

    @Transactional(readOnly = true)
    public ParentReportResponse getReport(
            Long parentUserId,
            Long childProfileId,
            ReportPeriod period
    ) {

        /*
         * Dashboard service also validates that the child
         * belongs to the authenticated parent.
         */
        ParentDashboardResponse dashboard =
                parentDashboardService.getDashboard(
                        parentUserId,
                        childProfileId
                );

        LocalDate today =
                LocalDate.now(APP_ZONE);

        LocalDate fromDate =
                calculateFromDate(
                        today,
                        period
                );

        /*
         * toDate in the response is inclusive.
         * Database query uses tomorrow as exclusive upper bound.
         */
        LocalDate toDate =
                today;

        OffsetDateTime from =
                fromDate
                        .atStartOfDay(APP_ZONE)
                        .toOffsetDateTime();

        OffsetDateTime toExclusive =
                toDate
                        .plusDays(1)
                        .atStartOfDay(APP_ZONE)
                        .toOffsetDateTime();

        List<ChallengeAttempt> challengeAttempts =
                challengeAttemptRepository
                        .findAllByChild_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAscIdAsc(
                                childProfileId,
                                from,
                                toExclusive
                        );

        List<SimulationRun> simulationRuns =
                simulationRunRepository
                        .findAllByChild_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtAscIdAsc(
                                childProfileId,
                                from,
                                toExclusive
                        );

        long correctChallengeAnswers =
                challengeAttempts.stream()
                        .filter(
                                ChallengeAttempt::isCorrect
                        )
                        .count();

        long totalActivities =
                Math.addExact(
                        challengeAttempts.size(),
                        simulationRuns.size()
                );

        int challengeXp =
                challengeAttempts.stream()
                        .mapToInt(
                                ChallengeAttempt::getXpAwarded
                        )
                        .sum();

        int simulationXp =
                simulationRuns.stream()
                        .mapToInt(
                                SimulationRun::getXpEarned
                        )
                        .sum();

        int xpEarned =
                Math.addExact(
                        challengeXp,
                        simulationXp
                );

        long newAchievements =
                childAchievementRepository
                        .countByChild_IdAndEarnedAtGreaterThanEqualAndEarnedAtLessThan(
                                childProfileId,
                                from,
                                toExclusive
                        );

        List<DailyActivityResponse> daily =
                buildDailyActivity(
                        fromDate,
                        toDate,
                        challengeAttempts,
                        simulationRuns
                );

        int activeDays =
                (int) daily.stream()
                        .filter(day ->
                                day.totalActivities() > 0
                        )
                        .count();

        return new ParentReportResponse(
                dashboard.childId(),
                dashboard.displayName(),

                period,

                fromDate,
                toDate,

                (long) challengeAttempts.size(),
                correctChallengeAnswers,

                (long) simulationRuns.size(),
                totalActivities,

                xpEarned,

                activeDays,

                newAchievements,

                dashboard.overallScore(),

                dashboard.areasForImprovement(),

                daily
        );
    }

    private LocalDate calculateFromDate(
            LocalDate today,
            ReportPeriod period
    ) {

        return switch (period) {

            case WEEKLY ->
                    today.with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.MONDAY
                            )
                    );

            case MONTHLY ->
                    today.withDayOfMonth(1);
        };
    }

    private List<DailyActivityResponse> buildDailyActivity(
            LocalDate fromDate,
            LocalDate toDate,
            List<ChallengeAttempt> challengeAttempts,
            List<SimulationRun> simulationRuns
    ) {

        List<DailyActivityResponse> result =
                new ArrayList<>();

        LocalDate date =
                fromDate;

        while (!date.isAfter(toDate)) {

            LocalDate currentDate =
                    date;

            List<ChallengeAttempt> dayChallenges =
                    challengeAttempts.stream()
                            .filter(attempt ->
                                    toLocalDate(
                                            attempt.getCreatedAt()
                                    ).equals(
                                            currentDate
                                    )
                            )
                            .toList();

            List<SimulationRun> daySimulations =
                    simulationRuns.stream()
                            .filter(run ->
                                    toLocalDate(
                                            run.getCreatedAt()
                                    ).equals(
                                            currentDate
                                    )
                            )
                            .toList();

            long correctAnswers =
                    dayChallenges.stream()
                            .filter(
                                    ChallengeAttempt::isCorrect
                            )
                            .count();

            long total =
                    Math.addExact(
                            dayChallenges.size(),
                            daySimulations.size()
                    );

            int challengeXp =
                    dayChallenges.stream()
                            .mapToInt(
                                    ChallengeAttempt::getXpAwarded
                            )
                            .sum();

            int simulationXp =
                    daySimulations.stream()
                            .mapToInt(
                                    SimulationRun::getXpEarned
                            )
                            .sum();

            result.add(
                    new DailyActivityResponse(
                            currentDate,

                            (long) dayChallenges.size(),
                            correctAnswers,

                            (long) daySimulations.size(),
                            total,

                            Math.addExact(
                                    challengeXp,
                                    simulationXp
                            )
                    )
            );

            date =
                    date.plusDays(1);
        }

        return result;
    }

    private LocalDate toLocalDate(
            OffsetDateTime dateTime
    ) {

        return dateTime
                .atZoneSameInstant(APP_ZONE)
                .toLocalDate();
    }
}