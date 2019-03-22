package de.codingair.codingapi.utils;

import de.codingair.codingapi.server.reflections.IReflection;

import java.util.*;
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

    public static final char COLOR_CHAR = getColorChar();
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");
    private static final Map<Character, ChatColor> BY_CHAR = new HashMap();
    private final char code;
    private final String toString;
    private final String name;

    private ChatColor(char code, String name) {
        this.code = code;
        this.name = name;
        this.toString = new String(new char[] {getColorChar(), code});
    }

    private static char getColorChar() {
        try {
            return (char) IReflection.getField(Class.forName("net.md_5.bungee.api.ChatColor"), "COLOR_CHAR").get(null);
        } catch(ClassNotFoundException e) {
            try {
                return (char) IReflection.getField(Class.forName("org.bukkit.ChatColor"), "COLOR_CHAR").get(null);
            } catch(ClassNotFoundException e1) {
                e1.printStackTrace();
                return '?';
            }
        }
    }

    public String toString() {
        return this.toString;
    }

    public static String stripColor(String input) {
        return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if(b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(code);
    }

    public String getName() {
        return this.name;
    }

    static {
        ChatColor[] arr$ = values();

        for(ChatColor colour : arr$) {
            BY_CHAR.put(colour.code, colour);
        }

    }

    public <T> T toBungeeCode() {
        try {
            for(Object o : Class.forName("net.md_5.bungee.api.ChatColor").getEnumConstants()) {
                if(o.toString().equals(this.toString())) return (T) o;
            }
        } catch(ClassNotFoundException ignored) {
        }

        return null;
    }

    public <T> T toSpigotCode() {
        try {
            for(Object o : Class.forName("org.bukkit.ChatColor").getEnumConstants()) {
                if(o.toString().equals(this.toString())) return (T) o;
            }
        } catch(ClassNotFoundException ignored) {
        }

        return null;
    }

    public <T> T toOriginal() {
        return toSpigotCode() == null ? toBungeeCode() : toSpigotCode();
    }

    public Object toOriginal(boolean bukkit) {
        if(bukkit) return toSpigotCode();
        else return toBungeeCode();
    }

    public static String getLastColor(String text, char colorChar) {
        if(!text.contains("" + colorChar)) return null;
        String[] data = text.split("" + colorChar);
        return colorChar + data[data.length - 1].substring(0, 1);
    }

    public static String getLastFullColor(String text, char colorChar) {
        String otherColor = getLastColor(text, colorChar);
        if(otherColor == null) return "";

        ChatColor[] extra = new ChatColor[] {MAGIC, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC};

        String stripped = text;
        for(ChatColor c : extra) {
            stripped = stripped.replace(c.toString(), "");
        }

        String lastColor = getLastColor(stripped, colorChar);

        if(!otherColor.equals(lastColor)) {
            for(ChatColor c : extra) {
                if(otherColor.equals(c.toString())) {
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
        return highlight(text, toHighlight, highlighter, RESET.toString, false);
    }

    public static String highlight(String text, String toHighlight, String highlighter, String resetColor, boolean ignoreCase) {
        if(toHighlight == null || toHighlight.isEmpty() || highlighter == null || highlighter.isEmpty()) return text;

        List<String> data = new ArrayList<>(Arrays.asList(ignoreCase ? text.toLowerCase().split(toHighlight.toLowerCase(), -1) : text.split(toHighlight, -1)));

        if(ignoreCase) {
            int size = data.size();
            int index = 0;
            for(int i = 0; i < size; i++) {
                String value = data.get(i);
                if(value.isEmpty()) continue;

                StringBuilder corrected = new StringBuilder();

                for(int i1 = 0; i1 < value.length(); i1++) {
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
        for(int i = 0; i < data.size(); i++) {
            String current = data.get(i);
            String color = getLastColor(current, '§');
            if(color != null) lastColor = color;

            builder.append(current);

            if(i < data.size() - 1) {
                index += current.length();

                StringBuilder highlight = new StringBuilder();
                for(int j = index; j < index + toHighlight.length(); j++) {
                    highlight.append(text.charAt(j));
                }

                builder.append(highlighter).append(highlight.toString()).append(resetColor).append(lastColor);
                index += toHighlight.length();
            }
        }

        return builder.toString();
    }
}
