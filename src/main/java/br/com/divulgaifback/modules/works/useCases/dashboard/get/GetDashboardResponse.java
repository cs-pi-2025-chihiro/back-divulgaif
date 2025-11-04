package br.com.divulgaifback.modules.works.useCases.dashboard.get;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Setter
@Getter
public class GetDashboardResponse {
    private List<GetDashboardWorksByStatus> totalWorksByStatus;
    private List<GetDashboardWorksByLabel> totalPublishedWorksByLabel;
    private List<GetDashboardWorksByAuthor> totalPublishedWorksByAuthor;

    @Getter
    @AllArgsConstructor
    public static class GetDashboardWorksByStatus {
        public Long total;
        public String status;
    }

    @Getter
    @AllArgsConstructor
    public static class GetDashboardWorksByLabel {
        public Long total;
        public String label;
    }

    @Getter
    @AllArgsConstructor
    public static class GetDashboardWorksByAuthor {
        public Long total;
        public String author;
    }
}
