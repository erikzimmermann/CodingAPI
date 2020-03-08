package de.codingair.codingapi.tools.io.utils;

public interface Serializable {
    boolean read(DataWriter d) throws Exception;
    void write(DataWriter d);

    void destroy();
}
