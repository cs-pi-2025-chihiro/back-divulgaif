package br.com.divulgaifback.modules.works.entities;

import br.com.divulgaifback.common.entities.BaseEntity;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "works")
@SQLDelete(sql = "UPDATE works SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Work extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "principal_link", length = 500)
    private String principalLink;

    @Column(name = "meta_tag", length = 500)
    private String metaTag;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "work_status_id", nullable = false)
    private WorkStatus workStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "work_type_id")
    private WorkType workType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "work_authors",
            joinColumns = @JoinColumn(name = "work_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "work_labels",
            joinColumns = @JoinColumn(name = "work_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "work_links",
            joinColumns = @JoinColumn(name = "work_id"),
            inverseJoinColumns = @JoinColumn(name = "link_id")
    )
    private Set<Link> links = new HashSet<>();

    public void addAuthor(Author author) {
        authors.add(author);
        author.getWorks().add(this);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getWorks().remove(this);
    }

    public void addLabel(Label label) {
        labels.add(label);
        label.getWorks().add(this);
    }

    public void removeLabel(Label label) {
        labels.remove(label);
        label.getWorks().remove(this);
    }

    public void addLink(Link link) {
        links.add(link);
        link.getWorksAssociated().add(this);
    }

    public void removeLink(Link link) {
        links.remove(link);
        link.getWorksAssociated().remove(this);
    }

    public boolean isSubmitted() {
        return submittedAt != null;
    }

    public boolean isApproved() {
        return approvedAt != null;
    }
}

