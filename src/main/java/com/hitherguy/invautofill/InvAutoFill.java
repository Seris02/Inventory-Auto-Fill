package com.hitherguy.invautofill;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.base.Throwables;
import com.hitherguy.invautofill.gui.InvAutoFillButtonLock;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod("invautofill")
public class InvAutoFill
{
	//some of this code looks eerily similar to Inventory Tweaks Renewed, but the only code I used from that mod was when I needed help
	//with sending packets and making sure the client and server bits worked correctly, thanks reo-ar
	//version 1.3 - and the code for the GUI button, which I adapted and improved, PS if you're reading this, you don't need to
	//use the obfuscationreflection to get guiLeft and guiTop, there are methods for that for ContainerScreen, -getGuiLeft() and -getGuiTop()
	private static final Object Sync = new Object();
	public static final String MODID = "invautofill";
	private static boolean ClientOnly = true;
	public static final String version = "1.2";
	public static SimpleChannel SInst;
	public static KeyBinding[] keyBindings;
	public static boolean allItems = true;
	public static boolean configged = false;
    public InvAutoFill() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENTSPEC);
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
    	if (keyBindings == null) {
    		keyBindings = new KeyBinding[4];
    	}
    	keyBindings[0] = new KeyBinding("key.autofillplayer.desc",GLFW.GLFW_KEY_SEMICOLON,"key.categories.invautofill");
    	keyBindings[1] = new KeyBinding("key.autofillinv.desc",GLFW.GLFW_KEY_LEFT_BRACKET,"key.categories.invautofill");
    	keyBindings[2] = new KeyBinding("key.autofillundermouse.desc",GLFW.GLFW_MOUSE_BUTTON_MIDDLE,"key.categories.invautofill");
    	keyBindings[3] = new KeyBinding("key.autofilltoggleall.desc",GLFW.GLFW_KEY_RIGHT_BRACKET,"key.categories.invautofill");
        for (KeyBinding key : keyBindings) {
        	ClientRegistry.registerKeyBinding(key);
        }
    }


    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    /*@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            
        }
    }*/
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
    	if (event.getGui() instanceof ContainerScreen && !(event.getGui() instanceof CreativeScreen)) {
    		ContainerScreen<?> c = ((ContainerScreen<?>)event.getGui());
    		Slot slotIn = ItemUtils.getPlayerSlots(c.getMenu()).get(8); //last hotbar slot
    		if (slotIn != null) {
    			try {
    				event.addWidget(new InvAutoFillButtonLock(c.getGuiLeft() + slotIn.x + 17, c.getGuiTop() + slotIn.y + 58));
    			} catch (Exception exception) {
    				Throwables.throwIfUnchecked(exception);
    				throw new RuntimeException(exception);
    			}
    		}
    	}
    	
    }
    
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL,receiveCanceled=true)
    public void onEvent(KeyInputEvent event) {
    	InputMappings.Input input = InputMappings.getKey(event.getKey(), event.getScanCode());
    	if (event.getAction() == GLFW.GLFW_PRESS && keyBindings[3].isActiveAndMatches(input)) {
    		ConfigHandler.CLIENT.allItems.set(!ConfigHandler.CLIENT.allItems.get());
    		allItems = ConfigHandler.CLIENT.allItems.get();
    		Minecraft instance = Minecraft.getInstance();
			String s = allItems ? "Autofilling all items." : "Autofilling only items present.";
			ITextComponent text = new StringTextComponent(s);
			instance.player.sendMessage(text, null);
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
    	if (!configged) {
    		allItems = ConfigHandler.CLIENT.allItems.get();
    	}
    	if (unsynced()) {
    		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> autofillIntoInventory(Utils.safeGetPlayer(),way,allItems));
    	} else {
    		SInst.sendToServer(new PacketAutofill(way,allItems));
    	}
    }
    
    public static void autofillIntoInventory(PlayerEntity player, boolean way, boolean allItems) {
    	Container c = player.containerMenu;
    	IInventory relevant = way ? player.inventory : ItemUtils.getNonPlayerInventory(c); //transferring from
    	IInventory irrelevant = way ? ItemUtils.getNonPlayerInventory(c) : player.inventory; //transferring to
    	int starting = way ? 9 : 0; //hotbar last
    	List<Slot> playerSlots = new ArrayList<>();
    	List<Slot> invSlots = new ArrayList<>();
    	List<Slot> relevantSlots = way ? playerSlots : invSlots;
    	List<Slot> irrelevantSlots = way ? invSlots : playerSlots;
    	boolean hotbarLocked = ConfigHandler.CLIENT.hotbarLocked.get();
    	for (int u = 0; u < c.slots.size(); u++) {
    		if (c.getSlot(u).container instanceof PlayerInventory && playerSlots.size() < 37) {
    			playerSlots.add(c.getSlot(u));
    		} else {
    			invSlots.add(c.getSlot(u));
    		}
    	}
		for (int x = starting; x < relevant.getContainerSize(); x++) {
			ItemStack item = relevant.getItem(x);
			if (item.isEmpty() || !relevantSlots.get(x).mayPickup(player)) {
				continue;
			}
			
			ItemUtils.quickMoveToContainer(irrelevant, item, allItems, irrelevantSlots);
		}
		if (starting > 0 && !hotbarLocked) { //hotbar last and locked
			for (int x = 0; x < 10; x++) {
				ItemStack item = relevant.getItem(x);
				if (item.isEmpty()) {
					continue;
				}
				ItemUtils.quickMoveToContainer(irrelevant, item, allItems, irrelevantSlots);
			}
		}
    }
}
