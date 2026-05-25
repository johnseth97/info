package com.johnseth97.info.util;

public final class NameUtil {

    private NameUtil() {}

    /** Converts "OAK_LOG" or "ZOMBIE_VILLAGER" to "Oak Log" / "Zombie Villager". */
    public static String pretty(String enumName) {
        if (enumName == null || enumName.isBlank()) return "";
        String[] parts = enumName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            if (part.length() == 1) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            } else {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
