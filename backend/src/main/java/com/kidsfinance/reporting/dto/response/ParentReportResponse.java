package com.kidsfinance.reporting.dto.response;

import com.kidsfinance.reporting.ReportPeriod;
import com.kidsfinance.scoring.SkillType;

import java.time.LocalDate;
import java.util.List;

public record ParentReportResponse(

        Long childId,
        String displayName,

        ReportPeriod period,

        LocalDate fromDate,
        LocalDate toDate,

        Long challengeAttempts,
        Long correctChallengeAnswers,

        Long simulationRuns,
        Long totalActivities,

        Integer xpEarned,

        Integer activeDays,

        Long newAchievementsEarned,

        Integer currentOverallScore,

        List<SkillType> areasForImprovement,

        List<DailyActivityResponse> dailyActivity

) {
}