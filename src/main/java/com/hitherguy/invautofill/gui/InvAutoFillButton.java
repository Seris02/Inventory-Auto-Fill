package com.hitherguy.invautofill.gui;

import javax.annotation.ParametersAreNonnullByDefault;

import com.hitherguy.invautofill.ConfigHandler;
import com.hitherguy.invautofill.InvAutoFill;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

public class InvAutoFillButton extends ExtendedButton {
	protected static final ResourceLocation button = new ResourceLocation(InvAutoFill.MODID,"textures/gui/hotlock.png");
	private final int tx;
	private final int ty;
	public InvAutoFillButton(int x, int y, int tx, int ty, IPressable handler) {
		super(x,y,14,16,new StringTextComponent(""),handler);
		this.tx = tx;
		this.ty = ty;
	}
	
	@Override
	@ParametersAreNonnullByDefault
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		isHovered = this.active && this.visible && isMouseOver(mouseX,mouseY);
		boolean isToggled = ConfigHandler.CLIENT.hotbarLocked.get();
		Minecraft.getInstance().getTextureManager().bind(button);
		//0 = white-locked
		//16 = green-locked
		//32 = white-unlocked
		//48 = green-unlocked
		blit(matrix,x,y,tx,ty + (isHovered ? (isToggled ? 16 : 48) : (isToggled ? 0 : 32)), 14, 16);
	}
	
}
