package me.cratecore.utils;

import org.bukkit.ChatColor;
import java.util.List;
import java.util.stream.Collectors;

public class ColorUtils {
    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    public static List<String> color(List<String> list) {
        return list.stream().map(ColorUtils::color).collect(Collectors.toList());
    }
}
