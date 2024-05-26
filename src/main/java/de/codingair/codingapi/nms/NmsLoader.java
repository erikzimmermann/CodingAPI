package de.codingair.codingapi.nms;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to annotate empty constructors that can be used to initialize classes to run their static body, which mostly is used for NMS preparation. Constructors may be private.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NmsLoader {
}
