package com.momosoftworks.coldsweat.mixin_plugin;

import com.google.common.collect.ImmutableMap;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ColdSweatMixinPlugin implements IMixinConfigPlugin
{
    private static final String MIXIN_PACKAGE = "com.momosoftworks.coldsweat.mixin.";
    private static final String COMPAT_MIXIN_PACKAGE = "com.momosoftworks.coldsweat.mixin.compat.";


    private static final Map<String, Supplier<Boolean>> CONDITIONS = ImmutableMap.of(
            COMPAT_MIXIN_PACKAGE + "MixinCreateOverlay", () -> modLoaded("create"),
            COMPAT_MIXIN_PACKAGE + "MixinCreateConnect", () -> modLoaded("create"),
            COMPAT_MIXIN_PACKAGE + "MixinSpoiledIcebox", () -> modLoaded("spoiled"),
            COMPAT_MIXIN_PACKAGE + "MixinSereneIceMelt", () -> modLoaded("sereneseasons")
    );

    @Override
    public void onLoad(String mixinPackage)
    {

    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        return CONDITIONS.getOrDefault(mixinClassName, () -> true).get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
    {

    }

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
    {

    }

    public static boolean modLoaded(String modId)
    {   return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }
}
