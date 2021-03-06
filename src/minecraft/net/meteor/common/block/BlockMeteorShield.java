package net.meteor.common.block;

import java.util.ArrayList;
import java.util.Random;

import net.meteor.common.ClientHandler;
import net.meteor.common.ClientProxy;
import net.meteor.common.HandlerAchievement;
import net.meteor.common.HandlerMeteor;
import net.meteor.common.LangLocalization;
import net.meteor.common.MeteorsMod;
import net.meteor.common.tileentity.TileEntityMeteorShield;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMeteorShield extends BlockContainer
{
	
	private Icon topUnlit;
	private Icon bottom;
	private Icon gemSide;
	private Icon noGemSide;
	private Icon crackedSide;
	
	public BlockMeteorShield(int i)
	{
		super(i, Material.rock);
		this.setLightOpacity(0);
		setTickRandomly(true);
	}

	@Override
	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLiving, ItemStack itemstack)
	{
		if ((par5EntityLiving instanceof EntityPlayer)) {
			EntityPlayer player = (EntityPlayer)par5EntityLiving;
			if (!par1World.isRemote) {
				player.sendChatToPlayer(ClientHandler.createMessage(LangLocalization.get("MeteorShield.charging"), EnumChatFormatting.YELLOW));
			}
			TileEntityMeteorShield shield = (TileEntityMeteorShield) par1World.getBlockTileEntity(par2, par3, par4);
			shield.owner = player.username;
			par1World.playSoundEffect(par2, par3, par4, "meteors:shield.humm", 1.0F, 1.0F);
		}
	}

	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
	{
		int meta = par6;
		if (!par1World.isRemote) {
			Chunk chunk = par1World.getChunkFromBlockCoords(par2, par4);
			TileEntityMeteorShield shield = (TileEntityMeteorShield)par1World.getBlockTileEntity(par2, par3, par4);
			MeteorsMod.proxy.metHandlers.get(par1World.provider.dimensionId).removeSafeChunks(chunk.xPosition, chunk.zPosition, MeteorsMod.instance.ShieldRadiusMultiplier * meta, shield.owner);
			par1World.playSoundEffect(par2 + 0.5D, par3 + 0.5D, par4 + 0.5D, "meteors:shield.powerdown", 1.0F, 1.0F);
		}
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	@Override
	public Icon getIcon(int i, int j)
	{
		if (i == 1) {
			if (j > 0) {
				return this.blockIcon;
			}
			return this.topUnlit;
		}
		if (i == 0)
			return this.bottom;
		if (j == 5)
			return this.crackedSide;
		if ((i == 2) && (j > 1))
			return this.gemSide;
		if ((i == 3) && (j > 2))
			return this.gemSide;
		if ((i == 4) && (j > 3))
			return this.gemSide;
		if ((i == 5) && (j > 4)) {
			return this.gemSide;
		}
		return this.noGemSide;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("meteors:shieldTop_lit");
		this.topUnlit = par1IconRegister.registerIcon("meteors:shieldTop_unlit");
		this.bottom = par1IconRegister.registerIcon("meteors:shieldBottom");
		this.crackedSide = par1IconRegister.registerIcon("meteors:shieldSideCracked");
		this.gemSide = par1IconRegister.registerIcon("meteors:sideGem");
		this.noGemSide = par1IconRegister.registerIcon("meteors:sideNoGem");
	}

	@Override
	public void updateTick(World world, int i, int j, int k, Random random)
	{
		int meta = world.getBlockMetadata(i, j, k);
		if (meta <= 0) {
			world.setBlockMetadataWithNotify(i, j, k, 1, 2);
			if (!world.isRemote) {
				TileEntityMeteorShield shield = (TileEntityMeteorShield) world.getBlockTileEntity(i, j, k);
				if (shield.owner != null && shield.owner.length() > 0) {
					EntityPlayer player = world.getPlayerEntityByName(shield.owner);
					if (player != null) {
						player.sendChatToPlayer(ClientHandler.createMessage(LangLocalization.get("MeteorShield.PowerUpgradePercentage") + " 20%", EnumChatFormatting.GREEN));
						player.sendChatToPlayer(ClientHandler.createMessage(LangLocalization.get("MeteorShield.howToUpgrade"), EnumChatFormatting.GOLD));
					}
				}
			}
		} else if (meta > 5) {
			world.setBlockMetadataWithNotify(i, j, k, 5, 2);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random)
	{
		if (random.nextInt(24) == 0) {
			world.playSound(i + 0.5D, j + 0.5D, k + 0.5D, "meteors:shield.humm", 1.0F, 1.0F, false);
		}
		if (world.getBlockMetadata(i, j, k) >= 1) {
			return;
		}
		for (int l = i - 2; l <= i + 2; l++)
			for (int i1 = k - 2; i1 <= k + 2; i1++) {
				if ((l > i - 2) && (l < i + 2) && (i1 == k - 1)) {
					i1 = k + 2;
				}
				if (random.nextInt(16) == 0)
				{
					for (int j1 = j; (j1 <= j + 1) && (world.isAirBlock((l - i) / 2 + i, j1, (i1 - k) / 2 + k)); j1++)
					{
						ClientProxy.spawnParticle("meteorshield", i + 0.5D, j + 2.0D, k + 0.5D, l - i + random.nextFloat() - 0.5D, j1 - j - random.nextFloat() - 1.0F, i1 - k + random.nextFloat() - 0.5D, world, -1);
					}
				}
			}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		ItemStack cItem = player.inventory.getCurrentItem();
		if (cItem == null) {
			return false;
		}

		// For future photo-ops lol
		//		if (cItem.itemID == up.D.cj) {
		//			if ((world.h(i, j, k) > 0) && (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)) {
		//				TileEntityMeteorShield shield = (TileEntityMeteorShield)world.q(i, j, k);
		//				player.a(new StringBuilder().append("Owner: ").append(shield.owner).toString());
		//				EnumMeteor met = (EnumMeteor)MeteorsMod.proxy.lastMeteorPrevented.get(shield.owner);
		//				player.a(new StringBuilder().append("Orig. ID: ").append(met != null ? met.getID() : -1).toString());
		//				int id;
		//				int id;
		//				if (met != null)
		//				{
		//					int id;
		//					if (met.getID() < 4)
		//						id = met.getID() + 1;
		//					else
		//						id = 0;
		//				}
		//				else {
		//					player.a("Type was null.");
		//					id = 1;
		//				}
		//				MeteorsMod.proxy.lastMeteorPrevented.put(shield.owner, EnumMeteor.getTypeFromID(id));
		//				ClientHandler.sendShieldProtectUpdate(shield.owner);
		//				player.a(new StringBuilder().append("Set to ID: ").append(id).toString());
		//			}
		//			return true;
		//		}

		if ((cItem.itemID == MeteorsMod.itemMeteorChips.itemID) && (world.getBlockMetadata(i, j, k) == 0)) {
			TileEntityMeteorShield shield = (TileEntityMeteorShield)world.getBlockTileEntity(i, j, k);
			shield.owner = player.username;
			this.updateTick(world, i, j, k, world.rand);
			world.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, "meteors:shield.powerup", 1.0F, 0.6F);
			if (!player.capabilities.isCreativeMode) cItem.stackSize--;
			return true;
		}
		if (cItem.itemID == MeteorsMod.itemRedMeteorGem.itemID) {
			int meta = world.getBlockMetadata(i, j, k);
			if ((meta > 0) && (meta < 5)) {
				boolean sendNoUpgradeMsg = false;
				if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
					Chunk chunk = world.getChunkFromBlockCoords(i, k);
					TileEntityMeteorShield shield = (TileEntityMeteorShield)world.getBlockTileEntity(i, j, k);
					HandlerMeteor meteorHandler = MeteorsMod.proxy.metHandlers.get(world.provider.dimensionId);
					meteorHandler.removeSafeChunks(chunk.xPosition, chunk.zPosition, MeteorsMod.instance.ShieldRadiusMultiplier * meta, shield.owner);
					meta++;
					meteorHandler.addSafeChunks(chunk.xPosition, chunk.zPosition, MeteorsMod.instance.ShieldRadiusMultiplier * meta, shield.owner);
					if (MeteorsMod.instance.ShieldRadiusMultiplier <= 0)
						sendNoUpgradeMsg = true;
				} else {
					meta++;
				}
				if (sendNoUpgradeMsg) {
					player.sendChatToPlayer(ChatMessageComponent.func_111077_e(LangLocalization.get("MeteorShield.noUpgrade")));
				}
				world.setBlockMetadataWithNotify(i, j, k, meta, 2);
				world.markBlockForUpdate(i, j, k);
				world.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, "meteors:shield.powerup", 1.0F, meta / 10.0F + 0.5F);
				if (!player.capabilities.isCreativeMode) cItem.stackSize--;
				if (!world.isRemote) {
					player.sendChatToPlayer(ClientHandler.createMessage(LangLocalization.get("MeteorShield.PowerUpgradePercentage") + " " + 20 * meta + "%", EnumChatFormatting.GREEN));
				}
				if (meta >= 5) {
					player.addStat(HandlerAchievement.shieldFullyUpgraded, 1);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return new TileEntityMeteorShield();
	}

	@Override
	public int getLightValue(IBlockAccess bAccess, int x, int y, int z)
	{
		if (bAccess.getBlockMetadata(x, y, z) == 5) {
			return 15;
		}
		return super.getLightValue(bAccess, x, y, z);
	}

	@Override
	public String getLocalizedName()
	{
		return LangLocalization.get(this.getUnlocalizedName() + ".name");
	}
	
	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = super.getBlockDropped(world, x, y, z, metadata, fortune);
		int gems = metadata > 1 ? metadata - 1 : 0;
		for (int i = 0; i < gems; i++) {
			ret.add(new ItemStack(MeteorsMod.itemRedMeteorGem, 1));
		}
		return ret;
	}
	
}