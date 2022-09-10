package dev.momostudios.coldsweat.common.item;

import dev.momostudios.coldsweat.api.temperature.Temperature;
import dev.momostudios.coldsweat.api.temperature.modifier.HellLampTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.TempHelper;
import dev.momostudios.coldsweat.client.gui.tooltip.HellspringTooltip;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.core.itemgroup.ColdSweatGroup;
import dev.momostudios.coldsweat.util.config.ConfigCache;
import dev.momostudios.coldsweat.util.config.ConfigHelper;
import dev.momostudios.coldsweat.util.config.DynamicValue;
import dev.momostudios.coldsweat.util.entity.NBTHelper;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber
public class HellspringLampItem extends Item
{
    public static DynamicValue<List<Item>> VALID_FUEL = DynamicValue.of(() ->
    {
        List<Item> list = new ArrayList<>();
        for (String itemID : ItemSettingsConfig.getInstance().soulLampItems())
        {
            list.addAll(ConfigHelper.getItems(itemID));
        }
        return list;
    });
    static DynamicValue<List<String>> VALID_DIMENSIONS = DynamicValue.of(() -> new ArrayList<>(ItemSettingsConfig.getInstance().soulLampDimensions()));

    public HellspringLampItem()
    {
        super(new Properties().tab(ColdSweatGroup.COLD_SWEAT).stacksTo(1).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (entityIn instanceof Player player && !worldIn.isClientSide)
        {
            double max = ConfigCache.getInstance().maxTemp;
            TempModifier lampMod = TempHelper.getModifier(player, Temperature.Type.WORLD, HellLampTempModifier.class);
            double temp;

            // Is selected
            if ((isSelected || player.getOffhandItem() == stack)
            // Is world temp more than max
            && (temp = lampMod != null ? lampMod.getLastInput().get() : TempHelper.getTemperature(player, Temperature.Type.WORLD).get()) > max && getFuel(stack) > 0
            // Is in valid dimension
            && VALID_DIMENSIONS.get().contains(worldIn.dimension().location().toString()))
            {
                // Drain fuel
                if (player.tickCount % 5 == 0 && !(player.isCreative() || player.isSpectator()))
                {
                    addFuel(stack, -0.01d * CSMath.clamp(temp - max, 1d, 3d));

                    AABB bb = new AABB(player.getX() - 3.5, player.getY() - 3.5, player.getZ() - 3.5,
                                       player.getX() + 3.5, player.getY() + 3.5, player.getZ() + 3.5);
                    for (Player entity : worldIn.getEntitiesOfClass(Player.class, bb))
                    {
                        HellLampTempModifier modifier = TempHelper.getModifier(entity, Temperature.Type.WORLD, HellLampTempModifier.class);
                        if (modifier != null)
                            modifier.expires(modifier.getTicksExisted() + 5);
                        else
                            TempHelper.replaceModifier(entity, new HellLampTempModifier().expires(5).tickRate(5), Temperature.Type.WORLD);
                    }
                }

                // If the conditions are met, turn on the lamp
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && !stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", true);

                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_ON, SoundSource.PLAYERS, player, 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }
            // If the conditions are not met, turn off the lamp
            else
            {
                if (stack.getOrCreateTag().getInt("stateChangeTimer") <= 0 && stack.getOrCreateTag().getBoolean("isOn"))
                {
                    stack.getOrCreateTag().putInt("stateChangeTimer", 10);
                    stack.getOrCreateTag().putBoolean("isOn", false);

                    if (getFuel(stack) < 0.5)
                        setFuel(stack, 0);

                    WorldHelper.playEntitySound(ModSounds.NETHER_LAMP_OFF, SoundSource.PLAYERS, player, 1.5f, (float) Math.random() / 5f + 0.9f);
                }
            }

            // Decrement the state change timer
            NBTHelper.incrementTag(stack, "stateChangeTimer", -1, tag -> tag > 0);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged;
    }

    private void setFuel(ItemStack stack, double fuel)
    {
        stack.getOrCreateTag().putDouble("fuel", fuel);
    }
    private void addFuel(ItemStack stack, double fuel)
    {
        setFuel(stack, Math.min(64, getFuel(stack) + fuel));
    }
    private double getFuel(ItemStack stack)
    {
        return stack.getOrCreateTag().getDouble("fuel");
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> itemList)
    {
        if (this.allowdedIn(tab))
        {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().putBoolean("isOn", true);
            stack.getOrCreateTag().putDouble("fuel", 64);
            itemList.add(stack);
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack newStack, Slot slot, ClickAction action, Player player, SlotAccess slotAccess)
    {
        if (VALID_FUEL.get().contains(newStack.getItem()) && getFuel(thisStack) < 64)
        {
            int stackCount = newStack.getCount();
            newStack.shrink(64 - (int) getFuel(thisStack));
            addFuel(thisStack, stackCount);
            return true;
        }
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack)
    {
        return Optional.of(new HellspringTooltip(getFuel(stack)));
    }
}
