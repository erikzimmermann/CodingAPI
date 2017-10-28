package de.codingair.codingapi.utils;

import de.codingair.codingapi.server.DefaultFontInfo;
import de.codingair.codingapi.tools.Converter;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author CodingAir
 * @verions: 1.0.0
 **/

public enum TextAlignment {
    LEFT,
    CENTER,
    JUSTIFY;

    public List<String> apply(List<String> lines) {
        if(this.equals(CENTER)) return center(lines);
        if(this.equals(JUSTIFY)) return justify(lines);

        List<String> other = new ArrayList<>();
        other.addAll(lines);
        return other;
    }

    public List<String> apply(String... lines) {
        return apply(Converter.fromArrayToList(lines));
    }

    private static int getLength(String text) {
        boolean isBold = false;
        boolean isColor = false;

        if(text == null) return 0;

        int length = 0;
        text = ChatColor.translateAlternateColorCodes('&', text);

        for(char c : text.toCharArray()) {
            if(c == ChatColor.COLOR_CHAR) {
                isColor = true;
            } else if(isColor) {
                isColor = false;
                if(c == 'l' || c == 'L') {
                    isBold = true;
                } else isBold = false;
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                length += isBold ? dFI.getBoldLength() : dFI.getLength();
            }
        }

        return length;
    }

    private List<String> justify(List<String> lines) {
        int longestLine = 0;

        for(String line : lines) {
            if(line == null) continue;

            int length = getLength(line);

            if(longestLine < length) longestLine = length;
        }

        List<String> configured = new ArrayList<>();

        for(String line : lines) {
            if(line == null) {
                configured.add(null);
                continue;
            }

            String[] words = line.split(" ");
            if(words.length <= 1) {
                configured.add(line);
                continue;
            }

            int messagePxSize = getLength(line);

            int toCompensate = longestLine - messagePxSize;
            int afterWord = toCompensate / (words.length - 1);

            int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
            int compensated = 0;

            StringBuilder sb = new StringBuilder();
            while(compensated < afterWord) {
                sb.append(" ");
                compensated += spaceLength;
            }

            StringBuilder newLine = new StringBuilder();

            for(int i = 0; i < words.length; i++) {
                if(i + 1 == words.length) newLine.append(words[i]);
                else newLine.append(words[i]).append(sb.toString().length() == 0 ? " " : sb.toString());
            }

            configured.add(newLine.toString());
        }

        return configured;
    }

    private List<String> center(List<String> lines) {
        int longestLine = 0;

        boolean isBold = false;
        boolean isColor = false;
        for(String line : lines) {
            if(line == null) continue;

            int length = 0;
            line = ChatColor.translateAlternateColorCodes('&', line);

            for(char c : line.toCharArray()) {
                if(c == ChatColor.COLOR_CHAR) {
                    isColor = true;
                } else if(isColor) {
                    isColor = false;
                    if(c == 'l' || c == 'L') {
                        isBold = true;
                    } else isBold = false;
                } else {
                    DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    length += isBold ? dFI.getBoldLength() : dFI.getLength();
                }
            }

            if(longestLine < length) longestLine = length;
        }

        List<String> configured = new ArrayList<>();

        for(String line : lines) {
            if(line == null) continue;

            int messagePxSize = 0;
            isColor = false;
            isBold = false;

            for(char c : line.toCharArray()) {
                if(c == ChatColor.COLOR_CHAR) {
                    isColor = true;
                } else if(isColor) {
                    isColor = false;
                    if(c == 'l' || c == 'L') {
                        isBold = true;
                        continue;
                    } else isBold = false;
                } else {
                    DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                }
            }

            int toCompensate = (longestLine - messagePxSize) / 2;
            int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
            int compensated = 0;

            StringBuilder sb = new StringBuilder();
            while(compensated < toCompensate) {
                sb.append(" ");
                compensated += spaceLength;
            }

            configured.add(sb.toString() + line);
        }

        return configured;
    }

    public static List<String> lineBreak(String text, int length) {
        List<String> lines = new ArrayList<>();

        if(text == null || length >= getLength(text)) {
            lines.add(text);
            return lines;
        }

        String[] words = text.split(" ");
        int cells = 0;
        StringBuilder line = new StringBuilder();
        StringBuilder lastColor = new StringBuilder();

        for(String word : words) {
            boolean isBold = false;
            boolean isColor = false;
            boolean wasColor = false;

            word = word + " ";

            for(char c : word.toCharArray()) {
                if(c == ChatColor.COLOR_CHAR) {
                    isColor = true;
                } else if(isColor) {
                    isColor = false;
                    if(c == 'l' || c == 'L') {
                        isBold = true;
                    } else isBold = false;

                    if(wasColor) lastColor.append(ChatColor.COLOR_CHAR + c);
                    else lastColor = new StringBuilder(ChatColor.COLOR_CHAR + c);

                    wasColor = true;
                } else {
                    DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    cells += isBold ? dFI.getBoldLength() : dFI.getLength();
                    wasColor = false;
                }
            }

            line.append(word);

            if(cells >= length) {
                line = new StringBuilder(line.substring(0, line.length() - 1));

                lines.add(lastColor + line.toString());
                line = new StringBuilder();
                cells = 0;
            }

        }

        if(line.length() > 0) lines.add(lastColor + line.toString());
        return lines;
    }

}
