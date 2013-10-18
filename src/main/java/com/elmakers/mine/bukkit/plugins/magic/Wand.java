package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

public class Wand {
	private ItemStack item;
	
	private static Enchantment WandEnchantment = Enchantment.ARROW_INFINITE;
	private static Material WandMaterial = Material.STICK;
	
	public Wand() {
		item = new ItemStack(WandMaterial);
		// This will make the Bukkit ItemStack into a real ItemStack with NBT data.
		item = InventoryUtils.getCopy(item);
		item.addUnsafeEnchantment(WandEnchantment, 1);
	}
	
	public Wand(ItemStack item) {
		this.item = item;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	@SuppressWarnings("deprecation")
	public void removeMateriall(Material material, byte data) {
		Integer id = material.getId();
		String materialString = id.toString();
		if (data > 0) {
			materialString += ":" + data;
		}

		String[] materials = getMaterials();
		Set<String> materialMap = new TreeSet<String>();
		for (int i = 0; i < materials.length; i++) {
			materialMap.add(materials[i]);
		}
		materialMap.remove(materialString);
		setMaterials(materialMap);
	}
	
	@SuppressWarnings("deprecation")
	public void addMaterial(Material material, byte data) {
		Integer id = material.getId();
		String materialString = id.toString();
		if (data > 0) {
			materialString += ":" + data;
		}

		String[] materials = getMaterials();
		Set<String> materialMap = new TreeSet<String>();
		for (int i = 0; i < materials.length; i++) {
			materialMap.add(materials[i]);
		}
		materialMap.add(materialString);
		setMaterials(materialMap);
	}
	
	protected void setMaterials(Collection<String> materialNames) {
		String spellString = StringUtils.join(materialNames, "|");

		// Set new spells count
		setMaterialCount(materialNames.size());

		// Set new spells string
		InventoryUtils.setMeta(item, "magic_materials", spellString);
	}
	
	public String[] getMaterials() {
		String materialsString = InventoryUtils.getMeta(item, "magic_materials");
		if (materialsString == null) materialsString = "";

		return StringUtils.split(materialsString, "|");
	}
	
	public void addSpells(Collection<String> spellNames) {
		String[] spells = getSpells();
		Set<String> spellMap = new TreeSet<String>();
		for (String spell : spells) {
			spellMap.add(spell);
		}
		for (String spellName : spellNames) { 	
			spellMap.add(spellName);
		}
				
		setSpells(spellMap);
	}
	
	public String[] getSpells() {
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		if (spellString == null) spellString = "";

		return StringUtils.split(spellString, "|");
	}

	public void removeSpell(String spellName) {
		String[] spells = getSpells();
		Set<String> spellMap = new TreeSet<String>();
		for (int i = 0; i < spells.length; i++) {
			spellMap.add(spells[i]);
		}
		spellMap.remove(spellName);
		setSpells(spellMap);
	}
	
	public void addSpell(String spellName) {
		List<String> names = new ArrayList<String>();
		names.add(spellName);
		addSpells(names);
	}
	
	public void setSpells(Collection<String> spellNames) {
		String spellString = StringUtils.join(spellNames, "|");

		// Set new spells count
		setSpellCount(spellNames.size());

		// Set new spells string
		InventoryUtils.setMeta(item, "magic_spells", spellString);
	}
	
	public void setSpellCount(int spellCount) {
		updateLore(spellCount, getMaterials().length);
	}
	
	public void setMaterialCount(int materialCount) {
		updateLore(getSpells().length, materialCount);
	}
	
	public void setName(String name) {
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);

		// The all-important last step of restoring the spell list, something
		// the Anvil will blow away.
		InventoryUtils.setMeta(item, "magic_spells", spellString);
	}

	protected void updateLore(int spellCount, int materialCount) {
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("Knows " + spellCount +" Spells");
		if (materialCount > 0) {
			lore.add("Has " + materialCount +" Materials");
		}
		lore.add("Left-click to cast active spell");
		lore.add("Right-click to cycle spells");
		meta.setLore(lore);
		item.setItemMeta(meta);

		// Reset spell list!
		InventoryUtils.setMeta(item, "magic_spells", spellString);
	}
	
	public static Wand getActiveWand(Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		return isWand(activeItem) ? new Wand(activeItem) : null;
	}
	
	public static boolean hasActiveWand(Player player) {
		ItemStack activeItem =  player.getInventory().getItemInHand();
		return isWand(activeItem);
	}

	public static boolean isWand(ItemStack item) {
		return item != null && item.getType() == Material.STICK && item.hasItemMeta() && item.getItemMeta().hasEnchant(WandEnchantment);
	}

	public static boolean isSpell(ItemStack item) {
		return item != null && item.getType() != Material.STICK && item.hasItemMeta() && item.getItemMeta().hasEnchant(WandEnchantment);
	}
	
	public void updateInventory(PlayerSpells playerSpells) {
		updateInventory(playerSpells, playerSpells.getPlayer().getInventory().getHeldItemSlot());
	}

	@SuppressWarnings("deprecation")
	protected void updateInventory(PlayerSpells playerSpells, int itemSlot) {
		Player player = playerSpells.getPlayer();
		Inventory inventory = player.getInventory();
		inventory.clear();
		inventory.setItem(itemSlot, item);
		String spellString = InventoryUtils.getMeta(item, "magic_spells");
		String[] spells = StringUtils.split(spellString, "|");

		int currentIndex = 0;
		for (int i = 0; i < spells.length; i++) {
			if (currentIndex == itemSlot) currentIndex++;
			Spell spell = playerSpells.getSpell(spells[i]);
			if (spell != null) {
				ItemStack itemStack = new ItemStack(spell.getMaterial(), 1);
				itemStack.addUnsafeEnchantment(WandEnchantment, 1);
				ItemMeta meta = itemStack.getItemMeta();
				meta.setDisplayName(spell.getName());
				List<String> lore = new ArrayList<String>();
				lore.add(spell.getCategory());
				lore.add(spell.getDescription());
				meta.setLore(lore);
				itemStack.setItemMeta(meta);
				inventory.setItem(currentIndex, itemStack);
			}

			currentIndex++;
		}

		player.updateInventory();
	}
	
	public void saveInventory(PlayerSpells playerSpells) {
		PlayerInventory inventory = playerSpells.getPlayer().getInventory();
		
		// Rebuild spell inventory, save in wand.
		ItemStack[] items = inventory.getContents();
		List<String> spellNames = new ArrayList<String>();
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) continue;
			if (!isSpell(items[i])) continue;

			Spell spell = playerSpells.getSpell(items[i].getType());
			if (spell == null) continue;
			spellNames.add(spell.getKey());
		}
		setSpells(spellNames);
	}

	public static boolean isActive(Player player) {
		ItemStack activeItem = player.getInventory().getItemInHand();
		return isWand(activeItem);
	}
}
