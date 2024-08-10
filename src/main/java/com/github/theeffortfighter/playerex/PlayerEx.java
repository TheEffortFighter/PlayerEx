package com.github.theeffortfighter.playerex;

import com.github.theeffortfighter.playerex.api.ExAPI;
import com.github.theeffortfighter.playerex.client.ClientConfig;
import com.github.theeffortfighter.playerex.util.CommonConfig;
import com.github.theeffortfighter.playerex.util.IProxy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;

@Mod(ExAPI.MODID)
public class PlayerEx {
	
	/** Proxy instance to get side specific methods. */
	public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> com.github.theeffortfighter.playerex.client.ClientProxy::new, () -> com.github.theeffortfighter.playerex.util.ServerProxy::new);
	
	public PlayerEx() {
		ModLoadingContext.get().registerConfig(Type.COMMON, CommonConfig.COMMON_SPEC);
		ModLoadingContext.get().registerConfig(Type.CLIENT, ClientConfig.CLIENT_SPEC);
	}
}
