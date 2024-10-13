package de.codingair.codingapi.server.blocks;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.codingapi.API;
import de.codingair.codingapi.server.blocks.data.BlockData;
import de.codingair.codingapi.server.specification.Version;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ModernBlock {
    private final Block block;
    private Material material;
    private BlockData data;

    public ModernBlock(Block block) {
        this.block = block;
    }

    private void prepareBlock() {
        UniversalScheduler.getScheduler(API.getInstance().getMainPlugin()).runTask(
                block.getLocation(),
                () -> {
                    if (this.data != null) {
                        if (!Version.atLeast(13))
                            this.data.setTypeAndDataTo(this.block, this.material, (byte) this.data.getData(block), false);
                        else {
                            block.setType(material);
                            this.data.setDataTo(block, this.data.getData(block));
                        }
                    } else {
                        block.setType(material);
                    }
                }
        );
    }

    public Block getBlock() {
        return block;
    }

    public BlockData getData() {
        return data;
    }

    public ModernBlock setData(BlockData data) {
        this.data = data;
        prepareBlock();
        return this;
    }

    public ModernBlock setTypeAndData(Material material, BlockData data) {
        this.material = material;
        this.data = data;
        prepareBlock();
        return this;
    }

    public ModernBlock setType(Material material) {
        this.material = material;
        prepareBlock();
        return this;
    }
}
