package br.com.divulgaifback.modules.works.useCases.dashboard.get;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.repositories.LabelRepository;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse.GetDashboardWorksByStatus;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse.GetDashboardWorksByLabel;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse.GetDashboardWorksByAuthor;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkRepository.WorksByStatusProjection;
import br.com.divulgaifback.modules.works.repositories.WorkRepository.WorksByLabelProjection;
import br.com.divulgaifback.modules.works.repositories.WorkRepository.WorksByAuthorProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetDashboardUseCase {
    private final WorkRepository workRepository;
    private final LabelRepository labelRepository;
    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public GetDashboardResponse execute(GetDashboardRequest request) {
        GetDashboardResponse response = new GetDashboardResponse();

        Integer statusId = Objects.nonNull(request) ? request.statusId() : null;
        Integer labelId = Objects.nonNull(request) ? request.labelId() : null;
        Integer authorId = Objects.nonNull(request) ? request.authorId() : null;
        LocalDateTime startDate = Objects.nonNull(request) ? request.startDate() : null;
        LocalDateTime endDate = Objects.nonNull(request) ? request.endDate() : null;

        final String filterStatusName;
        if (Objects.nonNull(statusId)) filterStatusName = WorkStatusEnum.fromId(statusId).name();
        else filterStatusName = WorkStatusEnum.PUBLISHED.name();

        setTotalWorksByStatus(response, statusId, startDate, endDate);
        setTotalWorksByLabel(response, labelId, filterStatusName, startDate, endDate);
        setTotalWorksByAuthor(response, authorId, filterStatusName, startDate, endDate);
        return response;
    }

    private void setTotalWorksByStatus(GetDashboardResponse response, Integer statusId, LocalDateTime startDate, LocalDateTime endDate) {
        if (Objects.nonNull(statusId)) {
            Long totalByStatus = workRepository.countByWorkStatusIdFiltered(statusId, startDate, endDate);
            WorkStatusEnum statusEnum = WorkStatusEnum.fromId(statusId);
            response.setTotalWorksByStatus(List.of(new GetDashboardWorksByStatus(totalByStatus, statusEnum.name())));
            return;
        }

        Map<String, Long> statusCountMap = workRepository.getCountsByStatusGrouped(startDate, endDate).stream()
                .collect(Collectors.toMap(
                        WorksByStatusProjection::getName,
                        WorksByStatusProjection::getTotal,
                        Long::sum
                ));

        List<GetDashboardWorksByStatus> worksByStatusList = new ArrayList<>();
        List<WorkStatusEnum> allStatus = List.of(WorkStatusEnum.values());

        for (WorkStatusEnum status : allStatus) {
            Long totalByStatus = statusCountMap.getOrDefault(status.name(), 0L);
            worksByStatusList.add(new GetDashboardWorksByStatus(totalByStatus, status.name()));
        }
        response.setTotalWorksByStatus(worksByStatusList);
    }

    private void setTotalWorksByLabel(GetDashboardResponse response, Integer labelId, String filterStatusName, LocalDateTime startDate, LocalDateTime endDate) {
        if (Objects.nonNull(labelId)) {
            Long totalByLabel = workRepository.countByLabelIdFiltered(labelId, startDate, endDate);
            Label label = labelRepository.findById(labelId).orElseThrow(() -> NotFoundException.with(Label.class, "labelId", labelId));
            response.setTotalPublishedWorksByLabel(List.of(new GetDashboardWorksByLabel(totalByLabel, label.getName())));
            return;
        }

        Pageable topFive = PageRequest.of(0, 5);
        List<WorksByLabelProjection> projections = workRepository.getCountsByLabelGrouped(filterStatusName, startDate, endDate, topFive);
        List<GetDashboardWorksByLabel> worksByLabelsList = projections.stream()
                .map(proj -> new GetDashboardWorksByLabel(proj.getTotal(), proj.getName()))
                .toList();

        response.setTotalPublishedWorksByLabel(worksByLabelsList);
    }

    private void setTotalWorksByAuthor(GetDashboardResponse response, Integer authorId, String filterStatusName, LocalDateTime startDate, LocalDateTime endDate) {
        if (Objects.nonNull(authorId)) {
            Long totalByAuthor = workRepository.countByAuthorIdFiltered(authorId, startDate, endDate);
            Author author = authorRepository.findById(authorId).orElseThrow(() -> NotFoundException.with(Author.class, "authorId", authorId));
            response.setTotalPublishedWorksByAuthor(List.of(new GetDashboardWorksByAuthor(totalByAuthor, author.getName())));
            return;
        }

        Pageable topFive = PageRequest.of(0, 5);
        List<WorksByAuthorProjection> projections = workRepository.getCountsByAuthorGrouped(filterStatusName, startDate, endDate, topFive);
        List<GetDashboardWorksByAuthor> worksByAuthorsList = projections.stream()
                .map(proj -> new GetDashboardWorksByAuthor(proj.getTotal(), proj.getName()))
                .toList();

        response.setTotalPublishedWorksByAuthor(worksByAuthorsList);
    }
}