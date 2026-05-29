package kr.farmily.api.common.upload;

import java.util.Set;

public enum UploadKind {
    DIARY("diary", 10 * 1024 * 1024),
    PROFILE_BG("profile/bg", 10 * 1024 * 1024),
    PROFILE_AVATAR("profile/avatar", 5 * 1024 * 1024),
    STORY("profile/story", 10 * 1024 * 1024),
    CONTENT_EXTRA("content/extra", 10 * 1024 * 1024);

    public final String prefix;
    public final long maxBytes;
    public static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    public static final Set<String> ALLOWED_MIMES =
            Set.of("image/jpeg", "image/png", "image/webp");

    UploadKind(String prefix, long maxBytes) {
        this.prefix = prefix;
        this.maxBytes = maxBytes;
    }

    public static String mimeOf(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}
