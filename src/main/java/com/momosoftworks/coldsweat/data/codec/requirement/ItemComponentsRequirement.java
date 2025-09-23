package com.momosoftworks.coldsweat.data.codec.requirement;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public record ItemComponentsRequirement(CompoundTag components)
{
    public static final Codec<ItemComponentsRequirement> CODEC = CompoundTag.CODEC.xmap(ItemComponentsRequirement::new, ItemComponentsRequirement::components);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemComponentsRequirement> STREAM_CODEC = StreamCodec.of(
            (buf, requirement) -> buf.writeNbt(requirement.components()),
            (buf) -> new ItemComponentsRequirement(buf.readNbt())
    );

    public ItemComponentsRequirement()
    {   this(new CompoundTag());
    }

    public boolean test(ItemStack stack)
    {   return this.components().isEmpty() || this.test(stack.getComponentsPatch(), RegistryHelper.getRegistryAccess());
    }

    public boolean test(@Nullable DataComponentPatch components, HolderLookup.Provider registryAccess)
    {
        CompoundTag serialized = (CompoundTag) DataComponentPatch.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, registryAccess), components).result().orElse(new CompoundTag());
        return NbtRequirement.compareNbt(this.components, serialized);
    }

    public static ItemComponentsRequirement parse(String data)
    {
        if (!data.startsWith("{"))
        {   data = "{" + data + "}";
        }
        return new ItemComponentsRequirement(NBTHelper.parseCompoundNbt(data));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {   return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {   return false;
        }

        ItemComponentsRequirement that = (ItemComponentsRequirement) obj;

        return components.equals(that.components);
    }

    @Override
    public String toString()
    {
        return "ItemComponents{" +
                "components=" + components +
                '}';
    }
}
