package com.coles.recipeeditor.network;

import com.coles.recipeeditor.CREMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CREReloadPacket() implements CustomPacketPayload {
    public static final Type<CREReloadPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CREMod.MOD_ID, "reload"));
    public static final StreamCodec<ByteBuf, CREReloadPacket> STREAM_CODEC = StreamCodec.of((buf, p) -> {}, buf -> new CREReloadPacket());
    @Override public Type<CREReloadPacket> type() { return TYPE; }
}
