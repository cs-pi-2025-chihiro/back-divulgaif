package br.com.divulgaifback.modules.users.entities.enums;

public enum RoleEnum {
    IS_STUDENT("IS_STUDENT"),
    IS_TEACHER("IS_TEACHER"),
    IS_ADMIN("IS_ADMIN");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
