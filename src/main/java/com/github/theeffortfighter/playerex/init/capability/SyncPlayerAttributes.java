package com.github.theeffortfighter.playerex.init.capability;

import com.github.theeffortfighter.playerex.PlayerEx;
import com.github.theeffortfighter.playerex.api.ExAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Network packet responsible for syncing server entity display data to the client.
 */
public class SyncPlayerAttributes {
	private CompoundTag tag;
	private double offset, scale;
	
	public SyncPlayerAttributes() {}
	
	/**
	 * Constructor.
	 * @param par0 Compound tag to send.
	 * @param par1 Offset
	 * @param par2 Scale
	 */
	public SyncPlayerAttributes(final @Nonnull CompoundTag par0, final double par1, final double par2) {
		this.tag = par0;
		this.offset = par1;
		this.scale = par2;
	}
	
	/**
	 * Receives a packet and writes the contents to the input buffer.
	 * @param par0 Input packet.
	 * @param par1 Input buffer
	 */
	public static void encode(SyncPlayerAttributes par0, FriendlyByteBuf par1) {
		par1.writeNbt(par0.tag);
		par1.writeDouble(par0.offset);
		par1.writeDouble(par0.scale);
	}
	
	/**
	 * Receives an input buffer and retrieves the contents to write to a new packet instance.
	 * @param par0 Input buffer.
	 * @return A new Packet instance.
	 */
	public static SyncPlayerAttributes decode(FriendlyByteBuf par0) {
		return new SyncPlayerAttributes(par0.readNbt(), par0.readDouble(), par0.readDouble());
	}
	
	/**
	 * Handles the packet's functionality.
	 * @param par0 Packet input.
	 * @param par1 Network context.
	 */
	public static void handle(SyncPlayerAttributes par0, Supplier<NetworkEvent.Context> par1) {
		if(par1.get().getDirection().getReceptionSide().isClient()) {
			par1.get().enqueueWork(() -> {
				PlayerEx.PROXY.player().ifPresent(var0 -> {
					if(par0.tag != null) {
						ExAPI.playerAttributes(var0).ifPresent(var1 -> ((AttributesCapability)var1).receive(par0.tag, par0.offset, par0.scale));
					}
				});
			});
			
			par1.get().setPacketHandled(true);
		}
	}
}
