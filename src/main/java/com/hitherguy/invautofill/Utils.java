package com.hitherguy.invautofill;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

public class Utils {
	
	public static PlayerEntity safeGetPlayer() {
		//Yeaaaaah, this method is just a blatant copypaste from Inventory Tweaks Renewed, sorry bout that
		Minecraft instance = Minecraft.getInstance();
		return instance.player;
	}
	
	
}









