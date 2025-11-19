package br.com.divulgaifback.modules.works.entities.enums;

public enum WorkStatusEnum {
    DRAFT(1),
    SUBMITTED(2),
    PENDING_CHANGES(3),
    PUBLISHED(4),
    REJECTED(5) ;

    public final Integer id;

    WorkStatusEnum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public static WorkStatusEnum fromId(Integer id) {
        for (WorkStatusEnum status : WorkStatusEnum.values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid WorkStatusEnum id: " + id);
    }
}
