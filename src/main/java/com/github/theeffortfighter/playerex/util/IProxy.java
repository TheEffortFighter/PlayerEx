package com.github.theeffortfighter.playerex.util;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;

public interface IProxy {
	
	/**
	 * @return An optional with empty conditional being a server player and a result being the client player.
	 */
	Optional<Player> player();
}
