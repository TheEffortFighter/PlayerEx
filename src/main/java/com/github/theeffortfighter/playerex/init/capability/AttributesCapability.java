package com.github.theeffortfighter.playerex.init.capability;

import com.github.theeffortfighter.playerex.api.attribute.IPlayerAttribute;
import com.github.theeffortfighter.playerex.api.attribute.IPlayerAttributes;
import com.github.theeffortfighter.playerex.api.attribute.PlayerAttributes;
import com.github.theeffortfighter.playerex.init.Registry;
import com.github.theeffortfighter.playerex.util.CommonConfig;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Capability implementation (object).
 */
public class AttributesCapability implements IPlayerAttributes {
	private Map<IPlayerAttribute, Float> clientStore;
	private Map<EquipmentSlot, ItemStack> equipmentStore;
	private AttributeMap attributeModifierManager;
	private CompoundTag tag;
	private double offset, scale;
	
	public AttributesCapability() {
		Builder var0 = AttributeSupplier.builder();
		ListTag var1 = new ListTag();
		CompoundTag var2 = new CompoundTag();
		
		this.tag = new CompoundTag();
		this.tag.put("Attributes", var1);
		this.tag.put("Modifiers", var2);
		
		for(IPlayerAttribute var : PlayerAttributes.attributes()) {
			putAttribute(var, 0D);
			
			if(var.type() == IPlayerAttribute.Type.ALL || var.type() == IPlayerAttribute.Type.DATA) {
				var0 = var0.add(var.get());
			}
		}
		
		this.offset = 0D;
		this.scale = 0D;
		this.attributeModifierManager = new AttributeMap(var0.build());
		this.clientStore = new HashMap<>();
		this.equipmentStore = new HashMap<>();
		this.equipmentStore.put(EquipmentSlot.HEAD, ItemStack.EMPTY);
		this.equipmentStore.put(EquipmentSlot.CHEST, ItemStack.EMPTY);
		this.equipmentStore.put(EquipmentSlot.LEGS, ItemStack.EMPTY);
		this.equipmentStore.put(EquipmentSlot.FEET, ItemStack.EMPTY);
		this.equipmentStore.put(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
	}
	
	/**
	 * @param par0
	 * @param par1
	 * @return The correct AttributeModifierManager instance based on the input IPlayerAttribute.
	 */
	private AttributeMap getAttributeModifier(Player par0, IPlayerAttribute par1) {
		if(par1.type() == IPlayerAttribute.Type.GAME) {
			return par0.getAttributes();
		} else if(par1.type() == IPlayerAttribute.Type.ALL || par1.type() == IPlayerAttribute.Type.DATA) {
			return this.attributeModifierManager;
		}
		
		return null;
	}
	
	/** 
	 * @return True if the attributeList is empty;
	 */
	private boolean isAttributeListEmpty() {
		return this.tag.getList("Attributes", 10).isEmpty();
	}
	
	/**
	 * Creates a CompoundNBT tag for the input IPlayerAttribute with the input storage value.
	 * @param par0
	 * @param par1
	 */
	private void createAttributeTag(IPlayerAttribute par0, double par1) {
		CompoundTag var1 = new CompoundTag();
		
		var1.putString("Name", par0.toString());
		var1.putDouble("Value", par1);
		
		this.tag.getList("Attributes", 10).add(var1);
	}
	
	/**
	 * If the Attribute Tag for the input IPlayerAttribute does not exist, creates one with {@link #createAttributeTag(IPlayerAttribute, double)}.
	 * If the Attribute Tag for the input IPlayerAttribute does exist however, only sets the storage value to the input value.
	 * @param par0
	 * @param par1
	 */
	private void putAttribute(IPlayerAttribute par0, double par1) {
		if(isAttributeListEmpty()) {
			createAttributeTag(par0, par1);
			
			return;
		}
		
		for(Tag var : this.tag.getList("Attributes", 10)) {
			CompoundTag var0 = (CompoundTag)var;
			String var1 = var0.getString("Name");
			
			if(var1.equals(par0.toString())) {
				var0.putDouble("Value", par1);
				
				return;
			}
		}
		
		createAttributeTag(par0, par1);
	}
	
	/**
	 * @param par0
	 * @return The input IPlayerAttribute instance's storage value if it exists; else returns the input IPlayerAttribute instance's default value.
	 */
	private double getAttribute(IPlayerAttribute par0) {
		double var0 = par0.get().getDefaultValue();
		
		if(isAttributeListEmpty()) return var0;
		
		for(Tag var : this.tag.getList("Attributes", 10)) {
			CompoundTag var1 = (CompoundTag)var;
			String var2 = var1.getString("Name");
			
			if(var2.equals(par0.toString())) return var1.getDouble("Value");
		}
		
		return var0;
	}
	
	/**
	 * Adds a new CompountNBT tag to the relevant modifier list to hold the input AttributeModifier; if the tag already exists, then nothing is done.
	 * If no modifier for the input attribute exists yet, also creates the attribute's modifier list.
	 * @param par0
	 * @param par1
	 */
	private void putModifier(IPlayerAttribute par0, AttributeModifier par1) {
		CompoundTag var0 = this.tag.getCompound("Modifiers");
		ListTag var1 = (var0.contains(par0.toString()) ? var0.getList(par0.toString(), 10) : new ListTag());
		
		for(Tag var : var1) {
			CompoundTag var2 = (CompoundTag)var;
			UUID var3 = var2.getUUID("UUID");
			UUID var4 = par1.getId();
			
			if(var3.equals(var4)) return;
		}
		
		var1.add(par1.save());
		var0.put(par0.toString(), var1);
	}
	
	/**
	 * Removes the input attribute modifier tag from the attribute's modifier list if it exists.
	 * If, after removal, the attribute's modifier list is empty, removes the modifier list too.
	 * @param par0
	 * @param par1
	 */
	private void removeModifier(IPlayerAttribute par0, AttributeModifier par1) {
		CompoundTag var0 = this.tag.getCompound("Modifiers");
		
		if(!var0.contains(par0.toString())) return;
		
		ListTag var1 = var0.getList(par0.toString(), 10);
		
		for(int var = 0; var < var1.size(); var++) {
			CompoundTag var2 = var1.getCompound(var);
			UUID var3 = var2.getUUID("UUID");
			UUID var4 = par1.getId();
			
			if(var3.equals(var4)) {
				var1.remove(var);
			}
		}
		
		if(var1.isEmpty()) var0.remove(par0.toString());
	}
	
	/**
	 * Takes all modifiers from the modifier list and reapplies them.
	 * @param par0
	 */
	private void refreshModifierMap(Player par0) {
		CompoundTag var0 = this.tag.getCompound("Modifiers");
		Multimap<IPlayerAttribute, AttributeModifier> var1 = ArrayListMultimap.create();
		
		if(var0.isEmpty()) return;
		
		for(String var : var0.getAllKeys()) {
			ListTag var2 = var0.getList(var, 10);
			IPlayerAttribute var3 = PlayerAttributes.fromRegistryName(var);
			
			if(var3 == null) return;
			
			for(Tag var4 : var2) {
				CompoundTag var5 = (CompoundTag)var4;
				AttributeModifier var6 = AttributeModifier.load(var5);
				
				var1.put(var3, var6);
			}
		}
		
		Multimap<Attribute, AttributeModifier> var2 = HashMultimap.create();
		Multimap<Attribute, AttributeModifier> var3 = HashMultimap.create();
		
		var1.forEach((var4, var5) -> {
			if(var4.type() == IPlayerAttribute.Type.GAME) {
				var2.put(var4.get(), var5);
			} else if(var4.type() == IPlayerAttribute.Type.ALL || var4.type() == IPlayerAttribute.Type.DATA) {
				var3.put(var4.get(), var5);
			}
		});
		
		par0.getAttributes().addTransientAttributeModifiers(var2);
		this.attributeModifierManager.addTransientAttributeModifiers(var3);
	}
	
	@Override
	public double expCoeff(Player par0) {
		return get(par0, PlayerAttributes.EXPERIENCE) / (this.offset + (this.scale * Math.pow(get(par0, PlayerAttributes.LEVEL), 2.0D)));
	}
	
	@Override
	public double get(Player par0, IPlayerAttribute par1) {
		if(par0.level.isClientSide) return this.clientStore.getOrDefault(par1, 0F);
		
		return getAttributeModifier(par0, par1).getValue(par1.get());
	}
	
	@Override
	public void add(Player par0, IPlayerAttribute par1, double par2) {
		double var0 = getAttribute(par1) + par2;
		
		Multimap<Attribute, AttributeModifier> var1 = HashMultimap.create();
		AttributeModifier var2 = new AttributeModifier(par1.uuid(), par1.toString(), var0, AttributeModifier.Operation.ADDITION);
		
		var1.put(par1.get(), var2);
		
		getAttributeModifier(par0, par1).addTransientAttributeModifiers(var1);
		putAttribute(par1, var0);
		
		PlayerAttributes.adders().get(par1.registryName()).forEach(var -> var.accept(par0, this, par2));
		
		send(par0);
	}
	
	@Override
	public void forceSet(Player par0, IPlayerAttribute par1, double par2) {
		Multimap<Attribute, AttributeModifier> var0 = HashMultimap.create();
		AttributeModifier var1 = new AttributeModifier(par1.uuid(), par1.toString(), 0D, AttributeModifier.Operation.ADDITION);
		
		var0.put(par1.get(), var1);
		
		getAttributeModifier(par0, par1).addTransientAttributeModifiers(var0);
		putAttribute(par1, 0D);
		
		send(par0);
	}
	
	@Override
	public IPlayerAttributes applyModifier(Player par0, IPlayerAttribute par1, AttributeModifier par2) {
		if(par0 == null || par1 == null || par2 == null) return this;
		
		Multimap<Attribute, AttributeModifier> var0 = HashMultimap.create();
		
		var0.put(par1.get(), par2);
		
		putModifier(par1, par2);
		getAttributeModifier(par0, par1).addTransientAttributeModifiers(var0);
		
		PlayerAttributes.modifiers().get(par1.registryName()).forEach(var -> var.accept(par0, this::applyModifier, par2));
		
		send(par0);
		
		return this;
	}
	
	@Override
	public IPlayerAttributes removeModifier(Player par0, IPlayerAttribute par1, AttributeModifier par2) {
		if(par0 == null || par1 == null || par2 == null) return this;
		
		Multimap<Attribute, AttributeModifier> var0 = HashMultimap.create();
		
		var0.put(par1.get(), par2);
		
		removeModifier(par1, par2);
		getAttributeModifier(par0, par1).removeAttributeModifiers(var0);
		
		PlayerAttributes.modifiers().get(par1.registryName()).forEach(var -> var.accept(par0, this::removeModifier, par2));
		
		send(par0);
		
		return this;
	}
	
	@Override
	public CompoundTag write() {
		return this.tag;
	}
	
	@Override
	public void read(CompoundTag par0) {
		this.tag = par0;
	}
	
	/**
	 * Updates and refreshes the capability data during event's such as death and cloning. Dev's should AVOID using this.
	 * @param par0 PlayerEntity instance.
	 */
	public void update(Player par0) {
		for(IPlayerAttribute par1 : PlayerAttributes.attributes()) {
			double var0 = getAttribute(par1);
			
			Multimap<Attribute, AttributeModifier> var1 = HashMultimap.create();
			AttributeModifier var2 = new AttributeModifier(par1.uuid(), par1.toString(), var0, AttributeModifier.Operation.ADDITION);
			
			var1.put(par1.get(), var2);
			
			getAttributeModifier(par0, par1).addTransientAttributeModifiers(var1);
		}
		
		refreshModifierMap(par0);
		
		this.offset = CommonConfig.COMMON.offset.get();
		this.scale = CommonConfig.COMMON.scale.get();
	}
	
	/**
	 * Receives data from the server.
	 * @param par0
	 * @param par1
	 * @param par2
	 */
	public void receive(CompoundTag par0, double par1, double par2) {
		if(par0 == null) return;
		if(!par0.contains("Data")) return;
		
		ListTag var0 = par0.getList("Data", 10);
		
		for(Tag var : var0) {
			CompoundTag var1 = (CompoundTag)var;
			IPlayerAttribute var2 = PlayerAttributes.fromRegistryName(var1.getString("Name"));
			
			this.clientStore.put(var2, var1.getFloat("Value"));
		}
		
		this.offset = par1;
		this.scale = par2;
	}
	
	/**
	 * Sends data from the server to the client.
	 * @param par0 PlayerEntity instance.
	 */
	public void send(Player par0) {
		if(par0 == null) return;
		if(par0.level.isClientSide) return;
		
		CompoundTag var0 = new CompoundTag();
		ListTag var1 = new ListTag();
		
		for(IPlayerAttribute var : PlayerAttributes.attributes()) {
			CompoundTag var2 = new CompoundTag();
			
			var2.putString("Name", var.toString());
			var2.putFloat("Value", (float)get(par0, var));
			var1.add(var2);
		}
		
		var0.put("Data", var1);
		
		Registry.NETWORK.sendTo(new SyncPlayerAttributes(var0, this.offset, this.scale), ((ServerPlayer)par0).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}
	
	/**
	 * Sets the equipment store.
	 * @param par0
	 * @param par1
	 */
	public void putEquipment(EquipmentSlot par0, ItemStack par1) {
		this.equipmentStore.put(par0, par1);
	}
	
	/**
	 * @param par0
	 * @return The ItemStack for this equipment
	 */
	public ItemStack getEquipment(EquipmentSlot par0) {
		return this.equipmentStore.getOrDefault(par0, ItemStack.EMPTY);
	}
}
