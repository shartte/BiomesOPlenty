package biomesoplenty.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBOPMoss extends ItemBlock
{
	@SideOnly(Side.CLIENT)
	private Icon texture;

	public ItemBOPMoss(int par1)
	{
		super(par1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		texture = iconRegister.registerIcon("BiomesOPlenty:item_moss");
	}

	@Override
	public Icon getIconFromDamage(int meta)
	{
		return texture;
	}
}
