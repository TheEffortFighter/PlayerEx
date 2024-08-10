package com.github.theeffortfighter.playerex.init;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.github.theeffortfighter.playerex.api.Util;
import com.github.theeffortfighter.playerex.api.attribute.IPlayerAttribute;
import com.github.theeffortfighter.playerex.api.attribute.IPlayerAttributes;
import com.github.theeffortfighter.playerex.api.attribute.PlayerAttributes;
import com.github.theeffortfighter.playerex.init.capability.AddPlayerAttributes;
import com.github.theeffortfighter.playerex.init.capability.SyncPlayerAttributes;
import com.github.theeffortfighter.playerex.init.container.PlayerAttributesContainer;
import com.github.theeffortfighter.playerex.init.container.SwitchScreens;
import com.github.theeffortfighter.playerex.util.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = ExAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registry {
	
	/** Network instance. */
	public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(ExAPI.MODID, "path"), () -> "1", "1"::equals, "1"::equals);
	
	/** Static identifier for the player elements container type. */
	public static MenuType<PlayerAttributesContainer> ATTRIBUTES_CONTAINER;

	@SubscribeEvent
	public static void capSetup(final RegisterCapabilitiesEvent event) {
		event.register(IPlayerAttributes.class);
	}
	
	/**
	 * Mod initialisation event.
	 * @param par0
	 */
	@SubscribeEvent
	public static void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent par0) {
		NETWORK.registerMessage(0, SwitchScreens.class, SwitchScreens::encode, SwitchScreens::decode, SwitchScreens::handle);
		NETWORK.registerMessage(1, SyncPlayerAttributes.class, SyncPlayerAttributes::encode, SyncPlayerAttributes::decode, SyncPlayerAttributes::handle);
		NETWORK.registerMessage(2, AddPlayerAttributes.class, AddPlayerAttributes::encode, AddPlayerAttributes::decode, AddPlayerAttributes::handle);
		
		par0.enqueueWork(() -> {
			PlayerAttributes.registerAdder(PlayerAttributes.LEVEL.registryName(), (var0, var1, var2) -> {
				var1.add(var0, PlayerAttributes.SKILLPOINTS, var2);
			});
			PlayerAttributes.registerAdder(PlayerAttributes.CONSTITUTION.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.maxHealth.get()) {var1.add(var0, PlayerAttributes.MAX_HEALTH, var2);}
				if(CommonConfig.COMMON.knockbackRes.get()) {Util.add(var1, var0, PlayerAttributes.KNOCKBACK_RESISTANCE, var2 * 0.01D, 1.0D);}
			});
			PlayerAttributes.registerAdder(PlayerAttributes.STRENGTH.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.healthRegen.get()) {var1.add(var0, PlayerAttributes.HEALTH_REGEN, var2 * 0.0005D);}
				if(CommonConfig.COMMON.meleeDamage.get()) {var1.add(var0, PlayerAttributes.MELEE_DAMAGE, var2 * 0.25D);}
				if(CommonConfig.COMMON.armor.get()) {Util.add(var1, var0, PlayerAttributes.ARMOR, var2 * 0.25D, 100D);}
			});
			PlayerAttributes.registerAdder(PlayerAttributes.DEXTERITY.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.rangedDamage.get()) {var1.add(var0, PlayerAttributes.RANGED_DAMAGE, var2 * 0.25D);}
				if(CommonConfig.COMMON.attackSpeed.get()) {Util.add(var1, var0, PlayerAttributes.ATTACK_SPEED, var2 * 0.1D, 8.0D);}
				if(CommonConfig.COMMON.movementSpeed.get()) {Util.add(var1, var0, PlayerAttributes.MOVEMENT_SPEED, var2 * 0.004D, 1.0D);}
				if(CommonConfig.COMMON.meleeCritDamage.get()) {Util.add(var1, var0, PlayerAttributes.MELEE_CRIT_DAMAGE, var2 * 0.05D, 10.0D);}
			});
			PlayerAttributes.registerAdder(PlayerAttributes.INTELLIGENCE.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.healthRegenAmp.get()) {Util.add(var1, var0, PlayerAttributes.HEALTH_REGEN_AMP, var2 * 0.01D, 10.0D);}
				if(CommonConfig.COMMON.rangedCritDamage.get()) {Util.add(var1, var0, PlayerAttributes.RANGED_CRIT_DAMAGE, var2 * 0.05D, 10.0D);}
				if(CommonConfig.COMMON.lifesteal.get()) {Util.add(var1, var0, PlayerAttributes.LIFESTEAL, var2 * 0.02D, 10.0D);}
			});
			PlayerAttributes.registerAdder(PlayerAttributes.LUCKINESS.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.luck.get()) {var1.add(var0, PlayerAttributes.LUCK, var2);}
				if(CommonConfig.COMMON.meleeCritChance.get()) {Util.add(var1, var0, PlayerAttributes.MELEE_CRIT_CHANCE, var2 * 0.02D, 1.0D);}
				if(CommonConfig.COMMON.rangedCritChance.get()) {Util.add(var1, var0, PlayerAttributes.RANGED_CRIT_CHANCE, var2 * 0.02D, 1.0D);}
				if(CommonConfig.COMMON.evasion.get()) {Util.add(var1, var0, PlayerAttributes.EVASION, var2 * 0.02D, 1.0D);}
			});
			PlayerAttributes.registerAdder(PlayerAttributes.MAX_HEALTH.registryName(), (var0, var1, var2) -> {
				if(var0.getHealth() > var0.getMaxHealth()) {
					var0.setHealth(var0.getMaxHealth());
				}
			});
			PlayerAttributes.registerModifier(PlayerAttributes.CONSTITUTION.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.maxHealth.get()) {var1.apply(var0, PlayerAttributes.MAX_HEALTH, var2);}
				if(CommonConfig.COMMON.knockbackRes.get()) {Util.apply(var1, var0, PlayerAttributes.KNOCKBACK_RESISTANCE, var2, 0.01D, 1.0D);}
			});
			PlayerAttributes.registerModifier(PlayerAttributes.STRENGTH.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.healthRegen.get()) {Util.apply(var1, var0, PlayerAttributes.HEALTH_REGEN, var2, 0.0005D);}
				if(CommonConfig.COMMON.meleeDamage.get()) {Util.apply(var1, var0, PlayerAttributes.MELEE_DAMAGE, var2, 0.25D);}
				if(CommonConfig.COMMON.armor.get()) {Util.apply(var1, var0, PlayerAttributes.ARMOR, var2, 0.25D, 100D);}
			});
			PlayerAttributes.registerModifier(PlayerAttributes.DEXTERITY.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.rangedDamage.get()) {Util.apply(var1, var0, PlayerAttributes.RANGED_DAMAGE, var2, 0.25D);}
				if(CommonConfig.COMMON.attackSpeed.get()) {Util.apply(var1, var0, PlayerAttributes.ATTACK_SPEED, var2, 0.1D, 8.0D);}
				if(CommonConfig.COMMON.movementSpeed.get()) {Util.apply(var1, var0, PlayerAttributes.MOVEMENT_SPEED, var2, 0.004D, 1.0D);}
				if(CommonConfig.COMMON.meleeCritDamage.get()) {Util.apply(var1, var0, PlayerAttributes.MELEE_CRIT_DAMAGE, var2, 0.05D, 10.0D);}
			});
			PlayerAttributes.registerModifier(PlayerAttributes.INTELLIGENCE.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.healthRegenAmp.get()) {Util.apply(var1, var0, PlayerAttributes.HEALTH_REGEN_AMP, var2, 0.01D, 10.0D);}
				if(CommonConfig.COMMON.rangedCritDamage.get()) {Util.apply(var1, var0, PlayerAttributes.RANGED_CRIT_DAMAGE, var2, 0.05D, 10.0D);}
				if(CommonConfig.COMMON.lifesteal.get()) {Util.apply(var1, var0, PlayerAttributes.LIFESTEAL, var2, 0.02D, 10.0D);}
			});
			PlayerAttributes.registerModifier(PlayerAttributes.LUCKINESS.registryName(), (var0, var1, var2) -> {
				if(CommonConfig.COMMON.luck.get()) {var1.apply(var0, PlayerAttributes.LUCK, var2);}
				if(CommonConfig.COMMON.meleeCritChance.get()) {Util.apply(var1, var0, PlayerAttributes.MELEE_CRIT_CHANCE, var2, 0.02D, 1.0D);}
				if(CommonConfig.COMMON.rangedCritChance.get()) {Util.apply(var1, var0, PlayerAttributes.RANGED_CRIT_CHANCE, var2, 0.02D, 1.0D);}
				if(CommonConfig.COMMON.evasion.get()) {Util.apply(var1, var0, PlayerAttributes.EVASION, var2, 0.02D, 1.0D);}
			});
			PlayerAttributes.registerModifier(PlayerAttributes.MAX_HEALTH.registryName(), (var0, var1, var2) -> {
				if(var0.getHealth() > var0.getMaxHealth()) {
					var0.setHealth(var0.getMaxHealth());
				}
			});
		});
	}
	
	/**
	 * Attributes register event.
	 * @param par0
	 */
	@SubscribeEvent
	public static void registerAttributes(final net.minecraftforge.event.RegistryEvent.Register<Attribute> par0) {
		for(IPlayerAttribute var : PlayerAttributes.attributes()) {
			if(var.type() == IPlayerAttribute.Type.ALL || var.type() == IPlayerAttribute.Type.DATA) {
				par0.getRegistry().register(var.get());
			}
		}
	}
	
	/**
	 * Event handling the registration of container types.
	 * @param par0
	 */
	@SubscribeEvent
	public static void registerContainerTypes(final net.minecraftforge.event.RegistryEvent.Register<MenuType<?>> par0) {
		ATTRIBUTES_CONTAINER = new MenuType<>(PlayerAttributesContainer::new);
		ATTRIBUTES_CONTAINER.setRegistryName(new ResourceLocation(ExAPI.MODID, "attributes"));
		par0.getRegistry().register(ATTRIBUTES_CONTAINER);
	}
}
