package noname.blockbuster.recording.actions;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import noname.blockbuster.entity.EntityActor;

/**
 * Guess what this action does
 *
 * Does exactly what you think, no less, no more.
 */
public class PlaceBlockAction extends InteractBlockAction
{
    public byte metadata;
    public NBTTagCompound itemData = new NBTTagCompound();

    public PlaceBlockAction()
    {}

    public PlaceBlockAction(BlockPos pos, byte metadata, ItemStack item)
    {
        super(pos);
        this.metadata = metadata;

        item.writeToNBT(this.itemData);
    }

    @Override
    public byte getType()
    {
        return Action.PLACE_BLOCK;
    }

    @Override
    public void apply(EntityActor actor)
    {
        ItemStack item = ItemStack.loadItemStackFromNBT(this.itemData);

        if (item.getItem() instanceof ItemBlock)
        {
            ItemBlock block = (ItemBlock) item.getItem();

            block.placeBlockAt(item, actor.fakePlayer, actor.worldObj, this.pos, EnumFacing.UP, 0, 0, 0, block.block.getStateFromMeta(this.metadata));
        }
    }

    @Override
    public void fromBytes(DataInput in) throws IOException
    {
        super.fromBytes(in);

        this.metadata = in.readByte();
        this.itemData = CompressedStreamTools.read((DataInputStream) in);
    }

    @Override
    public void toBytes(DataOutput out) throws IOException
    {
        super.toBytes(out);

        out.writeByte(this.metadata);
        CompressedStreamTools.write(this.itemData, out);
    }
}
