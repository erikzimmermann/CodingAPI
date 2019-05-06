package de.codingair.codingapi.files.loader;

public class Extra {
    private String text;
    private int line;

    public Extra(String text, int line) {
        this.text = text;
        this.line = line;
    }

    public String getText() {
        return text;
    }

    public int getLine() {
        return line;
    }
}
