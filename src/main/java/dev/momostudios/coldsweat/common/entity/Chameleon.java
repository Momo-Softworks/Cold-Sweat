package dev.momostudios.coldsweat.common.entity;

import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.renderer.animation.AnimationManager;
import dev.momostudios.coldsweat.common.entity.data.CSDataSerializers;
import dev.momostudios.coldsweat.common.entity.goals.EatDroppedItemsGoal;
import dev.momostudios.coldsweat.core.init.EntityInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ChameleonEatMessage;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Mod.EventBusSubscriber
public class Chameleon extends TamableAnimal
{
    static final EntityDataAccessor<Boolean> SHEDDING = SynchedEntityData.defineId(Chameleon.class, EntityDataSerializers.BOOLEAN);
    static final EntityDataAccessor<Long> LAST_SHED = SynchedEntityData.defineId(Chameleon.class, CSDataSerializers.LONG);
    static final EntityDataAccessor<Long> HURT_TIMESTAMP = SynchedEntityData.defineId(Chameleon.class, CSDataSerializers.LONG);
    static final EntityDataAccessor<Set<UUID>> TRUSTED_PLAYERS = SynchedEntityData.defineId(Chameleon.class, CSDataSerializers.PLAYER_LIST);

    public float xRotHead = 0;
    public float yRotHead = 0;
    public float xRotLeftEye = 0;
    public float yRotLeftEye = 0;
    public float xRotRightEye = 0;
    public float yRotRightEye = 0;

    float eatAnimationTimer = 20;
    public float opacity = 1;
    float desiredTemp = 1f;
    float temperature = 0f;

    Color lastColor = Color.GREEN;
    Color color = Color.GREEN;

