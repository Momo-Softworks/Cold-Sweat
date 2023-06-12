package dev.momostudios.coldsweat.common.entity;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.client.renderer.animation.AnimationManager;
import dev.momostudios.coldsweat.common.entity.data.edible.ChameleonEdibles;
import dev.momostudios.coldsweat.common.entity.data.edible.Edible;
import dev.momostudios.coldsweat.common.entity.goals.EatObjectsGoal;
import dev.momostudios.coldsweat.common.entity.goals.LazyLookGoal;
import dev.momostudios.coldsweat.core.init.EntityInit;
import dev.momostudios.coldsweat.core.network.ColdSweatPacketHandler;
import dev.momostudios.coldsweat.core.network.message.ChameleonEatMessage;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import dev.momostudios.coldsweat.util.registries.ModItems;
import dev.momostudios.coldsweat.util.registries.ModSounds;
import dev.momostudios.coldsweat.util.registries.ModTags;
import dev.momostudios.coldsweat.util.world.WorldHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

@Mod.EventBusSubscriber
public class ChameleonEntity extends Animal
{
    static Method GET_DATA_ITEM = ObfuscationReflectionHelper.findMethod(SynchedEntityData.class, "m_135379_", EntityDataAccessor.class);
    static
    {   GET_DATA_ITEM.setAccessible(true);
    }

