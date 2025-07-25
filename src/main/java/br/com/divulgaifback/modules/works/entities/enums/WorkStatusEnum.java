package br.com.divulgaifback.modules.works.entities.enums;

public enum WorkStatusEnum {
    DRAFT(1),
    SUBMITTED(2),
    PENDING_CHANGES(3),
    PUBLISHED(4),
    REJECTED(5);

    public final Integer id;

    WorkStatusEnum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
