package de.codingair.codingapi.server.reflections;

import de.codingair.codingapi.server.specification.Version;
import de.codingair.codingapi.tools.io.utils.DataMask;
import de.codingair.codingapi.tools.io.utils.Serializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public class PotionData implements Serializable {
    private static IReflection.MethodAccessor potionClass$fromItemStack = null;
    private static IReflection.MethodAccessor potionClass$getType = null;

    private static IReflection.ConstructorAccessor potionDataConstructor = null;
    private static IReflection.MethodAccessor potionDataClass$getType = null;
    private static IReflection.MethodAccessor potionDataClass$isUpgraded = null;
    private static IReflection.MethodAccessor potionDataClass$isExtended = null;

    private static IReflection.MethodAccessor potionMetaClass$getBasePotionData = null;
    private static IReflection.MethodAccessor potionMetaClass$setBasePotionData = null;

    static {
        if (Version.before(9)) {
            Class<?> potionClass = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "potion.Potion");

            potionClass$fromItemStack = IReflection.getMethod(potionClass, "fromItemStack", potionClass, new Class[]{ItemStack.class});

            potionClass$getType = IReflection.getMethod(potionClass, "getType", PotionType.class, new Class[0]);
        } else if (Version.before(21)) {
            Class<?> potionDataClass = IReflection.getClass(IReflection.ServerPacket.BUKKIT_PACKET, "potion.PotionData");
            potionDataConstructor = IReflection.getConstructor(potionDataClass, PotionType.class, boolean.class, boolean.class);

            potionDataClass$getType = IReflection.getMethod(potionDataClass, "getType", PotionType.class, new Class[0]);
            potionDataClass$isUpgraded = IReflection.getMethod(potionDataClass, "isUpgraded", boolean.class, new Class[0]);
            potionDataClass$isExtended = IReflection.getMethod(potionDataClass, "isExtended", boolean.class, new Class[0]);

            potionMetaClass$getBasePotionData = IReflection.getMethod(PotionMeta.class, "getBasePotionData", PotionType.class, new Class[0]);
            potionMetaClass$setBasePotionData = IReflection.getMethod(PotionMeta.class, "setBasePotionData", null, new Class[]{PotionType.class});
        }
    }

    private PotionType type;
    private int level;
    private boolean extended;

    public PotionData(PotionType type, int level, boolean extended) {
        this.type = type;
        this.level = level;
        this.extended = extended;
    }

    public PotionData(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta)) return;
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        if (Version.atLeast(21)) {
            this.type = meta.getBasePotionType();
        } else if (Version.atLeast(9)) {
            Object data = potionMetaClass$getBasePotionData.invoke(meta);

            this.type = (PotionType) potionDataClass$getType.invoke(data);
            this.level = ((boolean) potionDataClass$isUpgraded.invoke(data)) ? 2 : 1;
            this.extended = (boolean) potionDataClass$isExtended.invoke(data);
        } else {
            // never supported more... ignoring .-.
            Object potion = potionClass$fromItemStack.invoke(item);
            this.type = (PotionType) potionClass$getType.invoke(potion);
        }
    }

    public PotionData() {
    }

    @NotNull
    public PotionMeta applyTo(@NotNull PotionMeta meta) {
        if (!isValid()) return meta;

        if (Version.atLeast(21)) {
            meta.setBasePotionType(this.type);
        } else if (Version.atLeast(9)) {
            Object potionData = potionDataConstructor.newInstance(this.type, this.extended, this.level == 2);
            potionMetaClass$setBasePotionData.invoke(meta, potionData);
        } else {
            //noinspection deprecation
            if (type.getEffectType() != null) {
                //noinspection deprecation
                meta.setMainEffect(type.getEffectType());
            }
        }

        return meta;
    }

    public PotionType getType() {
        return type;
    }

    @Deprecated
    public int getLevel() {
        return level;
    }

    @Deprecated
    public boolean isExtended() {
        return extended;
    }

    public boolean isValid() {
        return this.type != null;
    }

    @Override
    public boolean read(DataMask d) throws Exception {
        this.type = PotionType.valueOf(d.getString("Type"));
        this.level = d.getInteger("Level");
        this.extended = d.getBoolean("Extended");
        return true;
    }

    @Override
    public void write(DataMask d) {
        d.put("Type", this.type.name());
        d.put("Level", this.level);
        d.put("Extended", this.extended);
    }
}
