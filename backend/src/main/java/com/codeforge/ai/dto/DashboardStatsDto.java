package com.codeforge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private Integer solvedCount;
    private Integer totalSubmissions;
    private Integer streak;
    private Integer accuracy;
    private Integer contests;
    private List<DashboardActivityDto> recentActivity;
    private List<DashboardTrendDto> solvedTrend;
}
