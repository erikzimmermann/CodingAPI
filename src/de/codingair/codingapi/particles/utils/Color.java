package de.codingair.codingapi.particles.utils;

public enum Color {
    RED(java.awt.Color.RED, "Red"),
    ORANGE(java.awt.Color.ORANGE, "Orange"),
    YELLOW(java.awt.Color.YELLOW, "Yellow"),
    GREEN(java.awt.Color.GREEN, "Green"),
    CYAN(java.awt.Color.CYAN, "Cyan"),
    BLUE(java.awt.Color.BLUE, "Blue"),
    MAGENTA(java.awt.Color.MAGENTA, "Magenta"),
    PINK(java.awt.Color.PINK, "Pink"),
    WHITE(java.awt.Color.WHITE, "White"),
    LIGHT_GRAY(java.awt.Color.LIGHT_GRAY, "LightGray"),
    GRAY(java.awt.Color.GRAY, "Gray"),
    DARK_GRAY(java.awt.Color.DARK_GRAY, "DarkGray"),
    BLACK(java.awt.Color.BLACK, "Black"),
    RAINBOW(null, "Rainbow"),
    ;

    public static int RAINBOW_COLOR_LENGTH = 8;
    private java.awt.Color c;
    private String name;

    Color(java.awt.Color c, String name) {
        this.c = c;
        this.name = name;
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
