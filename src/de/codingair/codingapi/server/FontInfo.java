package de.codingair.codingapi.server;

import org.bukkit.ChatColor;

public enum FontInfo {
    A('A', 26),
    a('a', 19),
    B('B', 20),
    b('b', 19),
    C('C', 22),
    c('c', 17),
    D('D', 21),
    d('d', 19),
    E('E', 20),
    e('e', 18),
    F('F', 20),
    f('f', 13),
    G('G', 22),
    g('g', 18),
    H('H', 23),
    h('h', 17),
    I('I', 11),
    i('i', 5),
    J('J', 17),
    j('j', 11),
    K('K', 19),
    k('k', 17),
    L('L', 19),
    l('l', 5),
    M('M', 28),
    m('m', 25),
    N('N', 22),
    n('n', 17),
    O('O', 26),
    o('o', 18),
    P('P', 18),
    p('p', 19),
    Q('Q', 27),
    q('q', 20),
    R('R', 18),
    r('r', 14),
    S('S', 20),
    s('s', 16),
    T('T', 23),
    t('t', 11),
    U('U', 23),
    u('u', 17),
    V('V', 23),
    v('v', 18),
    W('W', 30),
    w('w', 26),
    X('X', 26),
    x('x', 19),
    Y('Y', 25),
    y('y', 17),
    Z('Z', 25),
    z('z', 18),
    NUM_1('1', 13),
    NUM_2('2', 20),
    NUM_3('3', 19),
    NUM_4('4', 22),
    NUM_5('5', 20),
    NUM_6('6', 21),
    NUM_7('7', 22),
    NUM_8('8', 20),
    NUM_9('9', 21),
    NUM_0('0', 22),
    EXCLAMATION_POINT('!', 7),
    AT_SYMBOL('@', 30),
    NUM_SIGN('#', 22),
    DOLLAR_SIGN('$', 25),
    PERCENT('%', 30),
    UP_ARROW('^', 13),
    AMPERSAND('&', 22),
    ASTERISK('*', 11),
    LEFT_PARENTHESIS('(', 11),
    RIGHT_PERENTHESIS(')', 11),
    MINUS('-', 15),
    UNDERSCORE('_', 26),
    PLUS_SIGN('+', 20),
    EQUALS_SIGN('=', 19),
    LEFT_CURL_BRACE('{', 16),
    RIGHT_CURL_BRACE('}', 16),
    LEFT_BRACKET('[', 11),
    RIGHT_BRACKET(']', 11),
    COLON(':', 5),
    SEMI_COLON(';', 7),
    DOUBLE_QUOTE('"', 14),
    SINGLE_QUOTE('\'', 7),
    LEFT_ARROW('<', 14),
    RIGHT_ARROW('>', 14),
    QUESTION_MARK('?', 18),
    SLASH('/', 18),
    BACK_SLASH('\\', 17),
    LINE('|', 4),
    TILDE('~', 20),
    TICK('`', 6),
    PERIOD('.', 5),
    COMMA(',', 7),
    SPACE(' ', 8);

    public static final double CHAT_WIDTH = 1480.5;
    private static final double SPACE_BETWEEN_CHARS = 5.5;
    private char character;
    private double length;

    FontInfo(char character, double length) {
        this.character = character;
        this.length = length;
    }

    public double getSpaceAfter(char c) {
        double length = byChar(c).getLength();

        if(length <= 5) return 2;
        if(length <= 10) return 3;
        if(length <= 20) return 4;
        if(length <= 30) return 5;
        if(length <= 40) return 6;
        else return 7;
    }

    public static FontInfo byChar(char c) {
        for(FontInfo fontInfo : values()) {
            if(fontInfo.getCharacter() == c) return fontInfo;
        }

        return null;
    }

    public char getCharacter() {
        return character;
    }

    public double getLength() {
        return length;
    }

    public double getBoldLength() {
        if(this == FontInfo.SPACE) return this.getLength();
        return this.length + 1.0;
    }

    public static double getExactLength(String text) {
        double length = 0;
        boolean isColor = false;
        boolean isBold = false;

        for(char c : text.toCharArray()) {
            if(c == ChatColor.COLOR_CHAR) {
                isColor = true;
            } else if(isColor) {
                isColor = false;
                isBold = c == 'l' || c == 'L';
            } else {
                FontInfo info = byChar(c);
                if(info == null) continue;
                length += isBold ? info.getBoldLength() : info.getLength();
                length += SPACE_BETWEEN_CHARS;
            }
        }

        return length;
    }

    public static String center(String s) {
        double length = getExactLength(s);

        if(length >= CHAT_WIDTH) return s;

        double toCompensate = (CHAT_WIDTH - length) / 2;
        double space = toCompensate / (SPACE.getLength() + 1 + SPACE_BETWEEN_CHARS);
        int spaces = (int) Math.floor(space);

        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < spaces; i++) {
            builder.append(" ");
        }

        builder.append(s);

        return builder.toString();
    }
}
