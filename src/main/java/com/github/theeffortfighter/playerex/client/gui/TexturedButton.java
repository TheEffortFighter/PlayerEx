package com.github.theeffortfighter.playerex.client.gui;

import java.util.function.BiConsumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.TextComponent;

/**
 * Similar to ImageButton, but with some modifications specific to the needs of this object.
 */
public class TexturedButton extends AbstractButton {
	private AbstractContainerScreen<?> parentScreen;
	private BiConsumer<AbstractContainerScreen<?>, Integer> pressFunction;
	private int textureLat, textureLon, additionalData;
	
	/**
	 * Constructor.
	 * @param par0 ContainerScreen instance.
	 * @param par1 Button's x position.
	 * @param par2 Button's y position.
	 * @param par3 Button's width.
	 * @param par4 Button's height.
	 * @param par5 Button's texture start position (latitude).
	 * @param par6 Button's texture start position (longitude).
	 * @param par7 Additional data passed to the press function.
	 * @param par8 Called when the button is pressed. 
	 * // @param par9 Called when rendering the button.
	 */
	public TexturedButton(final AbstractContainerScreen<?> par0, final int par1, final int par2, int par3, int par4, final int par5, final int par6, final int par7, final BiConsumer<AbstractContainerScreen<?>, Integer> par8) {
		super(par0.getGuiLeft() + par1, par0.getGuiTop() + par2, par3, par4, new TextComponent(""));
		
		this.parentScreen = par0;
		this.textureLat = par5;
		this.textureLon = par6;
		this.additionalData = par7;
		this.pressFunction = par8;
	}
	
	public int getAdditionalData() {
		return this.additionalData;
	}
	
	@Override
	public void onPress() {
		this.pressFunction.accept(this.parentScreen, this.additionalData);
	}
	
	@Override
	public void renderButton(PoseStack par0, int par1, int par2, float par3) {
		Minecraft var0 = Minecraft.getInstance();
		
		RenderSystem.setShaderTexture(0, PlayerAttributesScreen.GUI);
		
		RenderSystem.disableDepthTest();
		
		int var1 = this.textureLon;
		
		if(this.active) {
			if(this.isHovered) {
				var1 += this.height;
			}
		} else {
			var1 += (2 * this.height);
		}
		
		this.blit(par0, this.x, this.y, this.textureLat, var1, this.width, this.height);
		
		RenderSystem.enableDepthTest();
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {

	}
}
