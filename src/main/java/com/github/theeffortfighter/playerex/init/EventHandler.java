package com.github.theeffortfighter.playerex.init;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.github.theeffortfighter.playerex.api.attribute.IPlayerAttribute;
import com.github.theeffortfighter.playerex.api.attribute.IPlayerAttributes;
import com.github.theeffortfighter.playerex.api.attribute.PlayerAttributes;
import com.github.theeffortfighter.playerex.init.capability.AttributesCapability;
import com.github.theeffortfighter.playerex.init.capability.CapabilityProvider;
import com.github.theeffortfighter.playerex.util.CommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = ExAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {
	
	/**
	 * Initialises the config to the game.
	 * @param par0
	 */
	private static void init(final Player par0) {
		if(par0 == null) return;
		if(par0.level.isClientSide) return;
		
		ExAPI.playerAttributes(par0).ifPresent(var -> {
			CompoundTag var0 = var.write();
			
			if(!var0.getBoolean("Initialised")) {
				var.add(par0, PlayerAttributes.CONSTITUTION, CommonConfig.COMMON.constitution.get().doubleValue());
				var.add(par0, PlayerAttributes.STRENGTH, CommonConfig.COMMON.strength.get().doubleValue());
				var.add(par0, PlayerAttributes.DEXTERITY, CommonConfig.COMMON.dexterity.get().doubleValue());
				var.add(par0, PlayerAttributes.INTELLIGENCE, CommonConfig.COMMON.intelligence.get().doubleValue());
				var.add(par0, PlayerAttributes.LUCKINESS, CommonConfig.COMMON.luckiness.get().doubleValue());
				var.add(par0, PlayerAttributes.MAX_HEALTH, (CommonConfig.COMMON.maxHealth.get() ? -20D : CommonConfig.COMMON.constitution.get().doubleValue() - 20D));
				var0.putBoolean("Initialised", true);
			}
		});
	}
	
	/** Bit of a temporary thing; we'll rewrite this nicely when we rework the system for 1.17 (or pre-1.17)*/
	public static void reset(final Player par0, final boolean par1) {
		if(par0 == null) return;
		if(par0.level.isClientSide) return;
		
		ExAPI.playerAttributes(par0).ifPresent(var -> {
			if(par1) {
				for(IPlayerAttribute v : PlayerAttributes.attributes()) {
					var.forceSet(par0, v, 0);
				}
			}
			
			var.add(par0, PlayerAttributes.CONSTITUTION, CommonConfig.COMMON.constitution.get().doubleValue());
			var.add(par0, PlayerAttributes.STRENGTH, CommonConfig.COMMON.strength.get().doubleValue());
			var.add(par0, PlayerAttributes.DEXTERITY, CommonConfig.COMMON.dexterity.get().doubleValue());
			var.add(par0, PlayerAttributes.INTELLIGENCE, CommonConfig.COMMON.intelligence.get().doubleValue());
			var.add(par0, PlayerAttributes.LUCKINESS, CommonConfig.COMMON.luckiness.get().doubleValue());
			var.add(par0, PlayerAttributes.MAX_HEALTH, (CommonConfig.COMMON.maxHealth.get() ? -20D : CommonConfig.COMMON.constitution.get().doubleValue() - 20D));
		});
	}
	
	/**
	 * Updates the values of the player's attributes.
	 * @param par0 Player instance.
	 */
	public static void update(Player par0) {
		if(par0 == null) return;
		if(par0.level.isClientSide) return;
		
		ExAPI.playerAttributes(par0).ifPresent(var -> ((AttributesCapability)var).update(par0));
	}
	
	/**
	 * Sends packets from the server to the client to sync the client with the server.
	 * @param par0 Player instance.
	 */
	public static void sync(Player par0) {
		if(par0 == null) return;
		if(par0.level.isClientSide) return;
		
		ExAPI.playerAttributes(par0).ifPresent(var -> ((AttributesCapability)var).send(par0));
	}
	
	@SubscribeEvent
    public static void serverLoad(final ServerStartingEvent par0) {
        ResetCommand.register(par0.getServer().getCommands().getDispatcher());
        LevelCommand.register(par0.getServer().getCommands().getDispatcher());
    }
	
	/**
	 * Event for attaching capabilities.
	 * @param par0
	 */
	@SubscribeEvent
    public static void onCapabilityAttachEntity(final net.minecraftforge.event.AttachCapabilitiesEvent<Entity> par0) {
		if(par0.getObject() instanceof Player) {
			par0.addCapability(new ResourceLocation(ExAPI.MODID, "playerattributes"), new CapabilityProvider());
			System.out.println("attach to " + par0.getObject().getStringUUID() + " " + par0.getObject().level.isClientSide());
		}
	}

	// for some reason the capability doesnt seem to be present on the PlayerEvent.Clone, im sure im just being dumb somehow but maybe its about isAlive==false
	private static LazyOptional<IPlayerAttributes> lastDiedPlayerAttributes = null;

	@SubscribeEvent
	public static void onPlayerEntityCloned(LivingDeathEvent event) {
		if (event.getEntityLiving() instanceof Player && !event.getEntityLiving().level.isClientSide()){
			lastDiedPlayerAttributes = ExAPI.playerAttributes((Player) event.getEntityLiving());
		}
	}

	@SubscribeEvent
    public static void onPlayerEntityCloned(final net.minecraftforge.event.entity.player.PlayerEvent.Clone par0) {
		Player newPlayer = par0.getPlayer();
		Player oldPlayer = par0.getOriginal();

		LazyOptional<IPlayerAttributes> oldAttributes = ExAPI.playerAttributes(oldPlayer).isPresent() ? ExAPI.playerAttributes(oldPlayer) : lastDiedPlayerAttributes;

		if(newPlayer.level.isClientSide) return;
		if(par0.isWasDeath() && CommonConfig.COMMON.resetOnDeath.get()) {
			reset(newPlayer, false);
			update(newPlayer);
			sync(newPlayer);

			return;
		}
		
		try {
			ExAPI.playerAttributes(newPlayer).ifPresent(par1 -> {
				oldAttributes.ifPresent(par2 -> {
					par1.read(par2.write());
				});
			});
		} catch(Exception parE) {}

		update(newPlayer);
		sync(newPlayer);

		if(par0.isWasDeath()) {
			newPlayer.heal(newPlayer.getMaxHealth());
		}
	}
	
	/**
	 * Event firing when a player changes dimensions.
	 * @param par0
	 */
	@SubscribeEvent
    public static void onPlayerChangedDimension(final net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent par0) {
		update(par0.getPlayer());
		sync(par0.getPlayer());
	}
	
	/**
	 * Event firing when the player respawns.
	 * @param par0
	 */
	@SubscribeEvent
	public static void onPlayerRespawn(final net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent par0) {
		update(par0.getPlayer());
		sync(par0.getPlayer());
	}
	
	/**
	 * Event firing when a player logs in.
	 * @param par0
	 */
	@SubscribeEvent
	public static void onPlayerLoggedIn(final net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent par0) {
		Player var0 = par0.getPlayer();
		
		init(var0);
		update(var0);
		sync(var0);
		
		if(var0.level.isClientSide) return;
		
		ExAPI.playerAttributes(var0).ifPresent(var -> {
			CompoundTag var1 = var.write();
			
			if(var1.contains("CurrentHealth")) {
				var0.setHealth(var1.getFloat("CurrentHealth"));
			}
		});
	}
	
	/**
	 * Event firing when a player logs out.
	 * @param par0
	 */
	@SubscribeEvent
	public static void onPlayerLoggedOut(final net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent par0) {
		Player var0 = par0.getPlayer();
		
		if(var0.level.isClientSide) return;
		
		ExAPI.playerAttributes(var0).ifPresent(var -> {
			CompoundTag var1 = var.write();
			
			var1.putFloat("CurrentHealth", var0.getHealth());
			var.read(var1);
		});
	}
	
	/**
	 * Equipment changed event; fired when an equipment slot is changed.
	 * @param par0
	 */
	@SubscribeEvent
	public static void onPlayerEquippedItems(final net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent par0) {
		if(!(par0.getEntityLiving() instanceof Player)) return;
		
		Player var0 = (Player)par0.getEntityLiving();
		ExAPI.playerAttributes(var0).ifPresent(var -> ((AttributesCapability)var).putEquipment(par0.getSlot(), par0.getFrom()));
	}
	
	/**
	 * Event firing every tick for the player.
	 * @param par0
	 */
	@SubscribeEvent
	public static void onPlayerTick(final net.minecraftforge.event.TickEvent.PlayerTickEvent par0) {
		Player var0 = par0.player;
		
		if(var0.level.isClientSide) return;
		
		ExAPI.playerAttributes(var0).ifPresent(var -> {
			var0.heal((float)var.get(var0, PlayerAttributes.HEALTH_REGEN));
			
			AttributesCapability var1 = (AttributesCapability)var;
			
			for(EquipmentSlot var2 : EquipmentSlot.values()) {
				ItemStack var3 = var0.getItemBySlot(var2);
				ItemStack var4 = var1.getEquipment(var2);
				
				if(!ItemStack.matches(var3, var4)) {
					var1.putEquipment(var2, var3);
					sync(var0);
				}
			}
		});
	}
	
	/**
	 * Event fired when xp is added.
	 * @param par0
	 */
	@SubscribeEvent
	public static void onExperienceProcessed(final net.minecraftforge.event.entity.player.PlayerXpEvent.XpChange par0) {
		Player var0 = par0.getPlayer();
		
		if(var0.level.isClientSide) return;
		
		int var1 = par0.getAmount();
		int var2 = Math.round((float)var1 * CommonConfig.COMMON.experienceSplit.get().floatValue() / 100F);
		int var3 = CommonConfig.COMMON.xpGetsSplit.get() ? Math.round((float)var1 * (100F - CommonConfig.COMMON.experienceSplit.get().floatValue()) / 100F) : par0.getAmount();
		
		ExAPI.playerAttributes(var0).ifPresent(var -> {
			var.add(var0, PlayerAttributes.EXPERIENCE, var2);
			
			float var4 = (float)var.expCoeff(var0);
			
			if(var4 > 0.95F) {
				var.add(var0, PlayerAttributes.LEVEL, 1);
				var.forceSet(var0, PlayerAttributes.EXPERIENCE, 0F);
			}
		});
		
		par0.setAmount(var3);
	}
	
	/**
	 * Event fired when an entity is healed.
	 * @param par0
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onHeal(final net.minecraftforge.event.entity.living.LivingHealEvent par0) {
		if(par0.getEntityLiving() instanceof Player) {
			Player var0 = (Player)par0.getEntityLiving();
			
			if(var0.level.isClientSide) return;
			
			ExAPI.playerAttributes(var0).ifPresent(var -> {
				par0.setAmount(par0.getAmount() * (1F + (float)var.get(var0, PlayerAttributes.HEALTH_REGEN_AMP)));
			});
		}
	}
	
	/**
	 * Event fired when a crit may or may not happen.
	 * @param par0
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onCrit(final net.minecraftforge.event.entity.player.CriticalHitEvent par0) {
		Player var0 = par0.getPlayer();
		Random var1 = new Random();
		
		if(var0.level.isClientSide) return;
		
		ExAPI.playerAttributes(var0).ifPresent(var -> {
			par0.setDamageModifier(1F + (float)var.get(var0, PlayerAttributes.MELEE_CRIT_DAMAGE));
			
			if(var1.nextInt(100) < (int)(100F * (float)var.get(var0, PlayerAttributes.MELEE_CRIT_CHANCE))) {
				par0.setResult(Result.ALLOW);
			} else {
				par0.setResult(Result.DENY);
			}
		});
	}
	
	/**
	 * Event fired when a living entity is hurt.
	 * @param par0
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingHurt(final net.minecraftforge.event.entity.living.LivingHurtEvent par0) {
		if(par0.getEntityLiving() instanceof Player) {
			Player var0 = (Player)par0.getEntityLiving();
			Random var1 = new Random();
			
			if(var0.level.isClientSide) return;
			
			ExAPI.playerAttributes(var0).ifPresent(var -> {
				if(par0.getSource().equals(DamageSource.IN_FIRE) || par0.getSource().equals(DamageSource.ON_FIRE) || par0.getSource().equals(DamageSource.HOT_FLOOR)) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.FIRE_RESISTANCE)));
				}
				
				if(par0.getSource().equals(DamageSource.LAVA)) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.LAVA_RESISTANCE)));
				}
				
				if(par0.getSource().isExplosion()) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.EXPLOSION_RESISTANCE)));
				}
				
				if(par0.getSource().isMagic()) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.POISON_RESISTANCE)));
				}
				
				if(par0.getSource().equals(DamageSource.WITHER)) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.WITHER_RESISTANCE)));
				}
				
				if(par0.getSource().equals(DamageSource.DROWN)) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.DROWNING_RESISTANCE)));
				}
				
				if(par0.getSource().equals(DamageSource.FALL)) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.FALLING_RESISTANCE)));
				}
				
				if(par0.getSource().isBypassArmor()) {
					par0.setAmount(par0.getAmount() * (1F - (float)var.get(var0, PlayerAttributes.DAMAGE_REDUCTION)));
				}
				
				if(par0.getSource().isProjectile()) {
					if(var1.nextInt(100) < (int)(100D * (float)var.get(var0, PlayerAttributes.EVASION))) {
						par0.setCanceled(true);
					}
				}
			});
		}
		
		if(par0.getSource().getEntity() instanceof Player) {
			Player var0 = (Player)par0.getSource().getEntity();
			
			ExAPI.playerAttributes(var0).ifPresent(var -> {
				var0.heal(par0.getAmount() * (float)var.get(var0, PlayerAttributes.LIFESTEAL));
			});
		}
	}
	
	/**
	 * Event fired when a living entity takes an arrow up the ***.
	 * @param par0
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onProjectileImpact(final net.minecraftforge.event.entity.ProjectileImpactEvent par0) {
		if(par0.getProjectile().getOwner() instanceof Player && par0.getProjectile() instanceof AbstractArrow) {
			Player var0 = (Player)par0.getProjectile().getOwner();
			Random var1 = new Random();
			
			ExAPI.playerAttributes(var0).ifPresent(var -> {
				boolean var2 = var1.nextInt(100) > (int)(100F * var.get(var0, PlayerAttributes.RANGED_CRIT_CHANCE));
				double var3 = ((AbstractArrow) par0.getProjectile()).getBaseDamage() + var.get(var0, PlayerAttributes.RANGED_DAMAGE);
				
				((AbstractArrow) par0.getProjectile()).setCritArrow(var2);
				((AbstractArrow) par0.getProjectile()).setBaseDamage(var2 ? (var3 * (1D + var.get(var0, PlayerAttributes.RANGED_CRIT_DAMAGE))) : var3);
			});
		}
	}
}
