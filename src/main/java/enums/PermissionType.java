package enums;

public enum PermissionType {
    PRIVATE(1),
    READ(2),
    PUBLIC(3),
    OWNER(4);

    private final int permissionType;

    private PermissionType(int permissionType) {
        this.permissionType = permissionType;
    }

    public int getValue() {
        return permissionType;
    }

}
