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
    public String content;
    public LocalDateTime publishedAt;
    public String imageUrl;
    public List<AuthorsList> authors;
    public List<TeachersList> teachers;
    public List<LabelsList> labels;
    public List<LinksList> links;
    public WorkTypeResponse workType;
    public WorkStatusResponse workStatus;

    public static class LabelsList {
        public Integer id;
        public String name;
    }

    public static class AuthorsList {
        public Integer id;
        public String name;
        public Integer userId;
        
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

    public static class WorkTypeResponse {
        public Integer id;
        public String name;
    }

    public static class WorkStatusResponse {
        public Integer id;
        public String name;
    }

    @Autowired
    private ModelMapper modelMapper;

    public GetWorkResponse toPresentation(Work work) {
        Hibernate.initialize(work.getAuthors());
        Hibernate.initialize(work.getLabels());
        Hibernate.initialize(work.getLinks());
        Hibernate.initialize(work.getTeacher());
        Hibernate.initialize(work.getWorkType());
        Hibernate.initialize(work.getWorkStatus());
        
        work.getAuthors().forEach(author -> {
            if (author.getUser() != null) {
                Hibernate.initialize(author.getUser());
            }
        });
        
        GetWorkResponse response = modelMapper.map(work, GetWorkResponse.class);
        response.content = work.getContent();
        
        response.authors = work.getAuthors().stream()
                .map(this::mapAuthor)
                .toList();
        
        return response;
    }
    
    private AuthorsList mapAuthor(br.com.divulgaifback.modules.users.entities.Author author) {
        AuthorsList authorResponse = new AuthorsList();
        authorResponse.id = author.getId();
        authorResponse.name = author.getName();
        authorResponse.userId = author.getUser() != null ? author.getUser().getId() : null;
        return authorResponse;
    }

}