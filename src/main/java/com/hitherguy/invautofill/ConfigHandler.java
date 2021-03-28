package com.hitherguy.invautofill;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
	public static class Client {
		public final ForgeConfigSpec.BooleanValue allItems;
		public final ForgeConfigSpec.BooleanValue hotbarLocked;
		public final ForgeConfigSpec.BooleanValue hotbarButtonEnabled;
		public Client(ForgeConfigSpec.Builder builder)  {
			builder.comment("Client").push("client");
			allItems = builder.comment("Set this to true if all items should be transferred when autofilling an inventory, as opposed to only transferring items that are already in that inventory.").define("allItems", true);
			hotbarLocked = builder.comment("Set this to true if the hotbar should not be messed with when filling.").define("hotbarLocked", true);
			hotbarButtonEnabled = builder.comment("Set this to true if the hotbar lock GUI button should be visible. False if it shouldn't be visible.").define("hotbarButtonEnabled", true);
			builder.pop();
		}
	}
	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENTSPEC;
	static {
		final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENTSPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}
}
