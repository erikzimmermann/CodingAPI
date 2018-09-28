package de.codingair.codingapi.files.loader;

public class Comment {
    private String comment;
    private int line;

    public Comment(String comment, int line) {
        this.comment = comment;
        this.line = line;
    }

    public String getComment() {
        return comment;
    }

    public int getLine() {
        return line;
    }
}
