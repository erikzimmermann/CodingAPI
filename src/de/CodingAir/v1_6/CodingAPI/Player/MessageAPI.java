package de.CodingAir.v1_6.CodingAPI.Player;

import de.CodingAir.v1_6.CodingAPI.Server.Reflections.IReflection;
import de.CodingAir.v1_6.CodingAPI.Server.Reflections.PacketUtils;
import de.CodingAir.v1_6.CodingAPI.Server.Version;
import de.CodingAir.v1_6.CodingAPI.Time.Countdown;
import de.CodingAir.v1_6.CodingAPI.Time.CountdownListener;
import de.CodingAir.v1_6.CodingAPI.Time.TimeFetcher;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class MessageAPI {
    private static HashMap<String, Countdown> countdowns = new HashMap<>();

    public static void sendActionBar(Player p, String message) {
        Object com = PacketUtils.getChatMessage(message);

        Class<?> packet = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutChat");

        Object bar;

        if(Version.getVersion().isBiggerThan(Version.v1_11)) {
            Class<?> type = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "ChatMessageType");
            IReflection.MethodAccessor a = IReflection.getMethod(type, "a", type, new Class[] {byte.class});
            IReflection.ConstructorAccessor constructor = IReflection.getConstructor(packet, PacketUtils.ChatMessageClass, type);

            bar = constructor.newInstance(com, a.invoke(null, (byte) 2));
        } else {
            IReflection.ConstructorAccessor constructor = IReflection.getConstructor(packet, PacketUtils.ChatMessageClass, Byte.class);
            bar = constructor.newInstance(com, (byte) 2);
        }

        PacketUtils.sendPacket(p, bar);
    }

    public static void sendActionBar(Player p, String message, Plugin plugin, int seconds) {
        Countdown countdown = countdowns.get(p.getName());
        if(countdown != null) {
            countdown.end();
        }

        if(seconds <= 0) {
            sendActionBar(p, "");
            return;
        }

        countdown = new Countdown(plugin, TimeFetcher.Time.SECONDS, seconds);
        countdown.addListener(new CountdownListener() {
            @Override
            protected void CountdownStartEvent() {
                sendActionBar(p, message);
            }

            @Override
            protected void CountdownEndEvent() {
                countdowns.remove(p.getName());
            }

            @Override
            protected void CountdownCancelEvent() {
            }

            @Override
            protected void onTick(int timeLeft) {
                sendActionBar(p, message);
            }
        });

        countdown.start();

        countdowns.put(p.getName(), countdown);
    }

    public static void sendTitle(Player p, String msg1, String msg2, int fadeIn, int stay, int fadeOut) {
        if(msg1 == null) msg1 = "";
        if(msg2 == null) msg2 = "";

        Class<?> packet = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutTitle");
        Class<?> enumTitle = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutTitle$EnumTitleAction");
        IReflection.ConstructorAccessor constructor = IReflection.getConstructor(packet, enumTitle, PacketUtils.ChatMessageClass, Integer.class, Integer.class, Integer.class);

        IReflection.MethodAccessor getNames = IReflection.getMethod(enumTitle, "a", String[].class, null);
        IReflection.MethodAccessor getEnum = IReflection.getMethod(enumTitle, "a", enumTitle, new Class[] {String.class});

        String[] names = (String[]) getNames.invoke(enumTitle);

        Object title = constructor.newInstance(getEnum.invoke(enumTitle, names[0]), PacketUtils.getChatMessage(msg1), fadeIn, stay, fadeOut);
        Object subTitle = constructor.newInstance(getEnum.invoke(enumTitle, names[1]), PacketUtils.getChatMessage(msg2), fadeIn, stay, fadeOut);

        PacketUtils.sendPacket(p, title);
        PacketUtils.sendPacket(p, subTitle);
    }

    public static void sendTablist(Player p, String header, String footer) {
        if(header == null) header = "";
        if(footer == null) footer = "";

        Class<?> packetClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutPlayerListHeaderFooter");
        IReflection.ConstructorAccessor constructor = IReflection.getConstructor(packetClass, PacketUtils.ChatMessageClass);
        IReflection.FieldAccessor b = IReflection.getField(packetClass, "b");

        Object tabHeader = PacketUtils.getChatMessage(header);
        Object tabFooter = PacketUtils.getChatMessage(footer);

        Object packet = constructor.newInstance(tabHeader);

        b.set(packet, tabFooter);

        PacketUtils.sendPacket(p, packet);
    }

}
