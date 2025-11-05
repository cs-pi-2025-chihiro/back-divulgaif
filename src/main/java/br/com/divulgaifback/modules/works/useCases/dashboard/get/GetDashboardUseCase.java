package br.com.divulgaifback.modules.works.useCases.dashboard.get;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.QAuthor;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.entities.QLabel;
import br.com.divulgaifback.modules.works.entities.QWork;
import br.com.divulgaifback.modules.works.entities.QWorkStatus;
import br.com.divulgaifback.modules.works.repositories.LabelRepository;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse.GetDashboardWorksByStatus;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse.GetDashboardWorksByLabel;
import br.com.divulgaifback.modules.works.useCases.dashboard.get.GetDashboardResponse.GetDashboardWorksByAuthor;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkRepository.WorksByStatusProjection;
import br.com.divulgaifback.modules.works.repositories.WorkRepository.WorksByLabelProjection;
import br.com.divulgaifback.modules.works.repositories.WorkRepository.WorksByAuthorProjection;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.annotation.Secured;
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

    private final JPAQueryFactory queryFactory;

    private static final QWork work = QWork.work;
    private static final QWorkStatus workStatus = QWorkStatus.workStatus;
    private static final QLabel label = QLabel.label;
    private static final QAuthor author = QAuthor.author;

    private static final String PUBLISHED_STATUS = WorkStatusEnum.PUBLISHED.name();

    @Secured({"IS_ADMIN", "IS_TEACHER"})
    @Transactional(readOnly = true)
    public GetDashboardResponse execute(GetDashboardRequest request) {
        GetDashboardResponse response = new GetDashboardResponse();

        Integer statusId = Objects.nonNull(request) ? request.statusId() : null;
        Integer labelId = Objects.nonNull(request) ? request.labelId() : null;
        Integer authorId = Objects.nonNull(request) ? request.authorId() : null;
        LocalDateTime startDate = Objects.nonNull(request) ? request.startDate() : null;
        LocalDateTime endDate = Objects.nonNull(request) ? request.endDate() : null;

        setTotalWorksByStatus(response, statusId, startDate, endDate);
        setTotalWorksByLabel(response, labelId, startDate, endDate);
        setTotalWorksByAuthor(response, authorId, startDate, endDate);
        return response;
    }

    private void setTotalWorksByStatus(GetDashboardResponse response, Integer statusId, LocalDateTime startDate, LocalDateTime endDate) {

        var query = queryFactory
                .select(Projections.constructor(GetDashboardWorksByStatus.class,
                        work.count(),
                        workStatus.name))
                .from(work)
                .join(work.workStatus, workStatus);


        if (Objects.nonNull(statusId)) query.where(workStatus.id.eq(statusId));

        if (Objects.nonNull(startDate)) query.where(work.createdAt.goe(startDate));

        if (Objects.nonNull(endDate)) query.where(work.createdAt.loe(endDate));

        List<GetDashboardWorksByStatus> results = query.groupBy(workStatus.id, workStatus.name).fetch();

        Map<String, Long> statusCountMap = results.stream()
                .collect(Collectors.toMap(
                        GetDashboardWorksByStatus::getStatus,
                        GetDashboardWorksByStatus::getTotal
                ));

        List<GetDashboardWorksByStatus> worksByStatusList = new ArrayList<>();

        List<WorkStatusEnum> allStatus = (Objects.nonNull(statusId))
                ? List.of(WorkStatusEnum.fromId(statusId))
                : List.of(WorkStatusEnum.values());

        for (WorkStatusEnum status : allStatus) {
            Long totalByStatus = statusCountMap.getOrDefault(status.name(), 0L);
            worksByStatusList.add(new GetDashboardWorksByStatus(totalByStatus, status.name()));
        }
        response.setTotalWorksByStatus(worksByStatusList);
    }

    private void setTotalWorksByLabel(GetDashboardResponse response, Integer labelId, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable topFive = PageRequest.of(0, 5);

        var query = queryFactory
                .select(Projections.constructor(GetDashboardWorksByLabel.class,
                        work.count(),
                        label.name))
                .from(work)
                .join(work.labels, label)
                .join(work.workStatus, workStatus)
                .where(workStatus.name.eq(PUBLISHED_STATUS));

        if (Objects.nonNull(labelId)) query.where(label.id.eq(labelId));
        if (Objects.nonNull(startDate)) query.where(work.createdAt.goe(startDate));
        if (Objects.nonNull(endDate)) query.where(work.createdAt.loe(endDate));

        query.groupBy(label.id, label.name).orderBy(work.count().desc());

        if (Objects.isNull(labelId)) query.limit(topFive.getPageSize()).offset(topFive.getOffset());

        List<GetDashboardWorksByLabel> results = query.fetch();

        if (Objects.nonNull(labelId) && results.isEmpty()) {
            Label l = labelRepository.findById(labelId)
                    .orElseThrow(() -> NotFoundException.with(Label.class, "labelId", labelId));
            response.setTotalPublishedWorksByLabel(List.of(new GetDashboardWorksByLabel(0L, l.getName())));
            return;
        }

        response.setTotalPublishedWorksByLabel(results);
    }

    private void setTotalWorksByAuthor(GetDashboardResponse response, Integer authorId, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable topFive = PageRequest.of(0, 5);

        var query = queryFactory
                .select(Projections.constructor(GetDashboardWorksByAuthor.class,
                        work.count(),
                        author.name))
                .from(work)
                .join(work.authors, author)
                .join(work.workStatus, workStatus)
                .where(workStatus.name.eq(PUBLISHED_STATUS));

        if (Objects.nonNull(authorId)) query.where(author.id.eq(authorId));
        if (Objects.nonNull(startDate)) query.where(work.createdAt.goe(startDate));
        if (Objects.nonNull(endDate)) query.where(work.createdAt.loe(endDate));

        query.groupBy(author.id, author.name).orderBy(work.count().desc());

        if (Objects.isNull(authorId)) query.limit(topFive.getPageSize()).offset(topFive.getOffset());

        List<GetDashboardWorksByAuthor> results = query.fetch();

        if (Objects.nonNull(authorId) && results.isEmpty()) {
            Author a = authorRepository.findById(authorId).orElseThrow(() -> NotFoundException.with(Author.class, "authorId", authorId));
            response.setTotalPublishedWorksByAuthor(List.of(new GetDashboardWorksByAuthor(0L, a.getName())));
            return;
        }

        response.setTotalPublishedWorksByAuthor(results);
    }
}