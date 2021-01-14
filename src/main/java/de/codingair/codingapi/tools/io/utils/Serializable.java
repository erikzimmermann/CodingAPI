package de.codingair.codingapi.tools.io.utils;

public interface Serializable {
    boolean read(DataMask d) throws Exception;
    void write(DataMask d);

    void destroy();
}
