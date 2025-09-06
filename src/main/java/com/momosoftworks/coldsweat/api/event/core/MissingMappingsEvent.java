package com.momosoftworks.coldsweat.api.event.core;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.IRegistryExtension;

public class MissingMappingsEvent extends Event
{
    public <T> Builder<T> remap(IRegistryExtension<T> registry)
    {   return new Builder<>(registry);
    }

    public static class Builder<T>
    {
        private final IRegistryExtension<T> registry;
        private String oldNamespace;
        private String newNamespace;
        private String oldPath;
        private String newPath;

        public Builder(IRegistryExtension<T> registry)
        {   this.registry = registry;
        }

        private ResourceLocation oldId()
        {   return ResourceLocation.fromNamespaceAndPath(oldNamespace, oldPath);
        }

        private ResourceLocation newId()
        {   return ResourceLocation.fromNamespaceAndPath(newNamespace, newPath);
        }

        public Builder<T> namespace(String newNamespace)
        {   this.newNamespace = newNamespace;
            this.oldNamespace = newNamespace;
            return this;
        }

        public Builder<T> from(ResourceLocation oldId)
        {   this.oldNamespace = oldId.getNamespace();
            this.oldPath = oldId.getPath();
            return this;
        }

        public Builder<T> from(String oldId)
        {
            if (oldId.contains(":"))
            {   String[] split = oldId.split(":");
                this.oldNamespace = split[0];
                this.oldPath = split[1];
            }
            else
            {   this.oldPath = oldId;
            }
            return this;
        }

        public void to(ResourceLocation newId)
        {   this.registry.addAlias(this.oldId(), newId);
        }

        public void to(DeferredHolder<T, ?> newEntry)
        {   this.registry.addAlias(this.oldId(), newEntry.getId());
        }

        public void to(String newId)
        {
            if (this.oldNamespace == null || this.newNamespace == null)
                throw new IllegalStateException("Namespace must be set before using path-only remap.");
            this.newPath = newId;
            this.registry.addAlias(this.oldId(), this.newId());
        }
    }
}
