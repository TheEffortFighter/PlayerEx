package com.github.theeffortfighter.playerex.client.gui;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.github.theeffortfighter.playerex.api.attribute.PlayerAttributes;
import com.github.theeffortfighter.playerex.client.ClientConfig;
import com.github.theeffortfighter.playerex.client.ClientRegistry;
import com.github.theeffortfighter.playerex.init.Registry;
import com.github.theeffortfighter.playerex.init.container.SwitchScreens;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Pair;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {
	public static final Set<IIngameOverlay> UTILS_BAR = new HashSet<>(Arrays.asList(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, ForgeIngameGui.JUMP_BAR_ELEMENT, ForgeIngameGui.ARMOR_LEVEL_ELEMENT, ForgeIngameGui.FOOD_LEVEL_ELEMENT, ForgeIngameGui.AIR_LEVEL_ELEMENT));
	public static final Set<IIngameOverlay> HEALTH_BAR = new HashSet<>(Arrays.asList(ForgeIngameGui.MOUNT_HEALTH_ELEMENT, ForgeIngameGui.PLAYER_HEALTH_ELEMENT));

	private static final BiFunction<String, Float, String> FORMAT = (par0, par1) -> (new DecimalFormat(par0)).format(par1);
	
	private static boolean isRidingJumpable(LocalPlayer par0) {
		Entity var0 = par0.getVehicle();
		
		return par0.isPassenger() && var0 instanceof PlayerRideableJumping;
	}
	
	private static boolean isRiding(LocalPlayer par0) {
		Entity var0 = par0.getVehicle();
		
		return par0.isPassenger() && var0 instanceof ItemSteerable;
	}
	
	private static boolean isFoodItem(ItemStack par0) {
		return par0.getItem().getFoodProperties() != null;
	}
	
	private static boolean isRotten(ItemStack par0) {
		if(!isFoodItem(par0)) return false;
		
		for(Pair<MobEffectInstance, Float> var : par0.getItem().getFoodProperties().getEffects()) {
			if(var.getFirst() != null && var.getFirst().getEffect() != null && var.getFirst().getEffect().getCategory() == MobEffectCategory.HARMFUL) return true;
		}
		
		return false;
	}
	
	private static int healthLat(final Player par0) {
		if(par0.hasEffect(MobEffects.WITHER)) return 213;
		if(par0.hasEffect(MobEffects.POISON)) return 205;
		//if(par0.isPotionActive(Effects.FROSTBITE)) return 221; TODO
		if(par0.hasEffect(MobEffects.ABSORPTION)) return 197;
		
		return 189;
	}
	
	private static void drawHealthBar(PoseStack par0, Minecraft par1, boolean par2) {
		if(par1 == null) return;
		
		LocalPlayer var0 = par1.player;
		
		if(var0 == null) return;
		
		Window var1 = par1.getWindow();
		int varX = var1.getGuiScaledWidth();
		int varY = var1.getGuiScaledHeight();
		
		if(par2 && ClientConfig.CLIENT.enableUtilsBar.get()) {
			int var2 = (int)(78F / var0.getMaxHealth() * var0.getHealth());
			
			RenderSystem.setShaderTexture(0, PlayerAttributesScreen.GUI);
			par1.gui.blit(par0, (varX / 2) - 91, varY - 37, 0, 181, 78, 8);
			par1.gui.blit(par0, (varX / 2) - 91, varY - 37, 0, healthLat(var0), var2, 8);

			return;
		} else if (par2 && !ClientConfig.CLIENT.enableUtilsBar.get()){
			int var2 = (int)(78F / var0.getMaxHealth() * var0.getHealth());

			RenderSystem.setShaderTexture(0, PlayerAttributesScreen.GUI);
			par1.gui.blit(par0, (varX / 2) - 91, varY - 48, 0, 181, 78, 8);
			par1.gui.blit(par0, (varX / 2) - 91, varY - 48, 0, healthLat(var0), var2, 8);

			return;
		}

		if (!ClientConfig.CLIENT.enableUtilsBar.get()) {

			Font var2 = par1.font;
			String var3 = FORMAT.apply("#.##", var0.getHealth() + var0.getAbsorptionAmount()) + "/" + FORMAT.apply("#.##", var0.getMaxHealth());

			int var4 = (varX - var2.width(var3)) / 2;

			//GL11.glPushMatrix();
			// GL11.glScalef(0.8F, 0.8F, 0.8F);
			par0.pushPose();
			par0.scale(0.8F, 0.8F, 0.8F);

			var2.draw(par0, var3, 1.25F * (var4 - 48F), 1.25F * (varY - 47F), 0xFFFFFF);//0x000066); TODO WHEN FROSTY

			//GL11.glPopMatrix();
			par0.popPose();
		} else {
			Font var2 = par1.font;
			String var3 = FORMAT.apply("#.##", var0.getHealth() + var0.getAbsorptionAmount()) + "/" + FORMAT.apply("#.##", var0.getMaxHealth());

			int var4 = (varX - var2.width(var3)) / 2;

			//GL11.glPushMatrix();
			// GL11.glScalef(0.8F, 0.8F, 0.8F);
			par0.pushPose();
			par0.scale(0.8F, 0.8F, 0.8F);

			var2.draw(par0, var3, 1.25F * (var4 - 48), 1.25F * (varY - 36F), 0xFFFFFF);//0x000066); TODO WHEN FROSTY

			//GL11.glPopMatrix();
			par0.popPose();
		}
	}

	private static void drawRidingHealthBar(PoseStack par0, Minecraft par1, boolean par2) {
		if(par1 == null) return;

		LocalPlayer var0 = par1.player;

		if(var0 == null) return;

		Window var1 = par1.getWindow();
		int varX = var1.getGuiScaledWidth();
		int varY = var1.getGuiScaledHeight();

		Entity var2 = var0.getVehicle();

		if(var2 instanceof LivingEntity) {
			LivingEntity var3 = (LivingEntity)var2;

			if(par2) {
				int var4 = (int)(78F / var3.getMaxHealth() * var3.getHealth());

				RenderSystem.setShaderTexture(0, PlayerAttributesScreen.GUI);
				par1.gui.blit(par0, (varX / 2) + 13, varY - 37, 0, 181, 78, 8);
				par1.gui.blit(par0, (varX / 2) + 13, varY - 37, 0, 189, var4, 8);

				return;
			}

			Font var4 = par1.font;
			String var5 = FORMAT.apply("#.##", var3.getHealth() + var3.getAbsorptionAmount()) + "/" + FORMAT.apply("#.##", var3.getMaxHealth());

			int var6 = (varX - var4.width(var5)) / 2;

			// GL11.glPushMatrix();
			// GL11.glScalef(0.8F, 0.8F, 0.8F);
			par0.pushPose();
			par0.scale(0.8F, 0.8F, 0.8F);

			var4.draw(par0, var5, 1.25F * (var6 + 55), 1.25F * (varY - 36F), 0xFFFFFF);

			// GL11.glPopMatrix();
			par0.popPose();
		}
	}

	private static void drawHorseJumpBar(PoseStack par0, Minecraft par1) {
		if(par1 == null) return;

		LocalPlayer var0 = par1.player;

		if(var0 == null) return;

		Window var1 = par1.getWindow();
		int varX = var1.getGuiScaledWidth();
		int varY = var1.getGuiScaledHeight();

		Entity var2 = var0.getVehicle();

		RenderSystem.setShaderTexture(0, PlayerAttributesScreen.GUI);
		par1.gui.blit(par0, (varX / 2) - 91, varY - 27, 0, 175, 182, 3);

		if(var2 instanceof LivingEntity) {
			float var3 = var0.getJumpRidingScale();
			int var4 = (int)(var3 * 183.0F);

			if(var4 > 0) {
				par1.gui.blit(par0, (varX / 2) - 91, varY - 27, 0, 178, var4, 3);
			}
		}
	}

	private static void drawLevelBar(PoseStack par0, Minecraft par1, boolean par2) {
		if(par1 == null) return;

		LocalPlayer var0 = par1.player;

		if(var0 == null) return;

		Window var1 = par1.getWindow();
		int varX = var1.getGuiScaledWidth();
		int varY = var1.getGuiScaledHeight();

		if(par2) {
			RenderSystem.setShaderTexture(0, PlayerAttributesScreen.GUI);
			par1.gui.blit(par0, (varX / 2) - 91, varY - 27, 0, 166, 182, 3);

			ExAPI.playerAttributes(var0).ifPresent(var -> {
				int var2 = 0, var3 = 166;

				if(ClientRegistry.HUD.isDown()) {
					var2 = (int)(182F * var.expCoeff(var0));
					var3 = 169;
				} else {
					int var4 = var0.getXpNeededForNextLevel();

					if(var4 > 0) {
						int var5 = (int)(var0.experienceProgress * 183.0F);

						if(var5 > 0) {
							var2 = var5;
							var3 = 172;
						}
					}
				}

				par1.gui.blit(par0, (varX / 2) - 91, varY - 27, 0, var3, var2, 3);
			});

			return;
		}

		Font var2 = par1.font;
		ExAPI.playerAttributes(var0).ifPresent(var -> {
			int var3 = 0, var4 = 0, var5 = varY - 36;

			if(ClientRegistry.HUD.isDown()) {
				var3 = (int)var.get(var0, PlayerAttributes.LEVEL);
				var4 = 16759296;
			} else {
				var3 = var0.experienceLevel;
				var4 = 8453920;
			}

			if(var3 <= 0) return;

			String var6 = "" + var3;
			int var7 = (varX - var2.width(var6)) / 2;

			var2.draw(par0, var6, (float)(var7 + 1), (float)var5, 0);
			var2.draw(par0, var6, (float)(var7 - 1), (float)var5, 0);
			var2.draw(par0, var6, (float)var7, (float)(var5 + 1), 0);
			var2.draw(par0, var6, (float)var7, (float)(var5 - 1), 0);
			var2.draw(par0, var6, (float)var7, (float)var5, var4);
		});
	}

	private static void drawUtilsBar(PoseStack par0, Minecraft par1, boolean par2) {
		if(par1 == null) return;

		LocalPlayer var0 = par1.player;

		if(var0 == null) return;

		Window var1 = par1.getWindow();
		int varX = var1.getGuiScaledWidth();
		int varY = var1.getGuiScaledHeight();
		int var2 = var0.getFoodData().getFoodLevel();
		int var3 = (int)(100F * Math.max((float)var0.getAirSupply(), 0F) / (float)var0.getMaxAirSupply());
		float var4 = var0.getFoodData().getSaturationLevel();
		boolean var5 = var0.hasEffect(MobEffects.HUNGER);
		boolean var6 = ClientConfig.CLIENT.enableFoodInfo.get().booleanValue();

		ItemStack var7 = var0.getMainHandItem();
		ItemStack var8 = var0.getOffhandItem();

		final int var9 = isFoodItem(var7) ? var7.getItem().getFoodProperties().getNutrition() : (isFoodItem(var8) ? var8.getItem().getFoodProperties().getNutrition() : 0);
		final float var10 = isFoodItem(var7) ? var7.getItem().getFoodProperties().getSaturationModifier() : (isFoodItem(var8) ? var8.getItem().getFoodProperties().getSaturationModifier() : 0F);
		final boolean var11 = isFoodItem(var7) ? isRotten(var7) : (isFoodItem(var8) ? isRotten(var8) : false);

		if(par2) {
			boolean var12 = var5 || (var9 > 0 && var2 < 20 && var11 && var6);

			RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
			par1.gui.blit(par0, (varX / 2) + 12, varY - 38, var12 ? 133 : 16, 27, 9, 9);
			par1.gui.blit(par0, (varX / 2) + 12, varY - 38, var12 ? 88 : 52, 27, 9, 9);
			par1.gui.blit(par0, (varX / 2) + (var3 < 100 ? 44 : 50), varY - 38, 34, 9, 9, 9);

			ExAPI.playerAttributes(var0).ifPresent(var -> {
				int var13 = Math.round((float)var.get(var0, PlayerAttributes.ARMOR));

				if(var3 < 100) {
					par1.gui.blit(par0, (varX / 2) + (var13 < 10 ? 66 : (var13 < 100 ? 70 : 76)), varY - 38, 16, 18, 9, 9);
				}
			});

			RenderSystem.setShaderTexture(0, PlayerAttributesScreen.GUI);

			if(ClientRegistry.HUD.isDown() && var6) {
				par1.gui.blit(par0, (varX / 2) + 12, varY - 38, 215, 0, 9, 9);
			}

			return;
		}

		Font var12 = par1.font;
		ExAPI.playerAttributes(var0).ifPresent(var -> {
			int var13 = Math.round((float)var.get(var0, PlayerAttributes.ARMOR));
			int var14 = Math.min(var2 + var9, 20);
			float var15 = Math.min(var14, var4 + (10F * var10));
			float var16 = Math.min(var14, var4);
			boolean var17 = var9 > 0 && var2 < 20;
			boolean var18 = var10 > 0F && var4 < 20F;

			String var19 = "x" + var13;
			String var20 = (int)(100F * ((ClientRegistry.HUD.isDown() && var6) ? (((var17 && var18) ? var15 : var16) / (float)var14) : (float)((var17 && var6) ? var14 : var2) / 20F)) + "%";
			int var21 = (int)(100F * Math.max((float)var0.getAirSupply(), 0F) / (float)var0.getMaxAirSupply());
			int var22 = (int)((System.currentTimeMillis() / 50L) % 20L);
			int var23 = (int)((float)((255 * Math.sin(Math.toRadians(18 * var22))) + 255F) / 2F);

			// GL11.glPushMatrix();
			// GL11.glScalef(0.8F, 0.8F, 0.8F);
			par0.pushPose();
			par0.scale(0.8F, 0.8F, 0.8F);

			var12.draw(par0, var19, 1.25F * ((varX / 2) + (var21 < 100 ? 54 : 60)), 1.25F * (varY - 36F), 0xFFFFFF);//+54 min left, + 60 max left

			if(var17 && var6) {
				GlStateManager._enableBlend();

				if(var23 > 8) {
					int var24 = 0xFFFFFF;
					int var25 = var23 << 24 & -var24;

					var12.draw(par0, new TextComponent(var20), 1.25F * ((varX / 2) + 22), 1.25F * (varY - 36F), var24 | var25);
				}

				GlStateManager._disableBlend();
			} else {
				var12.draw(par0, var20, 1.25F * ((varX / 2) + 22), 1.25F * (varY - 36F), 0xFFFFFF);
			}

			if(var21 < 100) {
				var12.draw(par0, var21 + "%", 1.25F * ((varX / 2) + (var13 < 10 ? 76 : (var13 < 100 ? 80 : 86))), 1.25F * (varY - 36F), 0xFFFFFF);
			}
			
			// GL11.glPopMatrix();
			par0.popPose();
		});
	}
	
	/**
	 * Event for adding to a gui container.
	 * @param par0
	 */
	@SubscribeEvent
	public static void onGuiInitPost(final ScreenEvent.InitScreenEvent.Post par0) {
		Screen var0 = par0.getScreen();
		
		if(var0 instanceof InventoryScreen) {
			AbstractContainerScreen<?> var1 = (AbstractContainerScreen<?>)var0;
			if(par0.getListenerList() != null) {
				par0.addListener(new TexturedButton(var1, ClientConfig.CLIENT.guiButtonX.get(), ClientConfig.CLIENT.guiButtonY.get(), 14, 13, 176, 0, 0, (var2, var3) -> {
					if(var2 instanceof InventoryScreen) {
						Registry.NETWORK.sendToServer(new SwitchScreens(false));
					}
				}));
			}
		}
	}
	
	/**
	 * Event for adding ingame HUD elements (pre-render).
	 * @param par0
	 */
	@SubscribeEvent
	public static void onHUDRenderPre(final net.minecraftforge.client.event.RenderGameOverlayEvent.Pre par0) {
		if(!ClientConfig.CLIENT.enableHUD.get()) return;
		
		ElementType var0 = par0.getType();
		PoseStack var1 = par0.getMatrixStack();
		LocalPlayer var2 = Minecraft.getInstance().player;
		
		// if(var0 == ElementType.HOTBAR) return;
		if(var2.isCreative() || var2.isSpectator()) return;

		if(isRidingJumpable(var2) && ClientConfig.CLIENT.enableUtilsBar.get()) {
			drawHorseJumpBar(var1, Minecraft.getInstance());
		} else if (ClientConfig.CLIENT.enableUtilsBar.get()){
			if(!isRiding(var2)) {
				drawUtilsBar(var1, Minecraft.getInstance(), true);
			}

			drawLevelBar(var1, Minecraft.getInstance(), true);
		}
		
		if(ClientConfig.CLIENT.enableHealthBar.get()) {
			drawHealthBar(var1, Minecraft.getInstance(), true);
			
			if(isRiding(var2) || isRidingJumpable(var2)) {//var2.isRidingHorse()
				drawRidingHealthBar(var1, Minecraft.getInstance(), true);
			}
		}

		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
	}
	
	/**
	 * Event for adding ingame HUD elements (post-render).
	 * @param par0
	 */
	@SubscribeEvent
	public static void onHUDRenderPost(final net.minecraftforge.client.event.RenderGameOverlayEvent.Post par0) {
		if(!ClientConfig.CLIENT.enableHUD.get()) return;
		
		PoseStack var0 = par0.getMatrixStack();
		LocalPlayer var1 = Minecraft.getInstance().player;
		
		// if(par0.getType() == ElementType.HOTBAR) return;
		if(var1.isCreative() || var1.isSpectator()) return;

		if(!isRidingJumpable(var1) && ClientConfig.CLIENT.enableUtilsBar.get()) {//!var1.isRidingHorse()
			if(!isRiding(var1)) {
				drawUtilsBar(var0, Minecraft.getInstance(), false);
			}

			drawLevelBar(var0, Minecraft.getInstance(), false);
		}
		
		if(ClientConfig.CLIENT.enableHealthBar.get()) {
			drawHealthBar(var0, Minecraft.getInstance(), false);
			
			if(isRiding(var1) || isRidingJumpable(var1)) {//var1.isRidingHorse()
				drawRidingHealthBar(var0, Minecraft.getInstance(), false);
			}
		}

	}
}
