package enums;

public enum TypeEnum {
    EOF(-1),
    FOLDER(1),
    FILE(2);

    private final int type;

    private TypeEnum(int type) {
        this.type = type;
    }

    public int getValue() {
        return type;
    }
}
