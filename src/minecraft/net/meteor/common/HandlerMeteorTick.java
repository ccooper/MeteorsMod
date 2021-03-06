package net.meteor.common;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class HandlerMeteorTick
implements ITickHandler
{
	private static Random random = new Random();
	private int ticks = 0;
	private int tickGoal = -1;
	private String worldName;
	private HandlerMeteor meteorHandler;
	private long lastTick = 0L;
	
	private boolean wasDay = true;
	
	public HandlerMeteorTick(HandlerMeteor metHand, String wName) {
		this.meteorHandler = metHand;
		this.worldName = wName;
	}

	public void tickStart(EnumSet type, Object... tickData){}

	public void tickEnd(EnumSet type, Object... tickData) {
		World world = (World)tickData[0];
		if (!world.isRemote) {
			if (world.getGameRules().getGameRuleBooleanValue("meteorsFall")) {
				long wTime = world.getTotalWorldTime();
				if (wTime % 20L == 0L && wTime != lastTick && world.getWorldInfo().getWorldName().equalsIgnoreCase(worldName)) {
					lastTick = wTime;
					if (this.tickGoal == -1) {
						this.tickGoal = getNewTickGoal();
					}
					MeteorsMod mod = MeteorsMod.instance;
					meteorHandler.updateMeteors();
					if ((world.getWorldTime() % 24000L >= 12000L || (!mod.meteorsFallOnlyAtNight)) && (meteorHandler.canSpawnNewMeteor())) {
						this.ticks++;
						if (this.ticks >= this.tickGoal) {
							if ((meteorHandler.canSpawnNewMeteor()) && (world.playerEntities.size() > 0)) {
								int x = world.rand.nextInt(mod.meteorFallDistance);
								int z = world.rand.nextInt(mod.meteorFallDistance);
								if (world.rand.nextBoolean()) x = -x;
								if (world.rand.nextBoolean()) z = -z;
								EntityPlayer player = (EntityPlayer) world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
								x = (int)(x + player.posX);
								z = (int)(z + player.posZ);
								ChunkCoordIntPair coords = world.getChunkFromBlockCoords(x, z).getChunkCoordIntPair();
								if (meteorHandler.canSpawnNewMeteorAt(coords)) {
									if (random.nextInt(100) < MeteorsMod.instance.kittyAttackChance) {
										meteorHandler.readyNewMeteor(x, z, HandlerMeteor.getMeteorSize(), 2500, EnumMeteor.KITTY);
									} else {
										meteorHandler.readyNewMeteor(x, z, HandlerMeteor.getMeteorSize(), random.nextInt(mod.RandTicksUntilMeteorCrashes + 1) + mod.MinTicksUntilMeteorCrashes, HandlerMeteor.getMeteorType());
									}
								}

							}

							this.ticks = 0;
							this.tickGoal = getNewTickGoal();
						}
					}
				}
			}
		}
		
	}

	public EnumSet ticks()
	{
		return EnumSet.of(TickType.WORLD);
	}

	public String getLabel()
	{
		return "Meteor";
	}

	private int getNewTickGoal() {
		return random.nextInt(MeteorsMod.instance.RandTicksUntilMeteorSpawn + 1) + MeteorsMod.instance.MinTicksUntilMeteorSpawn;
	}

}