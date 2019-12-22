package de.codingair.codingapi.tools.io;

public interface Serializable {
    boolean read(DataWriter d) throws Exception;
    void write(DataWriter d);

    void destroy();
}
