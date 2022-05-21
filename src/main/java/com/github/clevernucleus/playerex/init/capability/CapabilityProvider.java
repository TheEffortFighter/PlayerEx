package com.github.clevernucleus.playerex.init.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.clevernucleus.playerex.api.ExAPI;
import com.github.clevernucleus.playerex.api.attribute.IPlayerAttributes;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityProvider implements ICapabilitySerializable<Tag> {
	private final LazyOptional<IPlayerAttributes> optional;
	private final IPlayerAttributes data;
	
	public CapabilityProvider() {
		this.data = new AttributesCapability();
		this.optional = LazyOptional.of(() -> data);
	}
	
	@Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nullable Capability<T> par0, Direction par1) {
    	return ExAPI.PLAYER_ATTRIBUTES.orEmpty(par0, optional);
    }
    
    @Override
    public Tag serializeNBT() {
    	return ExAPI.PLAYER_ATTRIBUTES.writeNBT(data, null);
    }
    
    @Override
    public void deserializeNBT(Tag par0) {
    	ExAPI.PLAYER_ATTRIBUTES.readNBT(data, null, par0);
    }
}
