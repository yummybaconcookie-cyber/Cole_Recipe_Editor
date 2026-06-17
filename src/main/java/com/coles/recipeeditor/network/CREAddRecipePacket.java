package com.coles.recipeeditor.network;

import com.coles.recipeeditor.CREMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CREAddRecipePacket(String entryJson) implements CustomPacketPayload {
    public static final Type<CREAddRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CREMod.MOD_ID, "add_recipe"));
    public static final StreamCodec<ByteBuf, CREAddRecipePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CREAddRecipePacket::entryJson,
        CREAddRecipePacket::new
    );
    @Override public Type<CREAddRecipePacket> type() { return TYPE; }
}
