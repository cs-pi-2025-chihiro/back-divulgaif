package br.com.divulgaifback.modules.users.entities;

import br.com.divulgaifback.common.entities.BaseEntity;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.works.entities.Work;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "authors")
@SQLDelete(sql = "UPDATE authors SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Author extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "type", length = 100)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Work> works = new HashSet<>();

}
