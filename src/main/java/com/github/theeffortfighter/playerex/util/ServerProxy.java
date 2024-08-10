package com.github.theeffortfighter.playerex.util;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;

public class ServerProxy implements IProxy {
	
	@Override
	public Optional<Player> player() {
		return Optional.empty();
	}
}
