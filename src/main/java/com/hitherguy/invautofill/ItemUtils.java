package com.hitherguy.invautofill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemUtils {
	
	public static void quickMoveToContainer(IInventory inventory, ItemStack stack, boolean allItems, List<Slot> slots) {
		Set<Item> item = new HashSet<Item>();
		item.add(stack.getItem());
		boolean stackable = stack.isStackable();
		int stacksize = stack.getMaxStackSize();
		if (!allItems) {
			if (!inventory.hasAnyOf(item)) {
				return;
			}
		}
		int stacksunfilled = stacksNotFull(inventory,stack);
		for (int x = 0; x < inventory.getContainerSize(); x++) {
			if (stack.isEmpty()) {
				break;
			}
			if (stacksunfilled > 0) {
				if (stackable && inventory.getItem(x).sameItem(stack)) {
					if (inventory.getItem(x).getCount() < stacksize) {
						int h = stack.getCount() + inventory.getItem(x).getCount();
						int k = 0; 
						if (h > stacksize) {
							k = h - stacksize;
							h = stacksize;
						}
						inventory.getItem(x).setCount(h);
						stack.setCount(k);
						stacksunfilled = stacksNotFull(inventory,stack);
					}
				}
			} else {
				break;
			}
		}
		if (!stack.isEmpty()) {
			putInFreeSlot(inventory,stack,false, slots);
		}
	}
	public static void putInFreeSlot(IInventory inv, ItemStack stack, boolean inHotbar, List<Slot> slots) {
		boolean hotbarLocked = ConfigHandler.CLIENT.hotbarLocked.get();
		for (int x = (!inHotbar && inv instanceof PlayerInventory) ? 9 : 0; x < slots.size(); x++) {
			if (inv.getItem(x).isEmpty() && inv.canPlaceItem(x, stack) && !(inv instanceof PlayerInventory && x > 35 && !hotbarLocked)) {
				if (slots.get(x).mayPlace(stack)) {
					inv.setItem(x, stack.copy());
					stack.setCount(0);
					return;
				}
			}
		}
		if (!inHotbar && inv instanceof PlayerInventory) {
			putInFreeSlot(inv,stack,true, slots);
		}
	}

	public static int stacksNotFull(IInventory inv, ItemStack stack) {
		int slotsFree = 0;
		for (int y = 0; y < inv.getContainerSize(); y++) {
			if (inv.getItem(y).sameItem(stack)) {
				if (inv.getItem(y).getCount() < stack.getMaxStackSize()) {
					slotsFree++;
				}
			}
		}
		return slotsFree;
	}
	public static IInventory getNonPlayerInventory(Container container) {
		for (int x = 0; x < container.slots.size(); x++) {
			if (!(container.slots.get(x).container instanceof PlayerInventory)) {
				return container.slots.get(x).container;
			}
		}
		return null;
	}
	public static List<Slot> getPlayerSlots(Container container) {
		List<Slot> slots = new ArrayList<>();
		for (int x = 0; x < container.slots.size(); x++) {
			if (container.slots.get(x).container instanceof PlayerInventory) {
				slots.add(container.getSlot(x));
			}
		}
		return slots;
	}
}
