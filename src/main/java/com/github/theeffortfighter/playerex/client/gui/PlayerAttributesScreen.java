package com.github.theeffortfighter.playerex.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.github.theeffortfighter.playerex.api.client.ClientReg;
import com.github.theeffortfighter.playerex.api.client.Page;
import com.github.theeffortfighter.playerex.client.ClientConfig;
import com.github.theeffortfighter.playerex.init.Registry;
import com.github.theeffortfighter.playerex.init.container.PlayerAttributesContainer;
import com.github.theeffortfighter.playerex.init.container.SwitchScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class PlayerAttributesScreen extends AbstractContainerScreen<PlayerAttributesContainer> {
	
	/** Main GUI resources. */
	public static final ResourceLocation GUI = new ResourceLocation(ExAPI.MODID, "textures/gui/gui.png");
	
	/** Tab resources. */
	public static final ResourceLocation TAB = new ResourceLocation(ExAPI.MODID, "textures/gui/tab.png");
	
	private static final int[][] TAB_LOCATIONS = new int[][] {{0, -28}, {32, -28}, {61, -28}, {90, -28}, {119, -28}, {148, -28}, {3, 162}, {32, 162}, {61, 162}, {90, 162}, {119, 162}, {148, 162}};
	
	private List<Page> pages = new ArrayList<Page>();
	private Page activePage;
	
	public PlayerAttributesScreen(final PlayerAttributesContainer par0, final Inventory par1, final Component par2) {
		super(par0, par1, par2);
		
		this.pages.add(0, ClientReg.getPage(DefaultPage.REGISTRY_NAME));
		this.activePage = this.pages.get(0);
		
		Stream<Page> var0 = ClientReg.pageRegistry().stream().filter(var -> var != DefaultPage.REGISTRY_NAME).map(ClientReg::getPage);
		List<Page> var1 = var0.collect(Collectors.toList());
		
		var1.forEach(var -> {
			if(this.pages.size() < 12) {
				this.pages.add(var);
			}
		});
		var0.close();
	}
	
	public Page getPage(int par0) {
		return this.pages.get(par0);
	}
	
	@Override
	public void render(PoseStack par0, int par1, int par2, float par3) {
		this.renderBackground(par0);
		
		super.render(par0, par1, par2, par3);
		
		this.activePage.render(par0, par1, par2, par3);
		this.renderables.forEach(var -> {
			if(var instanceof PageButton) {
				PageButton var1 = (PageButton)var;
				
				if(var1.isHovered(par1, par2)) {
					this.renderTooltip(par0, getPage(var1.additionalData()).getTitle(), par1, par2);
				}
			}
		});
	}
	
	@Override
	protected void renderLabels(PoseStack par0, int par1, int par2) {
		this.activePage.drawGuiContainerForegroundLayer(par0, par1, par2);
	}
	
	@Override
	protected void renderBg(PoseStack par0, float par1, int par2, int par3) {
		int var0 = this.leftPos;
		int var1 = (this.height - this.imageHeight) / 2;
		
		RenderSystem.setShaderTexture(0, GUI);
		this.blit(par0, var0, var1, 0, 0, this.imageWidth, this.imageHeight);
		this.activePage.drawGuiContainerBackgroundLayer(par0, par1, par2, par3);
		this.renderables.forEach(var -> {
			var.render(par0, par2, par3, par1);
		});
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.addRenderableWidget(new TexturedButton(this, ClientConfig.CLIENT.guiButtonX.get(), ClientConfig.CLIENT.guiButtonY.get(), 14, 13, 190, 0, -1, (var0, var1) -> {
			Registry.NETWORK.sendToServer(new SwitchScreens(true));
			InventoryScreen var2 = new InventoryScreen(Minecraft.getInstance().player);
			Minecraft.getInstance().setScreen(var2);
		}));
		
		if(this.pages.size() > 1) {
			for(int var = 0; var < this.pages.size(); var++) {
				this.addRenderableWidget(new PageButton(this, TAB_LOCATIONS[var][0], TAB_LOCATIONS[var][1], 28, 32, (var % 6) * 28, (var < 6 ? 0 : 64), var, (var0, var1) -> {
					this.activePage = getPage(var1);
					this.activePage.init(this.minecraft, this, this.width, this.height);
					this.renderables.forEach(var2 -> {
						if(var2 instanceof PageButton) {
							((PageButton) var2).active = true;
						}
					});
					
					for(Page var2 : this.pages) {
						if(var2 != this.activePage) {
							for(AbstractWidget var3 : var2.getButtonList()) {
								var3.visible = false;
							}
						}
					}
					
					for(AbstractWidget var2 : this.activePage.getButtonList()) {
						var2.visible = true;
					}
				}));
			}
		}
		
		this.activePage.init(this.minecraft, this, this.width, this.height);
		
		for(AbstractWidget var : this.activePage.getButtonList()) {
			this.addRenderableWidget(var);
		}
	}
}
