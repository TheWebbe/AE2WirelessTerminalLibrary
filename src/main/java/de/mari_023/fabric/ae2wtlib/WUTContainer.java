package de.mari_023.fabric.ae2wtlib;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import appeng.container.ContainerLocator;
import appeng.container.ContainerNull;
import appeng.container.implementations.MEPortableCellContainer;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.CraftingTermSlot;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.IContainerCraftingPacket;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WUTContainer extends MEPortableCellContainer implements IAEAppEngInventory, IContainerCraftingPacket {

    public static ScreenHandlerType<WUTContainer> TYPE;

    public static final ContainerHelper<WUTContainer, WUTGuiObject> helper = new ContainerHelper<>(WUTContainer::new, WUTGuiObject.class);

    public static WUTContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
    private final CraftingTermSlot outputSlot;
    private Recipe<CraftingInventory> currentRecipe;

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final WUTGuiObject wirelessTerminalGUIObject;


    public WUTContainer(int id, final PlayerInventory ip, final WUTGuiObject gui) {
        super(TYPE, id, ip, gui);
        wirelessTerminalGUIObject = gui;

        final FixedItemInv crafting = getInventoryByName("crafting");
        final FixedWUTInv fixedWUTInv = new FixedWUTInv(getPlayerInv());

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                addSlot(craftingSlots[x + y * 3] = new CraftingMatrixSlot(this, crafting, x + y * 3, 37 + x * 18 + 43, -72 + y * 18 - 4));
            }
        }
        AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
        addSlot(outputSlot = new CraftingTermSlot(getPlayerInv().player, getActionSource(), getPowerSource(), gui.getIStorageGrid(), crafting, crafting, output, 131 + 43, -72 + 18 - 4, this));

        //armor
        addSlot(new AppEngSlot(fixedWUTInv, 3, 8, -76));
        addSlot(new AppEngSlot(fixedWUTInv, 2, 8, -58));
        addSlot(new AppEngSlot(fixedWUTInv, 1, 8, -40));
        addSlot(new AppEngSlot(fixedWUTInv, 0, 8, -22));
        //offhand
        addSlot(new AppEngSlot(fixedWUTInv, 4, 80, -22));
        //trashslot
        addSlot(new AppEngSlot(fixedWUTInv, 5, 98, -22));
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        if(!wirelessTerminalGUIObject.rangeCheck()) {
            if(isServer() && isValidContainer()) {
                getPlayerInv().player.sendSystemMessage(PlayerMessages.OutOfRange.get(), Util.NIL_UUID);
            }

            setValidContainer(false);
        } else {
            double powerMultiplier = AEConfig.instance().wireless_getDrainRate(wirelessTerminalGUIObject.getRange());
            try {
                Method method = super.getClass().getDeclaredMethod("setPowerMultiplier", double.class);
                method.setAccessible(true);
                method.invoke(this, powerMultiplier);
                method.setAccessible(false);
            } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {}
        }
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    @Override
    public void onContentChanged(Inventory inventory) {
        final ContainerNull cn = new ContainerNull();
        final CraftingInventory ic = new CraftingInventory(cn, 3, 3);

        for(int x = 0; x < 9; x++) {
            ic.setStack(x, craftingSlots[x].getStack());
        }

        if(currentRecipe == null || !currentRecipe.matches(ic, this.getPlayerInv().player.world)) {
            World world = this.getPlayerInv().player.world;
            currentRecipe = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, ic, world).orElse(null);
        }

        if(currentRecipe == null) {
            outputSlot.setStack(ItemStack.EMPTY);
        } else {
            final ItemStack craftingResult = currentRecipe.craft(ic);
            outputSlot.setStack(craftingResult);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void saveChanges() {}

    @Override
    public void onChangeInventory(FixedItemInv inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {}

    @Override
    public FixedItemInv getInventoryByName(String name) {
        if(name.equals("player")) {
            return new FixedInventoryVanillaWrapper(getPlayerInventory());
        } else if(name.equals("crafting")) {
            return craftingGrid;
        }
        return null;
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    @Override
    public ItemStack[] getViewCells() {
        return wirelessTerminalGUIObject.getViewCellStorage().getViewCells();
    }
}