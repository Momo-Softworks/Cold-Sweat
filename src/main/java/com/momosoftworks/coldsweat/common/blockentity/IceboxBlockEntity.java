package com.momosoftworks.coldsweat.common.blockentity;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.modifier.BlockInsulationTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.block.IceboxBlock;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.temperature.ITemperatureCap;
import com.momosoftworks.coldsweat.common.container.IceboxContainer;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.event.TaskScheduler;
import com.momosoftworks.coldsweat.core.init.*;
import com.momosoftworks.coldsweat.core.network.message.BlockDataUpdateMessage;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class IceboxBlockEntity extends HearthBlockEntity implements MenuProvider, WorldlyContainer
{
    public static int[] WATERSKIN_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static int[] FUEL_SLOT = {0};

    List<ServerPlayer> usingPlayers = new ArrayList<>();

    public IceboxBlockEntity(BlockPos pos, BlockState state)
    {   super(ModBlockEntities.ICEBOX.value(), pos, state);
        TaskScheduler.schedule(this::checkForSmokestack, 5);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {   return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sendUpdatePacket()
    {
        // Remove the players that aren't interacting with this block anymore
        usingPlayers.removeIf(player -> !(player.containerMenu instanceof IceboxContainer iceboxContainer && iceboxContainer.te == this));

        // Send data to all players with this block's menu open
        for (ServerPlayer player : usingPlayers)
        {   PacketDistributor.sendToPlayer(player, new BlockDataUpdateMessage(this));
        }
    }

    @Override
    protected Component getDefaultName()
    {   return Component.translatable("container." + ColdSweat.MOD_ID + ".icebox");
    }

    @Override
    public Component getDisplayName()
    {   return this.getCustomName() != null ? this.getCustomName() : this.getDefaultName();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T te)
    {
        if (te instanceof IceboxBlockEntity iceboxTE)
        {   iceboxTE.tick(level, state, pos);
        }
    }

    public void tick(Level level, BlockState state, BlockPos pos)
    {
        super.tick(level, pos);

        if (getFuel() > 0)
        {
            // Set state to frosted
            if (!state.getValue(IceboxBlock.FROSTED))
                level.setBlock(pos, state.setValue(IceboxBlock.FROSTED, true), 3);

            // Cool down waterskins
            if (ticksExisted % (20 / ConfigSettings.TEMP_RATE.get()) == 0)
            {
                boolean hasItemStacks = false;
                for (int i = 1; i < 10; i++)
                {
                    ItemStack stack = getItem(i);
                    double itemTemp = stack.getOrDefault(ModItemComponents.WATER_TEMPERATURE.value(), 0d);

                    if (stack.is(ModItems.FILLED_WATERSKIN) && itemTemp > -50)
                    {   hasItemStacks = true;
                        stack.set(ModItemComponents.WATER_TEMPERATURE.value(), Math.max(-50, itemTemp - 1));
                    }
                }
                if (hasItemStacks) setFuel(getFuel() - 1);
            }
        }
        // if no fuel, set state to unfrosted
        else if (state.getValue(IceboxBlock.FROSTED))
        {   level.setBlock(pos, state.setValue(IceboxBlock.FROSTED, false), 3);
        }
    }

    @Override
    public int getMaxPaths()
    {   return 1500;
    }

    @Override
    public int getSpreadRange()
    {   return 16;
    }

    @Override
    public int getMaxInsulationLevel()
    {   return 5;
    }

    @Override
    public SoundEvent getFuelDepleteSound()
    {   return ModSounds.ICEBOX_DEPLETE.value();
    }

    @Override
    public boolean hasSmokeStack()
    {   return this.hasSmokestack;
    }

    @Override
    protected void trySpreading(int pathCount, int firstIndex, int lastIndex)
    {
        if (this.hasSmokestack)
        {   super.trySpreading(pathCount, firstIndex, lastIndex);
        }
    }

    @Override
    void insulatePlayer(Player player)
    {
        // Apply the insulation effect
        if (!shouldUseColdFuel)
        {
            ITemperatureCap cap = EntityTempManager.getTemperatureCap(player);
            double temp = cap.getTrait(Temperature.Trait.WORLD);
            double min = cap.getTrait(Temperature.Trait.FREEZING_POINT);
            double max = cap.getTrait(Temperature.Trait.BURNING_POINT);

            // If the player is habitable, check the input temperature reported by their HearthTempModifier (if they have one)
            if (CSMath.betweenInclusive(temp, min, max))
            {
                // Find the player's HearthTempModifier
                Optional<? extends TempModifier> modifier = Temperature.getModifier(player, Temperature.Trait.WORLD, BlockInsulationTempModifier.class);
                // If they have one, refresh it
                if (modifier.isPresent())
                {
                    if (modifier.get().getExpireTime() - modifier.get().getTicksExisted() > 20)
                    {   return;
                    }
                    temp = modifier.get().getLastInput();
                }
                // This means the player is not insulated, and they are habitable without it
                else return;
            }

            // Tell the icebox to use cold fuel
            shouldUseColdFuel |= this.getColdFuel() > 0 && temp > max;
        }
        if (shouldUseColdFuel)
        {   int maxEffect = this.getMaxInsulationLevel() - 1;
            int effectLevel = (int) Math.min(maxEffect, (insulationLevel / (double) this.getInsulationTime()) * maxEffect);
            player.addEffect(new MobEffectInstance(ModEffects.INSULATED, 120, effectLevel, false, false, true));
        }
    }

    @Override
    public int getItemFuel(ItemStack item)
    {   return CSMath.getIfNotNull(ConfigSettings.ICEBOX_FUEL.get().get(item.getItem()),
                                   fuel -> fuel.test(item) ? fuel.value() : 0,
                                   0).intValue();
    }

    @Override
    protected void storeFuel(ItemStack stack, int amount)
    {
        if (this.getFuel() < this.getMaxFuel() - Math.abs(amount) * 0.75)
        {
            if (!stack.hasCraftingRemainingItem() || stack.getCount() > 1)
            {   int consumeCount = Math.min((int) Math.floor((this.getMaxFuel() - this.getFuel()) / (double) Math.abs(amount)), stack.getCount());
                stack.shrink(consumeCount);
                addFuel(amount * consumeCount);
            }
            else
            {   this.setItem(0, stack.getCraftingRemainingItem());
                addFuel(amount);
            }
        }
    }

    public int getFuel()
    {   return this.getColdFuel();
    }

    public void setFuel(int amount)
    {   this.setColdFuel(amount, true);
    }

    @Override
    public void setColdFuel(int amount, boolean update)
    {   super.setColdFuel(amount, update);
        this.sendUpdatePacket();
    }

    @Override
    public void addFuel(int amount)
    {   this.setColdFuelAndUpdate(this.getColdFuel() + amount);
    }

    @Override
    protected boolean isFuelChanged()
    {   return this.ticksExisted % 10 == 0;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInv)
    {
        // Track the players using this block
        if (playerInv.player instanceof ServerPlayer serverPlayer)
        {   usingPlayers.add(serverPlayer);
        }
        return new IceboxContainer(id, playerInv, this);
    }

    @Override
    protected void tickParticles()
    {
        if (this.hasSmokestack)
        {   super.tickParticles();
        }
    }

    @Override
    public ParticleOptions getAirParticle()
    {   return ModParticleTypes.GROUND_MIST.get();
    }

    @Override
    public void spawnAirParticle(int x, int y, int z, Random rand)
    {
        BlockPos pos = new BlockPos(x, y, z);
        boolean onGround = !this.level.getBlockState(pos.below()).isAir();
        if (rand.nextFloat() > (spreading ? 0.016f : 0.032f))
        {   return;
        }

        float xr = rand.nextFloat();
        float yr = onGround ? 0.1f : rand.nextFloat();
        float zr = rand.nextFloat();
        float xm = rand.nextFloat() / 20 - 0.025f;
        float zm = rand.nextFloat() / 20 - 0.025f;

        level.addParticle(onGround ? ModParticleTypes.GROUND_MIST.get()
                                   : ModParticleTypes.MIST.get(), false, x + xr, y + yr, z + zr, xm, 0, zm);
    }

    @Override
    public int getContainerSize()
    {   return 10;
    }

    @Override
    public int[] getSlotsForFace(Direction dir)
    {   return dir.getAxis() == Direction.Axis.Y ? WATERSKIN_SLOTS : FUEL_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction)
    {
        if (slot == 0)
            return this.getItemFuel(stack) != 0;
        else return stack.is(ModItems.WATERSKIN) || stack.is(ModItems.FILLED_WATERSKIN);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction)
    {   return true;
    }
}
