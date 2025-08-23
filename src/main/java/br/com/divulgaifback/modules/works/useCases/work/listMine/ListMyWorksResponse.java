package br.com.divulgaifback.modules.works.useCases.work.listMine;

import br.com.divulgaifback.modules.works.entities.Work;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Setter
public class ListMyWorksResponse {
    public Integer id;
    public String title;
    public String description;
    public LocalDateTime publishedAt;
    public String imageUrl;
    public String status;
    public String type;
    public List<AuthorsList> authors;
    public List<LabelsList> labels;

    public static class LabelsList {
        public Integer id;
        public String name;
    }

    public static class AuthorsList {
        public Integer id;
        public String name;
    }

    @Autowired
    private ModelMapper modelMapper;

    public ListMyWorksResponse toPresentation(Work work) {
        ListMyWorksResponse response;
        Hibernate.initialize(work.getAuthors());
        Hibernate.initialize(work.getLabels());
        response = modelMapper.map(work, ListMyWorksResponse.class);
        response.status = work.getWorkStatus().getName();
        response.type = work.getWorkType().getName();
        return response;
    }
}