    static final EntityDataAccessor<Boolean> SHEDDING = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.BOOLEAN);
    static final EntityDataAccessor<Integer> LAST_SHED = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    static final EntityDataAccessor<Integer> HURT_TIMESTAMP = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    static final EntityDataAccessor<CompoundTag> TRUSTED_PLAYERS = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.COMPOUND_TAG);
    static final EntityDataAccessor<BlockPos> TRACKING_POS = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.BLOCK_POS);
    static final EntityDataAccessor<Integer> EAT_TIMESTAMP = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);
    static final EntityDataAccessor<Float> TEMPERATURE = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.FLOAT);
    static final EntityDataAccessor<CompoundTag> EDIBLE_COOLDOWNS = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.COMPOUND_TAG);
    static final EntityDataAccessor<Boolean> SEARCHING = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.BOOLEAN);
    static final EntityDataAccessor<Integer> AGE_SECS = SynchedEntityData.defineId(ChameleonEntity.class, EntityDataSerializers.INT);

    public float xRotHead = 0;
    public float yRotHead = 0;
    public float xRotLeftEye = 0;
    public float yRotLeftEye = 0;
    public float xRotRightEye = 0;
    public float yRotRightEye = 0;
    public float xRotTail = 0;
    public float tailPhase = 0;

    float eatAnimationTimer = 0;
    public float opacity = 1;
    float desiredTemp = 1f;

    public ChameleonEntity(EntityType<ChameleonEntity> type, Level Level)
    {
        super(type, Level);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
        this.goalSelector.addGoal(2, new EatObjectsGoal(this, List.of(EntityType.SILVERFISH)));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25, Ingredient.of(ModTags.Items.CHAMELEON_TAMING), false));
        this.goalSelector.addGoal(5, new LazyLookGoal(this));
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
        this.entityData.define(LAST_SHED, 0);
        this.entityData.define(HURT_TIMESTAMP, 0);
        this.entityData.define(TRUSTED_PLAYERS, new CompoundTag());
        this.entityData.define(TRACKING_POS, BlockPos.ZERO);
        this.entityData.define(EAT_TIMESTAMP, 0);
        this.entityData.define(TEMPERATURE, 0f);
        this.entityData.define(EDIBLE_COOLDOWNS, new CompoundTag());
        this.entityData.define(SEARCHING, false);
        this.entityData.define(AGE_SECS, 0);
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source)
    {
        if (this.getVehicle() instanceof Player player)
        {
            if (source.equals(DamageSource.IN_WALL) || source.equals(DamageSource.FALL)) return true;
            return player.isCreative() || player.isInvulnerableTo(source);
        }
        return super.isInvulnerableTo(source);
    }

    @NotNull
    @Override
    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand)
    {
        if (this.isPlayerTrusted(player) && player.getPassengers().isEmpty())
        {
            this.startRiding(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public static void setHeight(EntityEvent.Size event)
    {
        if (event.getEntity() instanceof ChameleonEntity chameleon)
        {
            event.setNewEyeHeight(0.35F);
            if (chameleon.isBaby())
            {   event.setNewEyeHeight(0.25F);
                event.setNewSize(EntityDimensions.fixed(0.65f, 0.5f));
            }
            else event.setNewEyeHeight(0.35F);
        }
    }

    public int getTimeToShed()
    {
        return 600;
    }

    public int getEatAnimLength()
    {
        return 6;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob parent)
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
    protected SoundEvent getHurtSound(@NotNull DamageSource source)
    {
        return ModSounds.CHAMELEON_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound()
    {
        return ModSounds.CHAMELEON_DEATH;
    }

    @Override
    public void playAmbientSound()
    {
        SoundEvent soundevent = this.getAmbientSound();
        if (!this.level.isClientSide && soundevent != null && !this.isSearching())
        {
            // This method plays a sound that actually follows the entity
            WorldHelper.playEntitySound(soundevent, this, this.getSoundSource(), this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    protected void playHurtSound(@NotNull DamageSource damageSource)
    {
        SoundEvent soundevent = this.getHurtSound(damageSource);
        if (soundevent != null)
        {
            // This method plays a sound that actually follows the entity
            WorldHelper.playEntitySound(soundevent, this, this.getSoundSource(), this.getSoundVolume(), this.getVoicePitch());
        }
    }

    public boolean isWalking()
    {
        return new Vec2((float) getDeltaMovement().x, (float) getDeltaMovement().z).length() > 0.01;
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
    public int getHeadRotSpeed()
    {   return 20;
    }

    @Override
    public void tick()
    {
        super.tick();

        // Tick eat animation
        if (this.eatAnimationTimer > 0)
            this.eatAnimationTimer--;

        // Age
        if (!this.level.isClientSide && this.tickCount % 20 == 0)
        {   this.setAgeSecs(this.getAgeSecs() + 1);
        }

        // Tick shedding
        if (!this.level.isClientSide)
        {
            boolean shedding = this.isShedding();
            if (this.tickCount % 20 == 0 && this.getAgeSecs() * 20 - this.getLastShed() > 24000 && !shedding && this.random.nextInt(30) == 1)
            {
                this.setShedding(true);
                this.setLastShed(this.getAgeSecs() * 20);
            }

            if (shedding && this.getAgeSecs() * 20 - this.getLastShed() > this.getTimeToShed())
            {
                WorldHelper.entityDropItem(this, new ItemStack(ModItems.CHAMELEON_MOLT, this.random.nextInt(3) + 1));
                WorldHelper.playEntitySound(ModSounds.CHAMELEON_SHED, this, this.getSoundSource(), 1, this.getVoicePitch());
                this.setLastShed(this.getAgeSecs() * 20);
                this.setShedding(false);
            }
        }

        // Follow the player's movements when riding
        if (this.getVehicle() instanceof Player player)
        {
            float playerHeadYaw = player.yHeadRot;
            this.yHeadRot = CSMath.clamp(this.yHeadRot, playerHeadYaw - 50, playerHeadYaw + 50);
            this.yBodyRot = playerHeadYaw;
        }

        // Control temperature
        if (this.tickCount % 20 == 0)
        {   // Use the player's temperature if mounted
            this.desiredTemp = (float) CSMath.clamp(Temperature.get(this, Temperature.Type.WORLD), ConfigSettings.MIN_TEMP.get(), ConfigSettings.MAX_TEMP.get());
        }
        this.setTemperature(this.getTemperature() + (this.desiredTemp - this.getTemperature()) * 0.03f);

        // Handle dismounting
        if (this.getVehicle() instanceof Player player)
        {
            CompoundTag data = player.getPersistentData();
            if (player.isCrouching())
            {
                if (!data.getBoolean("Sneaking"))
                {
                    if (player.tickCount - data.getLong("LastSneak") < 8)
                        data.putInt("SneakCount", data.getInt("SneakCount") + 1);
                    else data.putInt("SneakCount", 1);
                    data.putLong("LastSneak", player.tickCount);
                    data.putBoolean("Sneaking", true);

                    if (data.getInt("SneakCount") >= 2)
                    {
                        this.stopRiding();
                        data.putInt("SneakCount", 0);
                    }
                }
            }
            else data.putBoolean("Sneaking", false);
        }

        // Spawn particles if tracking a position. After 5 minutes, clear the position
        if (this.tickCount % 5 == 0 && this.isTracking())
        {
            if (this.random.nextDouble() < 0.3)
            {
                WorldHelper.spawnParticle(this.level, ParticleTypes.HAPPY_VILLAGER,
                        this.getX() + Math.random() - 0.5, this.getY() + Math.random() - 0.5 + this.getBbHeight() / 2, this.getZ() + Math.random() - 0.5,
                        0.01, 0.01, 0.01);
            }

            if (this.tickCount % 20 == 0 && (this.tickCount - this.getEatTimestamp() > 6000
            || Math.sqrt(Math.pow(this.getX() - this.getTrackingPos().getX(), 2) + Math.pow(this.getZ() - this.getTrackingPos().getZ(), 2)) < 20))
            {
                // Award nearby players the "chameleon_find_biome" advancement
                if (this.getServer() != null)
                {
                    Advancement advancement = this.getServer().getAdvancements().getAdvancement(new ResourceLocation(ColdSweat.MOD_ID, "chameleon_find_biome"));
                    for (ServerPlayer player : this.level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(20)))
                    {
                        if (advancement != null)
                        {
                            if (player.getAdvancements().getOrStartProgress(advancement).isDone())
                                continue;
                            player.getAdvancements().award(advancement, "requirement");
                        }
                    }
                    this.clearTrackingPos();
                }
            }
        }

        // Tick cooldowns
        if (!level.isClientSide)
        {
            CompoundTag cooldowns = this.getCooldowns();
            for (String tag : cooldowns.getAllKeys())
            {
                int time = cooldowns.getInt(tag);
                if (time > 0)
                    cooldowns.putInt(tag, time - 1);
            }
            this.entityData.set(EDIBLE_COOLDOWNS, cooldowns);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if (source.getEntity() != null && !this.isInvulnerableTo(source))
        {   this.setHurtTimestamp(this.tickCount);
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel p_20118_, ITeleporter teleporter)
    {   this.clearTrackingPos();
        return super.changeDimension(p_20118_, teleporter);
    }

    public void onEatEntity(Entity entity)
    {
        if (!level.isClientSide)
        {
            if (entity instanceof ItemEntity itemEntity)
            {
                if (this.isTamingItem(itemEntity.getItem()))
                {
                    Player player = itemEntity.getThrower() != null ? this.level.getPlayerByUUID(itemEntity.getThrower()) : null;
                    if (player != null && !this.isPlayerTrusted(player))
                    {
                        if (player.isCreative() || Math.random() < 0.3)
                        {   this.setPersistenceRequired();
                            this.addTrustedPlayer(itemEntity.getThrower());
                            WorldHelper.spawnParticleBatch(this.level, ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 1, 1, 1, 6, 0.01);
                        }
                        else
                        {   WorldHelper.spawnParticleBatch(this.level, ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 1, 1, 1, 6, 0.01);
                        }
                    }
                }
                ChameleonEdibles.getEdible(itemEntity.getItem().getItem()).ifPresent(edible ->
                {
                    if (edible.onEaten(this, itemEntity) == Edible.Result.SUCCESS)
                    {   this.setCooldown(edible, edible.getCooldown());
                    }
                    else
                    {   this.setCooldown(edible, edible.getCooldown() / 4);
                    }
                });
            }
            this.setEatTimestamp(this.tickCount);
        }
    }

    @Override
    public double getMyRidingOffset()
    {
        return this.getVehicle() instanceof Player player
               ? player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HOGLIN_HEADPIECE)
                    ? 0.65 : 0.5
               : 0;
    }

    public static boolean canSpawn(EntityType<ChameleonEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random)
    {
        BlockState state = level.getBlockState(pos.below());
        return state.is(BlockTags.ANIMALS_SPAWNABLE_ON)
            || state.is(BlockTags.SAND)
            || state.is(BlockTags.LEAVES)
            || state.is(BlockTags.TERRACOTTA)
            && level.getBrightness(LightLayer.SKY, pos) > 12;
    }

    private void rotateBodyIfNecessary()
    {
        this.yBodyRot = Mth.rotateIfNecessary(this.yBodyRot, this.yHeadRot, (float)this.getMaxHeadYRot());
    }

    private void rotateHeadIfNecessary()
    {
        this.yHeadRot = Mth.rotateIfNecessary(this.yHeadRot, this.yBodyRot, (float)this.getMaxHeadYRot());
    }

    @Override
    public int getMaxHeadYRot()
    {
        return 60;
    }

    public void eatAnimation()
    {
        if (this.eatAnimationTimer <= 0)
        {
            if (!this.level.isClientSide)
            {
                ColdSweatPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new ChameleonEatMessage(this.getId()));
            }
            else
            {
                AnimationManager.ANIMATION_TIMERS.put(this, 0f);
            }
            this.eatAnimationTimer = this.getEatAnimLength();
        }
    }

    public void manualSync(EntityDataAccessor<?> accessor)
    {
        // Forge refuses to sync some DataItems, like CompoundTags
        try
        {   ((SynchedEntityData.DataItem<?>) GET_DATA_ITEM.invoke(this.entityData, accessor)).setDirty(true);
        } catch (Exception ignored) {}
    }

    public float getEatTimer()
    {
        return this.eatAnimationTimer;
    }

    public boolean isShedding()
    {
        return this.entityData.get(SHEDDING);
    }

    public void setShedding(boolean shedding)
    {
        this.entityData.set(SHEDDING, shedding);
    }

    public CompoundTag getTrustedPlayers()
    {
        return this.entityData.get(TRUSTED_PLAYERS);
    }

    public void addTrustedPlayer(UUID player)
    {
        CompoundTag trustedPlayers = this.getTrustedPlayers();
        ListTag players = trustedPlayers.getList("Players", 8);
        StringTag uuid = StringTag.valueOf(player.toString());
        if (!players.contains(uuid))
        {   players.add(uuid);
        }
        trustedPlayers.put("Players", players);

        this.entityData.set(TRUSTED_PLAYERS, trustedPlayers);
        this.manualSync(TRUSTED_PLAYERS);
    }

    public void removeTrustedPlayer(UUID player)
    {
        CompoundTag trustedPlayers = this.getTrustedPlayers();
        trustedPlayers.getList("Players", 8).removeIf(tag -> tag.getAsString().equals(player.toString()));
        this.entityData.set(TRUSTED_PLAYERS, trustedPlayers);
    }

    public boolean isPlayerTrusted(Player player)
    {
        return this.isPlayerTrusted(player.getUUID());
    }

    public boolean isPlayerTrusted(UUID player)
    {
        return this.getTrustedPlayers().getList("Players", 8).contains(StringTag.valueOf(player.toString()));
    }

    public int getLastShed()
    {
        return this.entityData.get(LAST_SHED);
    }

    public void setLastShed(int lastShed)
    {
        this.entityData.set(LAST_SHED, lastShed);
    }

    public float getTemperature()
    {
        return this.entityData.get(TEMPERATURE);
    }

    public void setTemperature(float temperature)
    {
        this.entityData.set(TEMPERATURE, temperature);
    }

    public void setSearching(boolean searching)
    {
        this.entityData.set(SEARCHING, searching);
    }

    public boolean isSearching()
    {
        return this.entityData.get(SEARCHING);
    }

    public int getHurtTimestamp()
    {
        return this.entityData.get(HURT_TIMESTAMP);
    }

    public void setHurtTimestamp(int hurtTimestamp)
    {
        this.entityData.set(HURT_TIMESTAMP, hurtTimestamp);
    }

    public void setTrackingPos(BlockPos pos)
    {
        this.entityData.set(TRACKING_POS, pos);
    }

    public void clearTrackingPos()
    {
        this.entityData.set(TRACKING_POS, BlockPos.ZERO);
    }

    public BlockPos getTrackingPos()
    {
        return this.entityData.get(TRACKING_POS);
    }

    public boolean isTracking()
    {
        return !this.entityData.get(TRACKING_POS).equals(BlockPos.ZERO);
    }

    public long getEatTimestamp()
    {
        return this.entityData.get(EAT_TIMESTAMP);
    }

    public void setEatTimestamp(int eatTimestamp)
    {
        this.entityData.set(EAT_TIMESTAMP, eatTimestamp);
    }

    public void setCooldown(Edible edible, Integer time)
    {
        CompoundTag map = this.entityData.get(EDIBLE_COOLDOWNS);
        map.putInt(edible.associatedItems().location().toString(), time);
        this.entityData.set(EDIBLE_COOLDOWNS, map);
    }

    public Integer getCooldown(Edible edible)
    {
        return this.entityData.get(EDIBLE_COOLDOWNS).getInt(edible.getName());
    }

    public CompoundTag getCooldowns()
    {
        return this.entityData.get(EDIBLE_COOLDOWNS);
    }

    public int getAgeSecs()
    {
        return this.entityData.get(AGE_SECS);
    }

    public void setAgeSecs(int ageSecs)
    {
        this.entityData.set(AGE_SECS, ageSecs);
    }

    public boolean isTamingItem(ItemStack item)
    {
        return item.is(ModTags.Items.CHAMELEON_TAMING);
    }


    @Override
    public CompoundTag saveWithoutId(CompoundTag tag)
    {
        super.saveWithoutId(tag);

        tag.put("TrustedPlayers", Objects.requireNonNullElseGet(this.getTrustedPlayers().get("Players"), ListTag::new));
        tag.putInt("LastShed", this.getLastShed());
        tag.putBoolean("Shedding", this.isShedding());
        tag.putInt("HurtTimestamp", this.getHurtTimestamp());
        tag.putInt("AgeInSeconds", this.getAgeSecs());
        tag.putLong("EatTimestamp", this.getEatTimestamp());

        // Tracking pos
        if (this.isTracking())
            tag.putLong("TrackingPos", this.getTrackingPos().asLong());
        else tag.remove("TrackingPos");

        // Edible cooldowns
        ListTag edibleCooldowns = new ListTag();
        for (String key : this.getCooldowns().getAllKeys())
        {
            CompoundTag cooldownTag = new CompoundTag();
            cooldownTag.putString("Edible", key);
            cooldownTag.putInt("Cooldown", this.getCooldowns().getInt(key));
            edibleCooldowns.add(cooldownTag);
        }
        tag.put("EdibleCooldowns", edibleCooldowns);

        tag.putFloat("Temperature", this.getTemperature());
        return tag;
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);

        CompoundTag players = new CompoundTag();
        players.put("Players", Objects.requireNonNullElseGet(nbt.get("TrustedPlayers"), ListTag::new));
        this.entityData.set(TRUSTED_PLAYERS, players);

        this.setLastShed(nbt.getInt("LastShed"));
        this.setShedding(nbt.getBoolean("Shedding"));
        this.setHurtTimestamp(nbt.getInt("HurtTimestamp"));
        this.setAgeSecs(nbt.getInt("AgeInSeconds"));
        this.setEatTimestamp(nbt.getInt("EatTimestamp"));

        if (nbt.contains("TrackingPos"))
            this.setTrackingPos(BlockPos.of(nbt.getLong("TrackingPos")));

        ListTag edibleCooldowns = nbt.getList("EdibleCooldowns", 10);
        for (int i = 0; i < edibleCooldowns.size(); i++)
        {
            CompoundTag cooldownTag = edibleCooldowns.getCompound(i);
            ChameleonEdibles.EDIBLES.stream().filter(ed -> ed.getName().equals(cooldownTag.getString("Item"))).findFirst().ifPresent(edible ->
            {   this.setCooldown(edible, cooldownTag.getInt("Cooldown"));
            });
        }
        this.setTemperature(nbt.getFloat("Temperature"));
    }
}
