package com.coles.recipeeditor.network;

import com.coles.recipeeditor.CREMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CRESavePacket() implements CustomPacketPayload {
    public static final Type<CRESavePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CREMod.MOD_ID, "save"));
    public static final StreamCodec<ByteBuf, CRESavePacket> STREAM_CODEC = StreamCodec.of((buf, p) -> {}, buf -> new CRESavePacket());
    @Override public Type<CRESavePacket> type() { return TYPE; }
}
