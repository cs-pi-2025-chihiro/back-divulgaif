package br.com.divulgaifback.modules.works.entities.enums;

public enum WorkTypeEnum {
    ARTICLE(1),
    SEARCH(2),
    DISSERTATION(3),
    EXTENSION(4),
    FINAL_THESIS(5);

    public final Integer id;

    WorkTypeEnum(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
