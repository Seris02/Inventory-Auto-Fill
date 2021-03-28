package com.hitherguy.invautofill.gui;

import com.hitherguy.invautofill.ConfigHandler;

public class InvAutoFillButtonLock extends InvAutoFillButton {
	public InvAutoFillButtonLock(int x, int y) {
		super(x,y,0,0,btn -> setToggle());
	}
	public static void setToggle() {
		ConfigHandler.CLIENT.hotbarLocked.set(!ConfigHandler.CLIENT.hotbarLocked.get());
	}
}
