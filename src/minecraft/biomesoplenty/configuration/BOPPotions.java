package biomesoplenty.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.potion.Potion;
import net.minecraftforge.common.MinecraftForge;
import biomesoplenty.api.Potions;
import biomesoplenty.potions.PotionEventHandler;
import biomesoplenty.potions.PotionNourishment;
import biomesoplenty.potions.PotionParalysis;

import com.google.common.base.Optional;

import cpw.mods.fml.common.registry.LanguageRegistry;

public class BOPPotions
{
	public static int potionOffset;
	private static final int MAXNEWPOTIONS = 8;

	public static void init()
	{
		extendPotionsArray();
		intializePotions();
		registerPotionNames();

		MinecraftForge.EVENT_BUS.register(new PotionEventHandler());
	}

	private static void intializePotions()
	{
		Potions.nourishment = Optional.of((new PotionNourishment(potionOffset + 0, false, 0)).setPotionName("potion.nourishment"));
		Potions.paralysis = Optional.of((new PotionParalysis(potionOffset + 1, true, 16767262)).setPotionName("potion.paralysis"));
	}

	private static void registerPotionNames()
	{
		LanguageRegistry.instance().addStringLocalization("potion.nourishment", "en_US", "Nourishment");
		LanguageRegistry.instance().addStringLocalization("potion.paralysis", "en_US", "Paralysis");
	}

	private static void extendPotionsArray()
	{
		System.out.println("[BiomesOPlenty] Extending Potions Array.");
		potionOffset = Potion.potionTypes.length;

		Potion[] potionTypes = new Potion[potionOffset + MAXNEWPOTIONS];
		System.arraycopy(Potion.potionTypes, 0, potionTypes, 0, potionOffset);

		Field field = null;
		Field[] fields = Potion.class.getDeclaredFields();

		for (Field f : fields)
			if (f.getName().equals("potionTypes") || f.getName().equals("field_76425_a"))
			{
				field = f;
				break;
			}

		try
		{
			field.setAccessible(true);

			Field modfield = Field.class.getDeclaredField("modifiers");
			modfield.setAccessible(true);
			modfield.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.set(null, potionTypes);
		}
		catch (Exception e)
		{
			System.err.println("[BiomesOPlenty] Severe error, please report this to the mod author:");
			System.err.println(e);
		}
	}
}
