package com.ishland.c2me.rewrites.chunk_serializer;

import com.ibm.asyncutil.util.Either;
import com.ishland.c2me.base.common.registry.SerializerAccess;
import com.ishland.c2me.rewrites.chunk_serializer.common.ChunkDataSerializer;
import com.ishland.c2me.rewrites.chunk_serializer.common.NbtWriter;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fml.common.Mod;

@Mod("c2me_rewrites_chunk_serializer")
public class TheMod {

    public TheMod() {
        if (ModuleEntryPoint.enabled) {
            SerializerAccess.registerSerializer((world, chunk) -> {
                NbtWriter nbtWriter = new NbtWriter();
                nbtWriter.start(Tag.TAG_COMPOUND);
                ChunkDataSerializer.write(world, chunk, nbtWriter);
                nbtWriter.finishCompound();
                final byte[] data = nbtWriter.toByteArray();
                nbtWriter.release();
                return Either.right(data);
            });
        }
    }
}
