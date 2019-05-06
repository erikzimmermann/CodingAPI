package de.codingair.codingapi.player.gui.book;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.reflections.IReflection;
import de.codingair.codingapi.server.reflections.Packet;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.Removable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Book implements Removable {
    private final UUID uniqueId = UUID.randomUUID();
    private Player player;
    private JavaPlugin plugin;

    private String author;
    private String title;
    private List<Page> pages = new ArrayList<>();

    private ItemStack bookItem;

    public Book(Player player, JavaPlugin plugin) {
        this(player, "PLUGIN", "TITLE", plugin);
    }

    public Book(Player player, String title, JavaPlugin plugin) {
        this(player, "PLUGIN", title, plugin);
    }

    public Book(Player player, String author, String title, JavaPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
        this.author = author;
        this.title = title;

        this.bookItem = new ItemBuilder(XMaterial.WRITTEN_BOOK).getItem();
        API.addRemovable(this);
    }

    public void open() {
        int slot = this.player.getInventory().getHeldItemSlot();

        ItemStack original = this.player.getInventory().getItem(slot);

        writeBook();

        this.player.getInventory().setItem(slot, bookItem);
        callOpenPacket();
        this.player.getInventory().setItem(slot, original);
    }

    public void update() {
        open();
    }

    public Button getButton(UUID uniqueId) {
        for(Page page : pages) {
            Button button = page.getButton(uniqueId);
            if(button != null) return button;
        }

        return null;
    }

    private void callOpenPacket() {
        //open
        Class<?> PacketDataSerializerClass = IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketDataSerializer");
        IReflection.ConstructorAccessor dataSCon = IReflection.getConstructor(PacketDataSerializerClass, ByteBuf.class);

        Packet packet = new Packet(IReflection.getClass(IReflection.ServerPacket.MINECRAFT_PACKAGE, "PacketPlayOutCustomPayload"), this.player);
        packet.initialize(new Class[]{String.class, PacketDataSerializerClass}, "MC|BOpen", dataSCon.newInstance(Unpooled.buffer()));
        packet.send();
    }

    private void writeBook() {
        BookMeta meta = (BookMeta) this.bookItem.getItemMeta();

        meta.setTitle(this.title);
        meta.setAuthor(this.author);

        pages(meta).clear();
        pages(meta).addAll(getPageData());

        bookItem.setItemMeta(meta);
    }

    private List pages(BookMeta meta) {
        Class<?> CraftMetaBookClass = IReflection.getClass(IReflection.ServerPacket.CRAFTBUKKIT_PACKAGE, "inventory.CraftMetaBook");
        IReflection.FieldAccessor pages = IReflection.getField(CraftMetaBookClass, "pages");

        return (List) pages.get(CraftMetaBookClass.cast(meta));
    }

    private List getPageData() {
        List list = new ArrayList<>();

        for(Page page : this.pages) {
            list.add(page.getFinal());
        }

        return list;
    }

    public void addPage(Page page) {
        this.pages.add(page);
    }

    @Override
    public void destroy() {
        this.player.closeInventory();
        this.pages.clear();
        API.removeRemovable(this);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public List<Page> getPages() {
        return pages;
    }

    @Override
    public Class<? extends Removable> getAbstractClass() {
        return Book.class;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }
}
