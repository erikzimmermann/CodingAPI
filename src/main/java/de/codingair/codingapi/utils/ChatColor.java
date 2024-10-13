package de.codingair.codingapi.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ChatColor {
    BLACK('0', "black"),
    DARK_BLUE('1', "dark_blue"),
    DARK_GREEN('2', "dark_green"),
    DARK_AQUA('3', "dark_aqua"),
    DARK_RED('4', "dark_red"),
    DARK_PURPLE('5', "dark_purple"),
    GOLD('6', "gold"),
    GRAY('7', "gray"),
    DARK_GRAY('8', "dark_gray"),
    BLUE('9', "blue"),
    GREEN('a', "green"),
    AQUA('b', "aqua"),
    RED('c', "red"),
    LIGHT_PURPLE('d', "light_purple"),
    YELLOW('e', "yellow"),
    WHITE('f', "white"),
    MAGIC('k', "obfuscated"),
    BOLD('l', "bold"),
    STRIKETHROUGH('m', "strikethrough"),
    UNDERLINE('n', "underline"),
    ITALIC('o', "italic"),
    RESET('r', "reset");

    public static final char COLOR_CHAR = '\u00A7';
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    public static final String ALL_HEX_CODES = "0123456789AaBbCcDdEeFf";
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[x0-9A-FK-OR]");
    private static final Map<Character, ChatColor> BY_CHAR = new HashMap<>();

    static {
        ChatColor[] arr$ = values();

        for (ChatColor colour : arr$) {
            BY_CHAR.put(colour.code, colour);
        }
    }

    private final char code;
    private final String toString;
    private final String name;

    ChatColor(char code, String name) {
        this.code = code;
        this.name = name;
        this.toString = new String(new char[]{COLOR_CHAR, code});
    }

    public static String stripColor(String input) {
        return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * @author Elementeral (https://www.spigotmc.org/threads/hex-color-code-translate.449748/#post-3867804)
     */
    public static String translateHexColorCodes(char altColorChar, String message) {
        final Pattern hexPattern = Pattern.compile(altColorChar + "#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length());

        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }

        return matcher.appendTail(buffer).toString();
    }

    public static String toLegacy(char altColorChar, String message) {
        if (message == null) return null;
        return hexToLegacy(altColorChar, message).replace(COLOR_CHAR, altColorChar);
    }

    public static String hexToLegacy(char altColorChar, String message) {
        final Pattern hexPattern = Pattern.compile(COLOR_CHAR + "x" + COLOR_CHAR + "." + COLOR_CHAR + "." + COLOR_CHAR + "." + COLOR_CHAR + "." + COLOR_CHAR + "." + COLOR_CHAR + ".");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length());

        while (matcher.find()) {
            String group = matcher.group(0);
            matcher.appendReplacement(buffer, altColorChar + "#"
                    + group.charAt(3)
                    + group.charAt(5)
                    + group.charAt(7)
                    + group.charAt(9)
                    + group.charAt(11)
                    + group.charAt(13)
            );
        }

        return matcher.appendTail(buffer).toString();
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && ALL_CODES.indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static String translateAll(char altColorChar, String textToTranslate) {
        if (textToTranslate == null) return null;
        return translateAlternateColorCodes(altColorChar, translateHexColorCodes(altColorChar, textToTranslate));
    }

    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(code);
    }

    public static String getLastColor(String text, char colorChar) {
        if (!text.contains("" + colorChar)) return null;
        String[] data = text.split("" + colorChar);
        return colorChar + data[data.length - 1].substring(0, 1);
    }

    public static String getLastFullColor(String text, char colorChar) {
        String otherColor = getLastColor(text, colorChar);
        if (otherColor == null) return "";

        ChatColor[] extra = new ChatColor[]{MAGIC, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC};

        String stripped = text;
        for (ChatColor c : extra) {
            stripped = stripped.replace(c.toString(), "");
        }

        String lastColor = getLastColor(stripped, colorChar);

        if (!otherColor.equals(lastColor)) {
            for (ChatColor c : extra) {
                if (otherColor.equals(c.toString())) {
                    return otherColor + lastColor;
                }
            }
        }

        return lastColor;
    }

    public static String highlight(String text, String toHighlight, String highlighter) {
        return highlight(text, toHighlight, highlighter, RESET.toString);
    }

    public static String highlight(String text, String toHighlight, String highlighter, String resetColor) {
        return highlight(text, toHighlight, highlighter, resetColor, false);
    }

    public static String highlight(String text, String toHighlight, String highlighter, String resetColor, boolean ignoreCase) {
        if (toHighlight == null || toHighlight.isEmpty() || highlighter == null || highlighter.isEmpty()) return text;

        List<String> data = new ArrayList<>(Arrays.asList(ignoreCase ? text.toLowerCase().split(toHighlight.toLowerCase(), -1) : text.split(toHighlight, -1)));

        if (ignoreCase) {
            int size = data.size();
            int index = 0;
            for (int i = 0; i < size; i++) {
                String value = data.get(i);
                if (value.isEmpty()) continue;

                StringBuilder corrected = new StringBuilder();

                for (int i1 = 0; i1 < value.length(); i1++) {
                    int origin = index + corrected.length() + i * toHighlight.length();
                    corrected.append(text.charAt(origin));
                }

                data.set(i, corrected.toString());
                index += corrected.length();
            }
        }

        StringBuilder builder = new StringBuilder();
        String lastColor = "";

        int index = 0;
        for (int i = 0; i < data.size(); i++) {
            String current = data.get(i);
            String color = getLastColor(current, '§');
            if (color != null) lastColor = color;

            builder.append(current);

            if (i < data.size() - 1) {
                index += current.length();

                StringBuilder highlight = new StringBuilder();
                for (int j = index; j < index + toHighlight.length(); j++) {
                    highlight.append(text.charAt(j));
                }

                builder.append(highlighter).append(highlight).append(resetColor).append(lastColor);
                index += toHighlight.length();
            }
        }

        return builder.toString();
    }

    public String toString() {
        return this.toString;
    }

    public String getName() {
        return this.name;
    }
}
