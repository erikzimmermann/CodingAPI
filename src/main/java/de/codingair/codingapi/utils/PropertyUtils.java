package de.codingair.codingapi.utils;

import com.mojang.authlib.properties.Property;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.specification.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utils class to access the value and signature values of the {@link com.mojang.authlib.properties.Property} class.
 */
public class PropertyUtils {

    @Nullable
    public static String getValue(@NotNull Property property) {
        IReflection.MethodAccessor getValue = IReflection.getMethod(Property.class, Version.choose("getValue", 20.02, "value"), String.class, new Class<?>[0]);
        return (String) getValue.invoke(property);
    }

    @Nullable
    public static String getSignature(@NotNull Property property) {
        IReflection.MethodAccessor getValue = IReflection.getMethod(Property.class, Version.choose("getSignature", 20.02, "signature"), String.class, new Class<?>[0]);
        return (String) getValue.invoke(property);
    }

}
