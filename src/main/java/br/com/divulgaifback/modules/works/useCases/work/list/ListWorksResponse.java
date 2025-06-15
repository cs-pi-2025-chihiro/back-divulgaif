package br.com.divulgaifback.modules.works.useCases.work.list;

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
public class ListWorksResponse {
    public Integer id;
    public String title;
    public String description;
    public LocalDateTime publishedAt;
    public String imageUrl;
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

    public ListWorksResponse toPresentation(Work work) {
        Hibernate.initialize(work.getAuthors());
        Hibernate.initialize(work.getLabels());
        return modelMapper.map(work, ListWorksResponse.class);
    }
}
