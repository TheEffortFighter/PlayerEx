package com.github.theeffortfighter.playerex.init.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Network packet responsible for switching between the player's default inventory and the player elements inventory.
 */
public class SwitchScreens {
	private boolean playerInventory;
	
	public SwitchScreens() {}
	
	/**
	 * Constructor.
	 * @param par0 Set true to switch to the player's default inventory, false to switch to the player's elements inventory.
	 */
	public SwitchScreens(final boolean par0) {
		this.playerInventory = par0;
	}
	
	/**
	 * Receives a packet and writes the contents to the input buffer.
	 * @param par0 Input packet.
	 * @param par1 Input buffer
	 */
	public static void encode(SwitchScreens par0, FriendlyByteBuf par1) {
		par1.writeBoolean(par0.playerInventory);
	}
	
	/**
	 * Receives an input buffer and retrieves the contents to write to a new packet instance.
	 * @param par0 Input buffer.
	 * @return A new Packet instance.
	 */
	public static SwitchScreens decode(FriendlyByteBuf par0) {
		return new SwitchScreens(par0.readBoolean());
	}
	
	/**
	 * Handles the packet's functionality.
	 * @param par0 Packet input.
	 * @param par1 Network context.
	 */
	public static void handle(SwitchScreens par0, Supplier<NetworkEvent.Context> par1) {
		par1.get().enqueueWork(() -> {
			if(par0.playerInventory) {
				ServerPlayer var0 = par1.get().getSender();
				
				if(var0 != null) {
					var0.containerMenu.removed(var0);
					var0.containerMenu = var0.inventoryMenu;
				}
			} else {
				par1.get().getSender().doCloseContainer();
				NetworkHooks.openGui(par1.get().getSender(), new PlayerAttributesContainerProvider());
			}
		});
		
		par1.get().setPacketHandled(true);
	}
}