    public Chameleon(EntityType<Chameleon> type, Level Level)
    {
        super(type, Level);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new EatDroppedItemsGoal(this, List.of(Items.SPIDER_EYE)));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.5D, true));
        this.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Silverfish.class, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.5D, Ingredient.of(Items.SPIDER_EYE), false));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(SHEDDING, false);
        this.entityData.define(LAST_SHED, 0L);
        this.entityData.define(HURT_TIMESTAMP, 0L);
        this.entityData.define(TRUSTED_PLAYERS, new HashSet<>());
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source)
    {
        if (this.getVehicle() instanceof Player player)
        {
            if (source.equals(DamageSource.IN_WALL) || source.equals(DamageSource.FALL)) return true;
            return player.isInvulnerableTo(source);
        }
        return super.isInvulnerableTo(source);
    }

    @NotNull
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand)
    {
        if (this.isPlayerTrusted(player) && player.getPassengers().isEmpty())
        {
            this.startRiding(player);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @SubscribeEvent
    public static void setHeight(EntityEvent.Size event)
    {
        if (event.getEntity() instanceof Chameleon)
        {
            event.setNewEyeHeight(0.35F);
        }
    }

    public int getTimeToShed()
    {
        return 1200;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob parent)
    {
        return EntityInit.CHAMELEON.get().create(level);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return ModSounds.CHAMELEON_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return ModSounds.CHAMELEON_HURT;
    }

    public boolean isWalking()
    {
        return new Vec2((float) getDeltaMovement().x, (float) getDeltaMovement().z).length() > 0.01;
    }

    public long getHurtTimestamp()
    {
        return this.entityData.get(HURT_TIMESTAMP);
    }

    public void setHurtTimestamp(long hurtTimestamp)
    {
        this.entityData.set(HURT_TIMESTAMP, hurtTimestamp);
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
    }

    public Color getLastColor()
    {
        return lastColor;
    }

    public void setLastColor(Color lastColor)
    {
        this.lastColor = lastColor;
    }

    @Override
    public float tickHeadTurn(float p_21260_, float p_21261_)
    {
        if (this.isWalking())
        {
            this.yBodyRot = this.getYRot();
            this.rotateHeadIfNecessary();
        }
        if (Math.abs(yHeadRot - yBodyRot) > 45)
        {
            rotateBodyIfNecessary();
        }
        return p_21261_;
    }

    @Override
    public void tick()
    {
        super.tick();

        // Tick eat animation
        if (this.eatAnimationTimer < getEatAnimLength())
            this.eatAnimationTimer++;

        // Tick shedding
        if (!this.level.isClientSide)
        {
            boolean shedding = this.isShedding();
            if (this.tickCount % 20 == 0 && this.tickCount - this.getLastShed() > 24000 && !shedding && this.random.nextInt(30) == 1)
            {
                this.setShedding(true);
                this.setLastShed(this.tickCount);
            }

            if (shedding && this.tickCount - this.getLastShed() > this.getTimeToShed())
            {
                WorldHelper.spawnItemOnEntity(this, new ItemStack(ModItems.CHAMELEON_MOLT));
                WorldHelper.playEntitySound(SoundEvents.BEEHIVE_EXIT, this, this.getSoundSource(), 1, (float) (Math.random() * 0.4 + 0.8));
                this.setShedding(false);
            }
        }

        // Follow the player's movements when riding
        if (this.getVehicle() instanceof Player player)
        {
            float playerHeadYaw = player.yHeadRot;
            this.yBodyRot = playerHeadYaw;
            this.yHeadRot = CSMath.clamp(this.yHeadRot, playerHeadYaw - 60, playerHeadYaw + 60);
        }

        // Control temperature
        if (this.tickCount % 20 == 0 && this.level.isClientSide)
        {
            // Use the player's temperature if mounted
            ConfigSettings config = ConfigSettings.getInstance();
            this.desiredTemp = (float) CSMath.clamp(Temperature.get(this, Temperature.Type.WORLD), config.minTemp, config.maxTemp);
        }
        this.setTemperature(this.getTemperature() + (this.desiredTemp - this.getTemperature()) * 0.03f);
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if (source.getEntity() != null && !this.isInvulnerableTo(source))
        {
            this.setHurtTimestamp(this.tickCount);
        }
        return super.hurt(source, amount);
    }

    @Override
    public double getMyRidingOffset()
    {
        return 0.5;
    }

    public static boolean canSpawn(EntityType<Chameleon> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, Random random)
    {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
    }

    private void rotateBodyIfNecessary()
    {
        this.yBodyRot = Mth.rotateIfNecessary(this.yBodyRot, this.yHeadRot, (float)this.getMaxHeadYRot());
    }

    private void rotateHeadIfNecessary()
    {
        this.yHeadRot = Mth.rotateIfNecessary(this.yHeadRot, this.yBodyRot, (float)this.getMaxHeadYRot());
    }

    public void eatAnimation()
    {
        if (this.eatAnimationTimer >= getEatAnimLength())
        {
            if (!this.level.isClientSide)
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new ChameleonEatMessage(this.getId()));
            }
            else
            {
                AnimationManager.ANIMATION_TIMERS.put(this, 0f);
            }
            this.eatAnimationTimer = 0;
        }
    }

    public float getEatTimer()
    {
        return this.eatAnimationTimer;
    }

    public float getEatAnimLength()
    {
        return 10;
    }

    public boolean isShedding()
    {
        return this.entityData.get(SHEDDING);
    }

    public void setShedding(boolean shedding)
    {
        this.entityData.set(SHEDDING, shedding);
    }

    public Set<UUID> getTrustedPlayers()
    {
        return this.entityData.get(TRUSTED_PLAYERS);
    }

    public void addTrustedPlayer(UUID player)
    {
        this.getTrustedPlayers().add(player);
    }

    public void removeTrustedPlayer(UUID player)
    {
        this.getTrustedPlayers().remove(player);
    }

    public boolean isPlayerTrusted(Player player)
    {
        return this.getTrustedPlayers().contains(player.getUUID());
    }

    public long getLastShed()
    {
        return this.entityData.get(LAST_SHED);
    }

    public void setLastShed(long lastShed)
    {
        this.entityData.set(LAST_SHED, lastShed);
    }

    public float getTemperature()
    {
        return this.temperature;
    }

    public void setTemperature(float temperature)
    {
        this.temperature = temperature;
    }

    @Override
    public void load(CompoundTag nbt)
    {
        this.setShedding(nbt.getBoolean("Shedding"));
        this.setLastShed(nbt.getLong("LastShed"));
        this.setHurtTimestamp(nbt.getLong("HurtTimestamp"));
        this.getTrustedPlayers().clear();
        nbt.getList("TrustedPlayers", 8).forEach((uuid) -> this.addTrustedPlayer(UUID.fromString(uuid.getAsString())));
        super.load(nbt);
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag nbt)
    {
        nbt.putBoolean("Shedding", this.isShedding());
        nbt.putLong("LastShed", this.getLastShed());
        nbt.putLong("HurtTimestamp", this.getHurtTimestamp());
        ListTag trustedPlayers = new ListTag();
        for (UUID uuid : this.getTrustedPlayers())
        {
            trustedPlayers.add(StringTag.valueOf(uuid.toString()));
        }
        nbt.put("TrustedPlayers", trustedPlayers);
        return super.saveWithoutId(nbt);
    }

    public enum Color
    {
        GREEN,
        RED,
        BLUE
    }
}
