package kr.farmily.api.common.util;

import java.util.regex.Pattern;

public final class HandleValidator {

    private static final Pattern HANDLE_PATTERN = Pattern.compile("^[a-z0-9-]{3,30}$");

    private HandleValidator() {}

    public static boolean isValid(String handle) {
        return handle != null && HANDLE_PATTERN.matcher(handle).matches();
    }
}
