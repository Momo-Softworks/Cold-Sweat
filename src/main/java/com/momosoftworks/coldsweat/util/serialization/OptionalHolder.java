package com.momosoftworks.coldsweat.util.serialization;

import com.mojang.serialization.Codec;
import com.momosoftworks.coldsweat.api.event.core.registry.FillOptionalHoldersEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

public class OptionalHolder<T>
{
    private final ResourceKey<T> key;
    private Holder<T> value = null;

    public OptionalHolder(ResourceKey<T> key)
    {   this.key = key;
        MinecraftForge.EVENT_BUS.register(this);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new ClientHandler()));
    }

    public static <T> Codec<OptionalHolder<T>> getCodec(ResourceKey<Registry<T>> key)
    {   return ResourceKey.codec(key).xmap(OptionalHolder::new, OptionalHolder::key);
    }

    public ResourceKey<T> key()
    {   return key;
    }

    public Holder<T> get()
    {   return Optional.ofNullable(value).orElseThrow();
    }

    public Optional<Holder<T>> value()
    {   return Optional.ofNullable(value);
    }

    public void setValue(Holder<T> value)
    {   this.value = value;
    }

    public static <T> OptionalHolder<T> ofHolder(Holder<T> holder)
    {
        OptionalHolder<T> opt = new OptionalHolder<>(holder.unwrapKey().orElseThrow());
        opt.setValue(holder);
        return opt;
    }

    public boolean is(Holder<T> holder)
    {   return value != null && value.equals(holder);
    }

    @SubscribeEvent
    public void onRegistriesLoaded(FillOptionalHoldersEvent event)
    {
        event.registryAccess().<T>registry(ResourceKey.createRegistryKey(this.key().registry())).ifPresent(registry ->
        {   registry.getHolder(this.key()).ifPresent(this::setValue);
        });
    }

    @SubscribeEvent
    public void onClosed(ServerStoppedEvent event)
    {   this.value = null;
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public class ClientHandler
    {
        @SubscribeEvent
        public void onClosed(ClientPlayerNetworkEvent.LoggingOut event)
        {
            if (event.getPlayer() == null) return;
            OptionalHolder.this.value = null;
            MinecraftForge.EVENT_BUS.unregister(OptionalHolder.this);
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    @Override
    public String toString()
    {
        return "OptionalHolder{" +
                "key=" + key +
                ", value=" + (value != null ? value.value() : null) +
                '}';
    }
}
