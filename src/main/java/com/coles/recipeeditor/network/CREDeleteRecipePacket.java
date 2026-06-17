package com.coles.recipeeditor.network;

import com.coles.recipeeditor.CREMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CREDeleteRecipePacket(String recipeId) implements CustomPacketPayload {
    public static final Type<CREDeleteRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CREMod.MOD_ID, "delete_recipe"));
    public static final StreamCodec<ByteBuf, CREDeleteRecipePacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CREDeleteRecipePacket::recipeId,
        CREDeleteRecipePacket::new
    );
    @Override public Type<CREDeleteRecipePacket> type() { return TYPE; }
}
