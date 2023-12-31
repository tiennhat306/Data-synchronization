package enums;

public enum UploadStatus {
    SUCCESS(1),
    FAILED(2),
    EXISTED(3),
    PERMISSION_DENIED(4),
    PERMISSION_ACCEPTED(5);

    private final int uploadStatus;

    private UploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public int getValue() {
        return uploadStatus;
    }

}
