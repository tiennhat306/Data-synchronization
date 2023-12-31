package enums;

public enum FolderTypeId {
    ROOT(1),
    GENERAL(2),
    TRASH(3);

    private final int folderType;

    private FolderTypeId(int folderType) {
        this.folderType = folderType;
    }

    public int getValue() {
        return folderType;
    }
}
