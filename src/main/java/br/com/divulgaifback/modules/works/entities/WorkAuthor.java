package br.com.divulgaifback.modules.works.entities;


import br.com.divulgaifback.common.entities.BaseEntity;
import br.com.divulgaifback.modules.users.entities.Author;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_authors")
@SQLDelete(sql = "UPDATE work_authors SET deleted_at = CURRENT_TIMESTAMP WHERE work_id = ? AND author_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class WorkAuthor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", insertable = false, updatable = false)
    private Work work;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", insertable = false, updatable = false)
    private Author author;

    @Column(name = "is_primary")
    private Boolean isPrimary;
}
