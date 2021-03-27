package com.hitherguy.invautofill;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAutofill {
	private final boolean way;
	public PacketAutofill(PacketBuffer buf) {
		this(buf.readBoolean());
	}
	public PacketAutofill(boolean way) {
		this.way = way;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			InvAutoFill.autofillIntoInventory(ctx.get().getSender(),way);
		});
		ctx.get().setPacketHandled(true);
	}
	public void encode(PacketBuffer buf) {
		buf.writeBoolean(way);
	}
}
