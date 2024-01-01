package enums;

public enum Role {
    USER((short) 1),
    ADMIN((short) 2);

    private final short role;

    private Role(short role) {
        this.role = role;
    }

    public short getValue() {
        return role;
    }
}
