package com.momosoftworks.coldsweat.common.entity.task;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.common.entity.GoatEntity;
import com.momosoftworks.coldsweat.core.init.ActivityInit;
import com.momosoftworks.coldsweat.core.init.EntityInit;
import com.momosoftworks.coldsweat.core.init.MemoryInit;
import com.momosoftworks.coldsweat.core.init.SoundInit;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.RangedInteger;

public class GoatTasks
{
    private static final RangedInteger WALKING_SPEED = RangedInteger.of(5, 16);
    private static final RangedInteger LONG_JUMP_COOLDOWN_RANGE = RangedInteger.of(600, 1200);
    private static final RangedInteger RAM_COOLDOWN_RANGE = RangedInteger.of(600, 6000);
    private static final RangedInteger SCREAMING_RAM_COOLDOWN_RANGE = RangedInteger.of(100, 300);
    private static final EntityPredicate RAM_TARGET_PREDICATE = (new EntityPredicate()).selector((entity) -> {
        return !entity.getType().equals(EntityInit.GOAT.get()) && entity.level.getWorldBorder().isWithinBounds(entity.getBoundingBox());
    });

    public GoatTasks()
    {
    }

    public static void resetLongJumpCooldown(GoatEntity goat)
    {
        goat.getBrain().setMemory(MemoryInit.LONG_JUMP_COOLING_DOWN.get(), LONG_JUMP_COOLDOWN_RANGE.randomValue(goat.level.getRandom()));
        goat.getBrain().setMemory(MemoryInit.RAM_COOLDOWN_TICKS.get(), RAM_COOLDOWN_RANGE.randomValue(goat.level.getRandom()));
    }

    public static Brain<?> makeBrain(Brain<GoatEntity> brain)
    {
        addCoreActivities(brain);
        addIdleActivities(brain);
        addLongJumpActivities(brain);
        addRamActivities(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void addCoreActivities(Brain<GoatEntity> brain)
    {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(new SwimTask(0.8F), new WalkTask(2.0F), new LookTask(45, 90), new WalkToTargetTask(), new TemptationCooldownTask(MemoryInit.TEMPTATION_COOLDOWN_TICKS.get()), new TemptationCooldownTask(MemoryInit.LONG_JUMP_COOLING_DOWN.get()), new TemptationCooldownTask((MemoryModuleType) MemoryInit.RAM_COOLDOWN_TICKS.get())));
    }

    private static void addIdleActivities(Brain<GoatEntity> brain)
    {
        brain.addActivityWithConditions(Activity.IDLE, ImmutableList.of(Pair.of(0, new RunSometimesTask<>(new LookAtEntityTask(EntityType.PLAYER, 6.0F), RangedInteger.of(30, 60))), Pair.of(0, new AnimalBreedTask(EntityInit.GOAT.get(), 1.0F)), Pair.of(1, new TemptTask((goat) -> {
            return 1.25F;
        })), Pair.of(2, new ChildFollowNearestAdultTask<>(WALKING_SPEED, 1.25F)), Pair.of(3, new FirstShuffledTask<>(ImmutableList.of(Pair.of(new WalkRandomlyTask(1.0F), 2), Pair.of(new WalkTowardsLookTargetTask(1.0F, 3), 2), Pair.of(new DummyTask(30, 60), 1))))), ImmutableSet.of(Pair.of(MemoryInit.RAM_TARGET.get(), MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryInit.LONG_JUMP_MID_JUMP.get(), MemoryModuleStatus.VALUE_ABSENT)));
    }

    private static void addLongJumpActivities(Brain<GoatEntity> brain)
    {
        brain.addActivityWithConditions(ActivityInit.LONG_JUMP.get(), ImmutableList.of(Pair.of(0, new LeapingChargeTask(LONG_JUMP_COOLDOWN_RANGE, SoundInit.ENTITY_GOAT_STEP.get())), Pair.of(1, new LongJumpTask<>(LONG_JUMP_COOLDOWN_RANGE, 5, 5, 1.5F, (goat) -> {
            return goat.isScreaming()
                   ? SoundInit.ENTITY_GOAT_SCREAMING_LONG_JUMP.get()
                   : SoundInit.ENTITY_GOAT_LONG_JUMP.get();
        }))), ImmutableSet.of(Pair.of(MemoryInit.TEMPTING_PLAYER.get(), MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryInit.LONG_JUMP_COOLING_DOWN.get(), MemoryModuleStatus.VALUE_ABSENT)));
    }

    private static void addRamActivities(Brain<GoatEntity> brain)
    {
        brain.addActivityWithConditions(ActivityInit.RAM.get(), ImmutableList.of(Pair.of(0, new RamImpactTask<>((goat) -> {
            return goat.isScreaming() ? SCREAMING_RAM_COOLDOWN_RANGE : RAM_COOLDOWN_RANGE;
        }, RAM_TARGET_PREDICATE, 3.0F, (goat) -> {
            return goat.isBaby() ? 1.0 : 2.5;
        }, (goat) -> {
            return goat.isScreaming()
                   ? SoundInit.ENTITY_GOAT_SCREAMING_RAM_IMPACT.get()
                   : SoundInit.ENTITY_GOAT_RAM_IMPACT.get();
        })), Pair.of(1, new PrepareRamTask<>((goat) -> {
            return goat.isScreaming()
                   ? SCREAMING_RAM_COOLDOWN_RANGE.getMinInclusive()
                   : RAM_COOLDOWN_RANGE.getMinInclusive();
        }, 4, 7, 1.25F, RAM_TARGET_PREDICATE, 20, (entityIn) -> {
            return entityIn.isScreaming()
                   ? SoundInit.ENTITY_GOAT_SCREAMING_PREPARE_RAM.get()
                   : SoundInit.ENTITY_GOAT_PREPARE_RAM.get();
        }))), ImmutableSet.of(Pair.of(MemoryInit.TEMPTING_PLAYER.get(), MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryModuleType.BREED_TARGET, MemoryModuleStatus.VALUE_ABSENT), Pair.of(MemoryInit.RAM_COOLDOWN_TICKS.get(), MemoryModuleStatus.VALUE_ABSENT)));
    }

    public static void updateActivities(GoatEntity goat)
    {   goat.getBrain().setActiveActivityToFirstValid(ImmutableList.of(ActivityInit.RAM.get(), ActivityInit.LONG_JUMP.get(), Activity.IDLE));
    }

    public static Ingredient getTemptItems()
    {   return Ingredient.of(Items.WHEAT);
    }
}
