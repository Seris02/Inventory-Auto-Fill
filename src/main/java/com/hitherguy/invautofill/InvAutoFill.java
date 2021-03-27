package com.hitherguy.invautofill;

import org.lwjgl.glfw.GLFW;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("invautofill")

public class InvAutoFill
{
	private static final Object Sync = new Object();
	public static final String MODID = "invautofill";
	private static boolean ClientOnly = true;
	public static final String version = "1";
	public static SimpleChannel SInst;
	public static KeyBinding[] keyBindings;
    public InvAutoFill() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        SInst = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID,"channel"), () -> version, s -> {
        	synchronized(Sync) {
        		ClientOnly = NetworkRegistry.ABSENT.equals(s) || NetworkRegistry.ACCEPTVANILLA.equals(s);
        		return version.equals(s) || ClientOnly;
        	}
        }, s -> version.equals(s) || NetworkRegistry.ABSENT.equals(s) || NetworkRegistry.ACCEPTVANILLA.equals(s));
        SInst.registerMessage(0,PacketAutofill.class,PacketAutofill::encode,PacketAutofill::new,PacketAutofill::handle);
    }
    public static boolean unsynced() {
    	synchronized (Sync) {
    		return ClientOnly;
    	}
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    	if (keyBindings == null) {
    		keyBindings = new KeyBinding[3];
    	}
    	keyBindings[0] = new KeyBinding("key.autofillplayer.desc",GLFW.GLFW_KEY_SEMICOLON,"key.categories.invautofill");
    	keyBindings[1] = new KeyBinding("key.autofillinv.desc",GLFW.GLFW_KEY_LEFT_BRACKET,"key.categories.invautofill");
    	keyBindings[2] = new KeyBinding("key.autofillundermouse.desc",GLFW.GLFW_MOUSE_BUTTON_MIDDLE,"key.categories.invautofill");
        for (KeyBinding key : keyBindings) {
        	ClientRegistry.registerKeyBinding(key);
        }
    }


    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void keyInput(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
    	InputMappings.Input input = InputMappings.getKey(event.getKeyCode(), event.getScanCode());
    	if (event.getGui() instanceof ContainerScreen 
    			&& !(event.getGui() instanceof CreativeScreen) 
    			&& !(event.getGui().getFocused() instanceof TextFieldWidget)) {
	    	if (keyBindings[0].isActiveAndMatches(input)) {
	    		autofill_request(true);
	    	}
	    	if (keyBindings[1].isActiveAndMatches(input)) {
	    		autofill_request(false);
	    	}
	    	Slot slotUnderMouse = ((ContainerScreen<?>) event.getGui()).getSlotUnderMouse();
	    	if (slotUnderMouse != null && slotUnderMouse.container != null) {
	    		boolean way = !(slotUnderMouse.container instanceof PlayerInventory);
	    		if (keyBindings[2].isActiveAndMatches(input)) {
	    			autofill_request(way);
	    		}
	    	}
    	}
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void mouseInput(GuiScreenEvent.MouseClickedEvent.Pre event) {
    	if (event.getGui() instanceof ContainerScreen && !(event.getGui() instanceof CreativeScreen)) {
    		if (!keyBindings[2].getKeyConflictContext().isActive() || !keyBindings[2].matchesMouse(event.getButton())) {
    			return;
    		}
    		Slot slotUnderMouse = ((ContainerScreen<?>) event.getGui()).getSlotUnderMouse();
	    	if (slotUnderMouse != null && slotUnderMouse.container != null) {
	    		boolean way = !(slotUnderMouse.container instanceof PlayerInventory);
	    		autofill_request(way);
	    		event.setCanceled(true);
	    	}
    	}
    }
    
    public static void autofill_request(boolean way) {
    	if (unsynced()) {
    		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> autofillIntoInventory(Utils.safeGetPlayer(),way));
    	} else {
    		SInst.sendToServer(new PacketAutofill(way));
    	}
    }
    
    public static void autofillIntoInventory(PlayerEntity player, boolean way) {
    	Container c = player.containerMenu;
    	IInventory relevant;
    	IInventory irrelevant;
		if (way) {
			relevant = player.inventory;
			irrelevant = ItemUtils.getNonPlayerInventory(c);
		} else {
			relevant = ItemUtils.getNonPlayerInventory(c);
			irrelevant = player.inventory;
		}
		for (int x = 0; x < relevant.getContainerSize(); x++) {
			ItemStack item = relevant.getItem(x);
			//System.out.println(item.getItem().getRegistryName().toString());
			if (item.isEmpty()) {
				continue;
			}
			ItemUtils.quickMoveToContainer(irrelevant, item, true);
		}
    }
}
