package de.codingair.codingapi.particles.utils;

public enum Color {
    RED(java.awt.Color.RED, "Red", 6),
    ORANGE(java.awt.Color.ORANGE, "Orange", 3),
    YELLOW(java.awt.Color.YELLOW, "Yellow", 2),
    GREEN(java.awt.Color.GREEN, "Green", 0),
    CYAN(java.awt.Color.CYAN, "Cyan", 18),
    BLUE(java.awt.Color.BLUE, "Blue", 14),
    MAGENTA(java.awt.Color.MAGENTA, "Magenta", 8),
    PINK(java.awt.Color.PINK, "Pink", 9),
    WHITE(java.awt.Color.WHITE, "White", "Green 2", 1),
    LIGHT_GRAY(java.awt.Color.LIGHT_GRAY, "LightGray", "Blueish-Green", 19),
    GRAY(java.awt.Color.GRAY, "Gray", "Cyan 2", 17),
    DARK_GRAY(java.awt.Color.DARK_GRAY, "DarkGray", "Orange 2", 4),
    BLACK(java.awt.Color.BLACK, "Black", "Purple", 11),
    RAINBOW(null, "Rainbow", 0),
    ;

    public static int RAINBOW_COLOR_LENGTH = 8;
    public static int RAINBOW_NOTE_COLOR_LENGTH = 24;
    private final java.awt.Color c;
    private final String name;
    private final String noteName;
    private final int noteColor;

    Color(java.awt.Color c, String name, int noteColor) {
        this.c = c;
        this.name = name;
        this.noteName = name;
        this.noteColor = noteColor;
    }

    Color(java.awt.Color c, String name, String noteName, int noteColor) {
        this.c = c;
        this.name = name;
        this.noteName = noteName;
        this.noteColor = noteColor;
    }

    public int getNoteColor() {
        return noteColor;
    }

    public String getNoteName() {
        return noteName;
    }

    public java.awt.Color getColor() {
        return c;
    }

    public String getName() {
        return name;
    }

    public Color next() {
        return next(this);
    }

    public Color previous() {
        return previous(this);
    }

    public static Color next(Color color) {
        for(int i = 0; i < values().length; i++) {
            if(values()[i] == color) return i + 1 == values().length ? values()[0] : values()[i + 1];
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with name=" + color.getName());
    }

    public static Color previous(Color color) {
        for(int i = 0; i < values().length; i++) {
            if(values()[i] == color) {
                return i - 1 < 0 ? values()[values().length - 1] : values()[i - 1];
            }
        }

        throw new IllegalArgumentException("Couldn't found AnimationType with name=" + color.getName());
    }
}
