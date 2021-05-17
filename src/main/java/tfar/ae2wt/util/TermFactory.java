package tfar.ae2wt.util;

import appeng.container.ContainerLocator;
import appeng.core.localization.GuiText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import tfar.ae2wt.wirelesscraftingterminal.WCTGuiObject;
import tfar.ae2wt.wirelessinterfaceterminal.WITContainer;
import tfar.ae2wt.wirelessinterfaceterminal.WITGuiObject;

import javax.annotation.Nullable;

public class TermFactory implements INamedContainerProvider {

    private final WCTGuiObject obj;
    private final ContainerLocator locator;

    TermFactory(WCTGuiObject obj, ContainerLocator locator) {

        this.obj = obj;
        this.locator = locator;
    }

    @Override
    public ITextComponent getDisplayName() {
        return  GuiText.Terminal.text();
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {

        WirelessCraftAmountContainer c = new WirelessCraftAmountContainer(p_createMenu_1_, p_createMenu_2_, obj);
        // Set the original locator on the opened server-side container for it to more
        // easily remember how to re-open after being closed.
        c.setLocator(locator);
        return c;
    }
}
