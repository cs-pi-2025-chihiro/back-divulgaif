package br.com.divulgaifback.modules.works.useCases.work.update;

import br.com.divulgaifback.modules.works.entities.Work;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class UpdateWorkResponse {
    public Integer id;
    public String title;
    public String description;
    public String content;
    public String principalLink;
    public String metaTag;
    public WorkStatusResponse workStatus;
    public WorkTypeResponse workType;
    public UserResponse teacher;
    public Set<AuthorResponse> authors;
    public Set<LabelResponse> labels;
    public Set<LinkResponse> links;

    @Autowired
    private ModelMapper modelMapper;

    public static class WorkStatusResponse {
        public Integer id;
        public String name;
    }

    public static class WorkTypeResponse {
        public Integer id;
        public String name;
    }

    public static class AuthorResponse {
        public Integer id;
        public String name;
        public String email;
        public String type;
        public UserResponse user;
    }

    public static class UserResponse {
        public Integer id;
        public String name;
        public String email;
    }

    public static class LabelResponse {
        public Integer id;
        public String name;
        public String color;
    }

    public static class LinkResponse {
        public Integer id;
        public String name;
        public String url;
        public String description;
    }

    public UpdateWorkResponse toPresentation(Work work) {
        return modelMapper.map(work, UpdateWorkResponse.class);
    }
}