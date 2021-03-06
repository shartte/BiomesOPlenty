package biomesoplenty.items.projectiles;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.network.packet.Packet70GameEvent;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import biomesoplenty.BiomesOPlenty;
import biomesoplenty.ClientProxy;
import biomesoplenty.api.Potions;

public class EntityDart extends EntityArrow
{
	public static enum DartType
	{
		NORMAL, POISON;
	}

	boolean isClient = BiomesOPlenty.proxy instanceof ClientProxy;

	private int xTile = -1;
	private int yTile = -1;
	private int zTile = -1;
	private int ticksInAir = 0;
	private int damage = 2;
	private DartType type = DartType.NORMAL;

	public EntityDart(World par1World)
	{
		super(par1World);
	}

	public EntityDart(World par1World, EntityLiving par2EntityLiving, float par3)
	{
		super(par1World, par2EntityLiving, par3);
	}

	public EntityDart(World par1World, double par2, double par4, double par6)
	{
		super(par1World, par2, par4, par6);
	}

	@Override
	public void onUpdate()
	{
		super.onEntityUpdate();

		if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
		{
			float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
			prevRotationYaw = rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / Math.PI);
			prevRotationPitch = rotationPitch = (float)(Math.atan2(motionY, f) * 180.0D / Math.PI);
		}

		int i = worldObj.getBlockId(xTile, yTile, zTile);

		if (i > 0)
		{
			Block.blocksList[i].setBlockBoundsBasedOnState(worldObj, xTile, yTile, zTile);
			AxisAlignedBB axisalignedbb = Block.blocksList[i].getCollisionBoundingBoxFromPool(worldObj, xTile, yTile, zTile);

			if (axisalignedbb != null && axisalignedbb.isVecInside(worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ)))
			{
				this.setDead();
			}
		}

		++ticksInAir;
		Vec3 vec3 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		Vec3 vec31 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);
		MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks_do_do(vec3, vec31, false, true);
		vec3 = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		vec31 = worldObj.getWorldVec3Pool().getVecFromPool(posX + motionX, posY + motionY, posZ + motionZ);

		if (movingobjectposition != null)
		{
			vec31 = worldObj.getWorldVec3Pool().getVecFromPool(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
		}

		Entity entity = null;
		List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
		double d0 = 0.0D;
		int l;
		float f1;

		for (l = 0; l < list.size(); ++l)
		{
			Entity entity1 = (Entity)list.get(l);

			if (entity1.canBeCollidedWith() && (entity1 != shootingEntity || ticksInAir >= 5))
			{
				f1 = 0.3F;
				AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand(f1, f1, f1);
				MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3, vec31);

				if (movingobjectposition1 != null)
				{
					double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

					if (d1 < d0 || d0 == 0.0D)
					{
						entity = entity1;
						d0 = d1;
					}
				}
			}
		}

		if (entity != null)
		{
			movingobjectposition = new MovingObjectPosition(entity);
		}

		if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer)
		{
			EntityPlayer entityplayer = (EntityPlayer)movingobjectposition.entityHit;

			if (entityplayer.capabilities.disableDamage || shootingEntity instanceof EntityPlayer && !((EntityPlayer)shootingEntity).func_96122_a(entityplayer))
			{
				movingobjectposition = null;
			}
		}

		float f2;
		float f3;

		if (movingobjectposition != null)
		{
			if (movingobjectposition.entityHit != null)
			{
				f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);

				DamageSource damagesource = null;

				if (shootingEntity == null)
				{
					damagesource = DamageSource.causeArrowDamage(this, this);
				}
				else
				{
					damagesource = DamageSource.causeArrowDamage(this, shootingEntity);
				}

				if (type == DartType.POISON)
				{
					damage = 1;
					if (movingobjectposition.entityHit instanceof EntityLiving) {
						//                        ((EntityLiving)movingobjectposition.entityHit).addPotionEffect(new PotionEffect(Potion.poison.id, 100));
						((EntityLiving)movingobjectposition.entityHit).addPotionEffect(new PotionEffect(Potions.paralysis.get().id, 100));
					}
				}

				if (movingobjectposition.entityHit.attackEntityFrom(damagesource, damage))
				{
					if (movingobjectposition.entityHit instanceof EntityLiving)
					{
						if (shootingEntity != null && movingobjectposition.entityHit != shootingEntity && movingobjectposition.entityHit instanceof EntityPlayer && shootingEntity instanceof EntityPlayerMP)
						{
							((EntityPlayerMP)shootingEntity).playerNetServerHandler.sendPacketToPlayer(new Packet70GameEvent(6, 0));
						}
					}

					this.playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
					this.setDead();
				}
				else
				{
					motionX *= -0.10000000149011612D;
					motionY *= -0.10000000149011612D;
					motionZ *= -0.10000000149011612D;
					rotationYaw += 180.0F;
					prevRotationYaw += 180.0F;
					ticksInAir = 0;
				}
			}
			else
			{
				xTile = movingobjectposition.blockX;
				yTile = movingobjectposition.blockY;
				zTile = movingobjectposition.blockZ;
				motionX = ((float)(movingobjectposition.hitVec.xCoord - posX));
				motionY = ((float)(movingobjectposition.hitVec.yCoord - posY));
				motionZ = ((float)(movingobjectposition.hitVec.zCoord - posZ));
				f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
				posX -= motionX / f2 * 0.05000000074505806D;
				posY -= motionY / f2 * 0.05000000074505806D;
				posZ -= motionZ / f2 * 0.05000000074505806D;

				for (int p = 0; p < 16; ++p)
				{
					BiomesOPlenty.proxy.spawnParticle("dart", posX, posY, posZ);
				}

				this.playSound("random.bowhit", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
				this.setDead();
			}
		}

		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		f2 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float)(Math.atan2(motionX, motionZ) * 180.0D / Math.PI);

		for (rotationPitch = (float)(Math.atan2(motionY, f2) * 180.0D / Math.PI); rotationPitch - prevRotationPitch < -180.0F; prevRotationPitch -= 360.0F)
		{
			;
		}

		while (rotationPitch - prevRotationPitch >= 180.0F)
		{
			prevRotationPitch += 360.0F;
		}

		while (rotationYaw - prevRotationYaw < -180.0F)
		{
			prevRotationYaw -= 360.0F;
		}

		while (rotationYaw - prevRotationYaw >= 180.0F)
		{
			prevRotationYaw += 360.0F;
		}

		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
		float f4 = 0.99F;
		f1 = 0.05F;

		if (this.isInWater())
		{
			for (int j1 = 0; j1 < 4; ++j1)
			{
				f3 = 0.25F;
				worldObj.spawnParticle("bubble", posX - motionX * f3, posY - motionY * f3, posZ - motionZ * f3, motionX, motionY, motionZ);
			}

			f4 = 0.8F;
		}

		motionX *= f4;
		motionY *= f4;
		motionZ *= f4;
		motionY -= f1;
		this.setPosition(posX, posY, posZ);
		this.doBlockCollisions();
	}

	public void setDartType(DartType par1)
	{
		type = par1;

		byte b0 = dataWatcher.getWatchableObjectByte(16);

		if (par1 == DartType.POISON)
		{
			dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 1)));
		}
		else
		{
			dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & -2)));
		}
	}

	/**
	 * Whether the arrow has a stream of critical hit particles flying behind it.
	 */
	public boolean isPoisonous()
	{
		byte b0 = dataWatcher.getWatchableObjectByte(16);
		return (b0 & 1) != 0;
	}
}