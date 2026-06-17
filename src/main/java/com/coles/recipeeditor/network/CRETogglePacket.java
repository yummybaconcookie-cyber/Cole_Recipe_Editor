package com.coles.recipeeditor.network;

import com.coles.recipeeditor.CREMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CRETogglePacket(String recipeId, boolean enabled) implements CustomPacketPayload {
    public static final Type<CRETogglePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CREMod.MOD_ID, "toggle"));
    public static final StreamCodec<ByteBuf, CRETogglePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CRETogglePacket::recipeId,
        ByteBufCodecs.BOOL, CRETogglePacket::enabled,
        CRETogglePacket::new
    );
    @Override public Type<CRETogglePacket> type() { return TYPE; }
}
