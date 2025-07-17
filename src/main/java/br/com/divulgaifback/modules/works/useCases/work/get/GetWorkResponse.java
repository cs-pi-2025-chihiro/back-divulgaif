package br.com.divulgaifback.modules.works.useCases.work.get;

import br.com.divulgaifback.modules.works.entities.Work;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class GetWorkResponse {
    public Integer id;
    public String title;
    public String description;
    public LocalDateTime publishedAt;
    public String imageUrl;
    public List<AuthorsList> authors;
    public List<TeachersList> teachers;
    public List<LabelsList> labels;
    public List<LinksList> links;

    public static class LabelsList {
        public Integer id;
        public String name;
    }

    public static class AuthorsList {
        public Integer id;
        public String name;
    }

    public static class TeachersList {
        public Integer id;
        public String name;
    }

    public static class LinksList {
        public Integer id;
        public String name;
        public String url;
    }

    @Autowired
    private ModelMapper modelMapper;

    public GetWorkResponse toPresentation(Work work) {
        Hibernate.initialize(work.getAuthors());
        Hibernate.initialize(work.getLabels());
        Hibernate.initialize(work.getLinks());
        Hibernate.initialize(work.getTeacher());
        return modelMapper.map(work, GetWorkResponse.class);
    }

}
