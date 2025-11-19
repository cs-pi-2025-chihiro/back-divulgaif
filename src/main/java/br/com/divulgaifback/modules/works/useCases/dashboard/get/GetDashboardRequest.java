package br.com.divulgaifback.modules.works.useCases.dashboard.get;

import java.time.LocalDateTime;

public record GetDashboardRequest(
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer statusId,
    Integer labelId,
    Integer authorId
) {
}
