package de.CodingAir.v1_6.CodingAPI.Server.Reflections;

import de.CodingAir.v1_6.CodingAPI.Server.Version;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PotionData {
    private PotionType type;
    private int level;
    private boolean splash;
    private boolean extended;

    public PotionData(PotionType type, int level, boolean splash, boolean extended) {
        this.type = type;
        this.level = level;
        this.splash = splash;
        this.extended = extended;
    }

    public PotionData(ItemStack item) {
        if(Version.getVersion().isBiggerThan(Version.v1_8)) {
            if(item.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                org.bukkit.potion.PotionData data = meta.getBasePotionData();

                this.type = data.getType();
                this.level = data.isUpgraded() ? 2 : 1;
                this.splash = false;
                this.extended = data.isExtended();
            }
        } else {
            Potion potion = Potion.fromItemStack(item);

            this.type = potion.getType();
            this.level = potion.getLevel();
            this.splash = potion.isSplash();
            this.extended = potion.hasExtendedDuration();
        }
    }

//    public short toDamageValue() {
//        if (this.type == PotionType.WATER) {
//            return 0;
//        } else {
//            short damage;
//            if (this.type == null) {
//                damage = (short)(this.name == 0 ? 8192 : this.name);
//            } else {
//                damage = (short)(this.level - 1);
//                damage = (short)(damage << 5);
//                damage |= (short)this.type.getDamageValue();
//            }
//
//            if (this.splash) {
//                damage = (short)(damage | 16384);
//            }
//
//            if (this.extended) {
//                damage = (short)(damage | 64);
//            }
//
//            return damage;
//        }
//    }

    public PotionData(Potion potion) {
        this.type = potion.getType();
        this.level = potion.getLevel();
        this.splash = potion.isSplash();
        this.extended = potion.hasExtendedDuration();
    }

    public PotionType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public boolean isSplash() {
        return splash;
    }

    public boolean isExtended() {
        return extended;
    }

    public PotionMeta getMeta() {
        if(getPotion() == null) return null;
        return (PotionMeta) getPotion().toItemStack(1).getItemMeta();
    }

    public Potion getPotion() {
        if(this.type == null) return null;
        return new Potion(this.type, this.level, this.splash, this.extended);
    }

    public org.bukkit.potion.PotionData getBukkitData() {
        return new org.bukkit.potion.PotionData(this.type, this.extended, this.level == 2);
    }

    public boolean isCorrect() {
        return this.type != null;
    }

    public String toJSONString() {
        JSONObject object = new JSONObject();
        object.put("Type", this.type.name());
        object.put("Level", this.level);
        object.put("Splash", this.splash);
        object.put("Extended", this.extended);
        return object.toJSONString();
    }

    public static PotionData fromJSONString(String code) {
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(code);

            PotionType type = PotionType.valueOf((String) json.get("Type"));
            int level = Integer.parseInt(json.get("Level") + "");
            boolean splash = (boolean) json.get("Splash");
            boolean extended = (boolean) json.get("Extended");

            return new PotionData(type, level, splash, extended);
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
