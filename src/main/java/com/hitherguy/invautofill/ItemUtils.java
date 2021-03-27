package com.hitherguy.invautofill;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemUtils {
	
	public static void quickMoveToContainer(IInventory inventory, ItemStack stack, boolean allItems) {
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
			putInFreeSlot(inventory,stack);
		}
	}
	public static void putInFreeSlot(IInventory inv, ItemStack stack) {
		for (int x = 0; x < inv.getContainerSize(); x++) {
			if (inv.getItem(x).isEmpty() && inv.canPlaceItem(x, stack)) {
				inv.setItem(x, stack.copy());
				stack.setCount(0);
			}
		}
	}
	public static int freeSlot(IInventory inv, ItemStack stack) {
		for (int x = 0; x < inv.getContainerSize(); x++) {
			if (inv.getItem(x).isEmpty() && inv.canPlaceItem(x, stack)) {
				return x;
			}
		}
		return inv.getContainerSize() + 1;
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
}
