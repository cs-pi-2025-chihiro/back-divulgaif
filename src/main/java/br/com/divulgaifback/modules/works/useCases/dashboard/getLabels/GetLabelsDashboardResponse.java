package br.com.divulgaifback.modules.works.useCases.dashboard.getLabels;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class GetLabelsDashboardResponse {
    Long quantityOfLabels;
    String mostUsedLabel;
    String leastUsedLabel;
}
