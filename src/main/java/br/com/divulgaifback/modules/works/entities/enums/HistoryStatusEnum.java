package br.com.divulgaifback.modules.works.entities.enums;

public enum HistoryStatusEnum {
    PENDING_CHANGES(1),
    CORRECTED_CHANGES(2);

    public final Integer id;

    HistoryStatusEnum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
