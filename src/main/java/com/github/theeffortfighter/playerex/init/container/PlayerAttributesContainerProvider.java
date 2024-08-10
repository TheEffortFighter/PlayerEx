package com.github.theeffortfighter.playerex.init.container;

import com.github.theeffortfighter.playerex.api.ExAPI;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * Object wrapper used as an identifier and getter for the PlayerAttributesContainer.
 */
public class PlayerAttributesContainerProvider implements MenuProvider {
	
	@Override
	public AbstractContainerMenu createMenu(int par0, Inventory par1, Player par2) {
		return new PlayerAttributesContainer(par0, par1);
	}
	
	@Override
	public Component getDisplayName() {
		return new TranslatableComponent(ExAPI.MODID + ".container");
	}
}
