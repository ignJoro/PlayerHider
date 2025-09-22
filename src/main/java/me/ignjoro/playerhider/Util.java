package me.ignjoro.playerhider;

import org.bukkit.ChatColor;
import java.util.HashMap;
import java.util.Map;

public class Util {

    private static final Map<Character, Character> SMALL_CAPS_MAP = new HashMap<>();
    private static final Map<String, ChatColor> COLOR_MAP = new HashMap<>();

    static {
        SMALL_CAPS_MAP.put('a', 'ᴀ');
        SMALL_CAPS_MAP.put('b', 'ʙ');
        SMALL_CAPS_MAP.put('c', 'ᴄ');
        SMALL_CAPS_MAP.put('d', 'ᴅ');
        SMALL_CAPS_MAP.put('e', 'ᴇ');
        SMALL_CAPS_MAP.put('f', 'ꜰ');
        SMALL_CAPS_MAP.put('g', 'ɢ');
        SMALL_CAPS_MAP.put('h', 'ʜ');
        SMALL_CAPS_MAP.put('i', 'ɪ');
        SMALL_CAPS_MAP.put('j', 'ᴊ');
        SMALL_CAPS_MAP.put('k', 'ᴋ');
        SMALL_CAPS_MAP.put('l', 'ʟ');
        SMALL_CAPS_MAP.put('m', 'ᴍ');
        SMALL_CAPS_MAP.put('n', 'ɴ');
        SMALL_CAPS_MAP.put('o', 'ᴏ');
        SMALL_CAPS_MAP.put('p', 'ᴘ');
        SMALL_CAPS_MAP.put('q', 'ǫ');
        SMALL_CAPS_MAP.put('r', 'ʀ');
        SMALL_CAPS_MAP.put('s', 's');
        SMALL_CAPS_MAP.put('t', 'ᴛ');
        SMALL_CAPS_MAP.put('u', 'ᴜ');
        SMALL_CAPS_MAP.put('v', 'ᴠ');
        SMALL_CAPS_MAP.put('w', 'ᴡ');
        SMALL_CAPS_MAP.put('x', 'x');
        SMALL_CAPS_MAP.put('y', 'ʏ');
        SMALL_CAPS_MAP.put('z', 'ᴢ');

        COLOR_MAP.put("BLACK", ChatColor.BLACK);
        COLOR_MAP.put("DARK_BLUE", ChatColor.DARK_BLUE);
        COLOR_MAP.put("DARK_GREEN", ChatColor.DARK_GREEN);
        COLOR_MAP.put("DARK_AQUA", ChatColor.DARK_AQUA);
        COLOR_MAP.put("DARK_RED", ChatColor.DARK_RED);
        COLOR_MAP.put("DARK_PURPLE", ChatColor.DARK_PURPLE);
        COLOR_MAP.put("GOLD", ChatColor.GOLD);
        COLOR_MAP.put("GRAY", ChatColor.GRAY);
        COLOR_MAP.put("DARK_GRAY", ChatColor.DARK_GRAY);
        COLOR_MAP.put("BLUE", ChatColor.BLUE);
        COLOR_MAP.put("GREEN", ChatColor.GREEN);
        COLOR_MAP.put("AQUA", ChatColor.AQUA);
        COLOR_MAP.put("RED", ChatColor.RED);
        COLOR_MAP.put("LIGHT_PURPLE", ChatColor.LIGHT_PURPLE);
        COLOR_MAP.put("YELLOW", ChatColor.YELLOW);
        COLOR_MAP.put("WHITE", ChatColor.WHITE);
    }

    public static String text(String input) {
        StringBuilder result = new StringBuilder();
        ChatColor currentColor = ChatColor.GRAY;

        String[] parts = input.split(" ");

        for (String part : parts) {
            if (part.startsWith("$") && COLOR_MAP.containsKey(part.substring(1))) {
                currentColor = COLOR_MAP.get(part.substring(1));
            } else {
                StringBuilder sb = new StringBuilder();
                for (char c : part.toLowerCase().toCharArray()) {
                    sb.append(SMALL_CAPS_MAP.getOrDefault(c, c));
                }
                result.append(currentColor).append(sb).append(" ");
            }
        }

        return result.toString().trim();
    }
}
