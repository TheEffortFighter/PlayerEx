package com.github.theeffortfighter.playerex.init.capability;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.github.theeffortfighter.playerex.api.attribute.IPlayerAttribute;
import com.github.theeffortfighter.playerex.api.attribute.PlayerAttributes;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;

public class AddPlayerAttributes {
	private CompoundTag tag;
	
	public AddPlayerAttributes() {}
	
	
	@SafeVarargs
	public AddPlayerAttributes(Pair<IPlayerAttribute, Float> ... par0) {
		this.tag = new CompoundTag();
		ListTag var0 = new ListTag();
		
		for(Pair<IPlayerAttribute, Float> var : par0) {
			CompoundTag var1 = new CompoundTag();
			String var2 = var.getFirst().toString();
			float var3 = var.getSecond().floatValue();
			
			var1.putString("Name", var2);
			var1.putFloat("Value", var3);
			var0.add(var1);
		}
		
		this.tag.put("Attributes", var0);
	}
	
	/**
	 * Constructor.
	 * @param par0 Compound tag to send.
	 */
	public AddPlayerAttributes(final @Nonnull CompoundTag par0) {
		this.tag = par0;
	}
	
	/**
	 * Receives a packet and writes the contents to the input buffer.
	 * @param par0 Input packet.
	 * @param par1 Input buffer
	 */
	public static void encode(AddPlayerAttributes par0, FriendlyByteBuf par1) {
		par1.writeNbt(par0.tag);
	}
	
	/**
	 * Receives an input buffer and retrieves the contents to write to a new packet instance.
	 * @param par0 Input buffer.
	 * @return A new Packet instance.
	 */
	public static AddPlayerAttributes decode(FriendlyByteBuf par0) {
		return new AddPlayerAttributes(par0.readNbt());
	}
	
	/**
	 * Handles the packet's functionality.
	 * @param par0 Packet input.
	 * @param par1 Network context.
	 */
	public static void handle(AddPlayerAttributes par0, Supplier<NetworkEvent.Context> par1) {
		par1.get().enqueueWork(() -> {
			ServerPlayer var0 = par1.get().getSender();
			
			if(var0 != null && par0.tag != null) {
				ListTag var1 = par0.tag.getList("Attributes", 10);
				Map<IPlayerAttribute, Float> var2 = Maps.newHashMap();
				
				for(Tag var : var1) {
					CompoundTag var3 = (CompoundTag)var;
					IPlayerAttribute var4 = PlayerAttributes.fromRegistryName(var3.getString("Name"));
					
					var2.put(var4, var3.getFloat("Value"));
				}
				
				ExAPI.playerAttributes(var0).ifPresent(var -> var2.forEach((var3, var4) -> var.add(var0, var3, var4.doubleValue())));
			}
		});
		
		par1.get().setPacketHandled(true);
	}
}
