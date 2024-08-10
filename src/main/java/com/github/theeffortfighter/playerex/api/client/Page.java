package com.github.theeffortfighter.playerex.api.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/**
 * Extend this class to create a page object.
 */
public class Page extends Screen {
	public Page(Component par0) {
		super(par0);
	}
	
	/**
	 * @return The protected button list as public.
	 */
	public List<AbstractWidget> getButtonList() {
		List<AbstractWidget> out = new ArrayList<>();
		this.renderables.forEach((a) -> {
			if (a instanceof AbstractWidget){
				out.add((AbstractWidget) a);
			}
		});

		return out;
	}
	
	/**
	 * @return The item to display on the page's tab.
	 */
	public ItemStack displayStack() {
		return ItemStack.EMPTY;
	}
	
	public void drawGuiContainerForegroundLayer(PoseStack par0, int par1, int par2) {}
	
	public void drawGuiContainerBackgroundLayer(PoseStack par0, float par1, int par2, int par3) {}
	
	/**
	 * Allows initialisation of the Page >> Screen with access to the Parent ContainerScreen.
	 * @param par0
	 * @param par1
	 * @param par2
	 * @param par3
	 */
	public void init(Minecraft par0, AbstractContainerScreen<?> par1, int par2, int par3) {
		this.init(par0, par2, par3);
		this.init(par1);
	}
	
	protected void init(AbstractContainerScreen<?> par0) {}
}
