package com.github.theeffortfighter.playerex.client;

import com.github.theeffortfighter.playerex.client.gui.ClientEventHandler;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.lwjgl.glfw.GLFW;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.github.theeffortfighter.playerex.api.client.ClientReg;
import com.github.theeffortfighter.playerex.client.gui.DefaultPage;
import com.github.theeffortfighter.playerex.client.gui.PlayerAttributesScreen;
import com.github.theeffortfighter.playerex.client.gui.ResistancePage;
import com.github.theeffortfighter.playerex.init.Registry;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistry {
	public static final KeyMapping HUD = new KeyMapping("key." + ExAPI.MODID + ".hud", KeyConflictContext.IN_GAME, key(GLFW.GLFW_KEY_LEFT_ALT), "PlayerEx");
	
	private static InputConstants.Key key(int par0) {
		return InputConstants.Type.KEYSYM.getOrCreate(par0);
	}
	
	/**
	 * Mod client initialisation event.
	 * @param par0
	 */
	@SubscribeEvent
	public static void clientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent par0) {
		MenuScreens.register(Registry.ATTRIBUTES_CONTAINER, PlayerAttributesScreen::new);
		net.minecraftforge.client.ClientRegistry.registerKeyBinding(HUD);
		
		par0.enqueueWork(() -> {
			ClientReg.registerPage(DefaultPage.REGISTRY_NAME, new DefaultPage());
			ClientReg.registerPage(ResistancePage.REGISTRY_NAME, new ResistancePage());
		});

		updateHudState();
	}

	public static void updateHudState(){
		ClientEventHandler.HEALTH_BAR.forEach((element) -> OverlayRegistry.enableOverlay(element, !ClientConfig.CLIENT.enableHealthBar.get()));
		ClientEventHandler.UTILS_BAR.forEach((element) -> OverlayRegistry.enableOverlay(element, !ClientConfig.CLIENT.enableHUD.get()));
		ClientEventHandler.UTILS_BAR.forEach((element) -> OverlayRegistry.enableOverlay(element, !ClientConfig.CLIENT.enableUtilsBar.get()));
	}
}
