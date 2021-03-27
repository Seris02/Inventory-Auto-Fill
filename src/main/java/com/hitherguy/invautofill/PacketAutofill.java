package com.hitherguy.invautofill;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketAutofill {
	private final boolean way;
	private final boolean allItems;
	public PacketAutofill(PacketBuffer buf) {
		this(buf.readBoolean(),buf.readBoolean());
	}
	public PacketAutofill(boolean way, boolean allItems) {
		this.way = way;
		this.allItems = allItems;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {

		ctx.get().enqueueWork(() -> {
			InvAutoFill.autofillIntoInventory(ctx.get().getSender(),way,allItems);
		});
		ctx.get().setPacketHandled(true);
	}
	public void encode(PacketBuffer buf) {
		buf.writeBoolean(way);
		buf.writeBoolean(allItems);
	}
}
