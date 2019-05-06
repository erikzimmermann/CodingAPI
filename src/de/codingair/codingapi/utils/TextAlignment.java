package de.codingair.codingapi.utils;

import de.codingair.codingapi.server.DefaultFontInfo;
import de.codingair.codingapi.tools.Converter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Removing of this disclaimer is forbidden.
 *
 * @author codingair
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

        int lineIndex = 0;
        for(String line : lines) {
            if(lineIndex == lines.size() - 1) break;
            lineIndex++;

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
        String l = null;

        for(String line : lines) {
            if(line == null) continue;

            int length = DefaultFontInfo.getExactLength(line);
            if(longestLine < length) {
                longestLine = length;
                l = line;
            }
        }

        List<String> configured = new ArrayList<>();

        for(String line : lines) {
            if(line == null) continue;
            int messagePxSize = DefaultFontInfo.getExactLength(line);

            int toCompensate = (longestLine - messagePxSize) / 2 / DefaultFontInfo.SPACE.getLength();

            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < toCompensate; i++) {
                sb.append(" ");
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
        String[] lastColor = new String[]{"", ""};

        for(String word : words) {
            boolean isBold = false;
            boolean isColor = false;

            word = word + " ";

            String temp = de.codingair.codingapi.utils.ChatColor.getLastFullColor(word, ChatColor.COLOR_CHAR);
            lastColor[1] = temp.isEmpty() ? lastColor[1] : temp;

            for(char c : word.toCharArray()) {
                if(c == ChatColor.COLOR_CHAR) {
                    isColor = true;
                } else if(!isColor) {
                    DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    cells += isBold ? dFI.getBoldLength() : dFI.getLength();
                } else {
                    isColor = false;
                }
            }

            line.append(word);



            if(cells >= length) {
                line = new StringBuilder(line.toString().substring(0, line.length() - 1));
                lines.add(lastColor[0] + line.toString());
                line = new StringBuilder();
                cells = 0;

                lastColor[0] = lastColor[1];
                lastColor[1] = "";
            }
        }

        if(line.length() > 0) {
            lines.add(lastColor[0] + line.toString());
        }
        return lines;
    }


    private final static int CENTER_PX = 154;
    private final static int MAX_PX = 250;

    public static void sendCenteredMessage(Player player, String message) {
        if(message == null || message.equals("")) player.sendMessage("");
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()) {
            if(c == '§') {
                previousCode = true;
            } else if(previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        player.sendMessage(sb.toString() + message);
    }

    public static void sendCenteredMessage(Player player, BaseComponent base) {
        String message = ChatColor.translateAlternateColorCodes('&', base.toLegacyText());

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()) {
            if(c == '§') {
                previousCode = true;
                continue;
            } else if(previousCode) {
                previousCode = false;
                if(c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                } else isBold = false;
            } else if(c != ' ') {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }

            if(messagePxSize >= MAX_PX) {
//                throw new IllegalStateException("BaseComponent is to long!");
                return;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;

        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        TextComponent spaceBase = new TextComponent(sb.toString());
        spaceBase.addExtra(base);

        player.spigot().sendMessage(spaceBase);
    }
}
