package com.github.theeffortfighter.playerex.init;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public class ResetCommand {
	
	/** Error procedure. */
	private static boolean error(final CommandContext<CommandSourceStack> par0, final Player par1) {
		if(par1.level.isClientSide) return true;
		
		PlayerList var0 = par0.getSource().getServer().getPlayerList();
		
		if(!var0.getPlayers().contains(par1)) {
			par0.getSource().sendSuccess(new TextComponent(ChatFormatting.RED + par1.getDisplayName().toString() + ChatFormatting.GRAY + " does not exist!"), true);
			
			return true;
		}
		
		return false;
	}
	
	private static int reset(CommandContext<CommandSourceStack> par0, Player par1) throws CommandSyntaxException {
		if(error(par0, par1)) return 1;
		
		EventHandler.reset(par1, true);
		EventHandler.update(par1);
		EventHandler.sync(par1);
		
		return 1;
	}
	
	/**
	 * Register the command(s) to the game.
	 * @param par0
	 */
	public static void register(CommandDispatcher<CommandSourceStack> par0) {
		par0.register(Commands.literal(ExAPI.MODID).requires(var -> var.hasPermission(2)).then(Commands.literal("reset").then(Commands.argument("player", EntityArgument.player()).suggests((var0, var1) -> {
			PlayerList var2 = var0.getSource().getServer().getPlayerList();
			
			return SharedSuggestionProvider.suggest(var2.getPlayers().stream().map(var3 -> var3.getGameProfile().getName()), var1);
		}).executes(var -> {
			return reset(var, EntityArgument.getPlayer(var, "player"));
		}))));
	}
}
