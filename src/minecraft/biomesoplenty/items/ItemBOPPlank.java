package biomesoplenty.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBOPPlank extends ItemBlock
{
	private static final String[] woodTypes = new String[] {"acaciaPlank", "cherryPlank", "darkPlank", "firPlank", "holyPlank", "magicPlank", "mangrovePlank", "palmPlank", "redwoodPlank", "willowPlank", "bambooThatching", "pinePlank", "hellBarkPlank", "jacarandaPlank"};

	public ItemBOPPlank(int par1)
	{
		super(par1);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta)
	{
		return meta & 15;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		int meta = itemStack.getItemDamage();
		if (meta < 0 || meta >= woodTypes.length) {
			meta = 0;
		}

		return woodTypes[meta];
	}
}
