package com.github.theeffortfighter.playerex.init.container;

import com.github.theeffortfighter.playerex.init.Registry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Container for holding player attributes.
 */
public class PlayerAttributesContainer extends AbstractContainerMenu {
	public PlayerAttributesContainer(final int par0, final Inventory par1) {
		super(Registry.ATTRIBUTES_CONTAINER, par0);
	}
	
	@Override
	public boolean stillValid(final Player par0) {
		return true;
	}
}
