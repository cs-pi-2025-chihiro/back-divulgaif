package br.com.divulgaifback.modules.works.entities;

import br.com.divulgaifback.common.entities.BaseEntity;
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
@Table(name = "links")
@SQLDelete(sql = "UPDATE links SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Link extends BaseEntity {

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "links", fetch = FetchType.LAZY)
    private Set<Work> worksAssociated = new HashSet<>();
}
