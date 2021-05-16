package tfar.ae2wtlib.net.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.ae2wtlib.wit.WITScreen;

import java.util.function.Supplier;

public class S2CInterfaceTerminalPacket {

    private CompoundNBT nbt;

    public S2CInterfaceTerminalPacket(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public S2CInterfaceTerminalPacket(PacketBuffer buf) {
    }

    public void encode(PacketBuffer buf) {

    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        PlayerEntity player = ctx.get().getSender();

        if (player == null) return;

        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) return;

            final Screen screen = Minecraft.getInstance().currentScreen;
            if (screen instanceof WITScreen) {
                WITScreen s = (WITScreen) screen;
                if (nbt != null) s.postUpdate(nbt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}