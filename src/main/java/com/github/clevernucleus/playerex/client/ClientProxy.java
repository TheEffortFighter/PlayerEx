package com.github.clevernucleus.playerex.client;

import java.util.Optional;

import com.github.clevernucleus.playerex.util.IProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;;

public class ClientProxy implements IProxy {
	
	@Override
	public Optional<Player> player() {
		return Optional.ofNullable(Minecraft.getInstance().player);
	}
}
