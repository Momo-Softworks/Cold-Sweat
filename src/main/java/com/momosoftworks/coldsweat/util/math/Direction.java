package com.momosoftworks.coldsweat.util.math;

import com.google.common.collect.Iterators;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Direction {
    DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

    /** Ordering index for D-U-N-S-W-E */
    private final int data3d;
    /** Index of the opposite Direction in the VALUES array */
    private final int oppositeIndex;
    /** Ordering index for the HORIZONTALS field (S-W-N-E) */
    private final int data2d;
    private final String name;
    private final Direction.Axis axis;
    private final Direction.AxisDirection axisDirection;
    /** Normalized vector that points in the direction of this Direction */
    private final Vec3i normal;
    private static final Direction[] VALUES = values();
    private static final Map<String, Direction> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Direction::getName, (p_199787_0_) -> {
        return p_199787_0_;
    }));
    private static final Direction[] BY_3D_DATA = Arrays.stream(VALUES).sorted(Comparator.comparingInt((p_199790_0_) -> {
        return p_199790_0_.data3d;
    })).toArray((p_199788_0_) -> {
        return new Direction[p_199788_0_];
    });
    /** All Facings with horizontal axis in order S-W-N-E */
    private static final Direction[] BY_2D_DATA = Arrays.stream(VALUES).filter((p_199786_0_) -> {
        return p_199786_0_.getAxis().isHorizontal();
    }).sorted(Comparator.comparingInt((p_199789_0_) -> {
        return p_199789_0_.data2d;
    })).toArray((p_199791_0_) -> {
        return new Direction[p_199791_0_];
    });

    private Direction(int pData3d, int pOppositeIndex, int pData2d, String pName, Direction.AxisDirection pAxisDirection, Direction.Axis pAxis, Vec3i pNormal) {
        this.data3d = pData3d;
        this.data2d = pData2d;
        this.oppositeIndex = pOppositeIndex;
        this.name = pName;
        this.axis = pAxis;
        this.axisDirection = pAxisDirection;
        this.normal = pNormal;
    }

    /**
     * Gets the {@code Direction} values for the provided entity's
     * looking direction. Dependent on yaw and pitch of entity looking.
     */
    public static Direction[] orderedByNearest(Entity entity) {
        float f = entity.getRotationYawHead() * ((float)Math.PI / 180F);
        float f1 = -entity.rotationPitch * ((float)Math.PI / 180F);
        float f2 = MathHelper.sin(f);
        float f3 = MathHelper.cos(f);
        float f4 = MathHelper.sin(f1);
        float f5 = MathHelper.cos(f1);
        boolean flag = f4 > 0.0F;
        boolean flag1 = f2 < 0.0F;
        boolean flag2 = f5 > 0.0F;
        float f6 = flag ? f4 : -f4;
        float f7 = flag1 ? -f2 : f2;
        float f8 = flag2 ? f5 : -f5;
        float f9 = f6 * f3;
        float f10 = f8 * f3;
        Direction direction = flag ? EAST : WEST;
        Direction direction1 = flag1 ? UP : DOWN;
        Direction direction2 = flag2 ? SOUTH : NORTH;
        if (f6 > f8) {
            if (f7 > f9) {
                return makeDirectionArray(direction1, direction, direction2);
            } else {
                return f10 > f7 ? makeDirectionArray(direction, direction2, direction1) : makeDirectionArray(direction, direction1, direction2);
            }
        } else if (f7 > f10) {
            return makeDirectionArray(direction1, direction2, direction);
        } else {
            return f9 > f7 ? makeDirectionArray(direction2, direction, direction1) : makeDirectionArray(direction2, direction1, direction);
        }
    }

    /**
     * Creates an array of x y z equivalent facing values.
     */
    private static Direction[] makeDirectionArray(Direction first, Direction second, Direction third) {
        return new Direction[]{first, second, third, third.getOpposite(), second.getOpposite(), first.getOpposite()};
    }

    public Quaternion getRotation() {
        Quaternion quaternion = Vec3f.XP.rotationDegrees(90.0F);
        switch(this) {
            case DOWN:
                return Vec3f.XP.rotationDegrees(180.0F);
            case UP:
                return Quaternion.ONE.copy();
            case NORTH:
                quaternion.mul(Vec3f.ZP.rotationDegrees(180.0F));
                return quaternion;
            case SOUTH:
                return quaternion;
            case WEST:
                quaternion.mul(Vec3f.ZP.rotationDegrees(90.0F));
                return quaternion;
            case EAST:
            default:
                quaternion.mul(Vec3f.ZP.rotationDegrees(-90.0F));
                return quaternion;
        }
    }

    /**
     * @return the index of this Direction (0-5). The order is D-U-N-S-W-E
     */
    public int get3DDataValue() {
        return this.data3d;
    }

    /**
     * @return the index of this horizontal facing (0-3). The order is S-W-N-E
     */
    public int get2DDataValue() {
        return this.data2d;
    }

    /**
     * Get the AxisDirection of this Facing.
     */
    public Direction.AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    /**
     * @return the opposite Direction (e.g. DOWN => UP)
     */
    public Direction getOpposite() {
        return from3DDataValue(this.oppositeIndex);
    }

    /**
     * Rotate this Direction around the Y axis clockwise (NORTH => EAST => SOUTH => WEST => NORTH)
     */
    public Direction getClockWise() {
        switch(this) {
            case NORTH:
                return EAST;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            case EAST:
                return SOUTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    /**
     * Rotate this Direction around the Y axis counter-clockwise (NORTH => WEST => SOUTH => EAST => NORTH)
     */
    public Direction getCounterClockWise() {
        switch(this) {
            case NORTH:
                return WEST;
            case SOUTH:
                return EAST;
            case WEST:
                return SOUTH;
            case EAST:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get CCW facing of " + this);
        }
    }

    /**
     * @return the offset in the x direction
     */
    public int getStepX() {
        return this.normal.getX();
    }

    /**
     * @return the offset in the y direction
     */
    public int getStepY() {
        return this.normal.getY();
    }

    /**
     * @return the offset in the z direction
     */
    public int getStepZ() {
        return this.normal.getZ();
    }

    public Vec3f step() {
        return new Vec3f((float)this.getStepX(), (float)this.getStepY(), (float)this.getStepZ());
    }

    /**
     * Same as getName, but does not override the method from Enum.
     */
    public String getName() {
        return this.name;
    }

    public Direction.Axis getAxis() {
        return this.axis;
    }

    /**
     * @return the Direction specified by the given name or null if no such Direction exists
     */
    @Nullable
    public static Direction byName(@Nullable String name) {
        return name == null ? null : BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * @return the {@code Direction} corresponding to the given index (0-5). Out of bounds values are wrapped around. The
     * order is D-U-N-S-W-E.
     * @see #get3DDataValue
     */
    public static Direction from3DDataValue(int index) {
        return BY_3D_DATA[(int) MathHelper.abs(index % BY_3D_DATA.length)];
    }

    /**
     * @return the Direction corresponding to the given horizontal index (0-3). Out of bounds values are wrapped around.
     * The order is S-W-N-E.
     * @see #get2DDataValue
     */
    public static Direction from2DDataValue(int horizontalIndex) {
        return BY_2D_DATA[(int) MathHelper.abs(horizontalIndex % BY_2D_DATA.length)];
    }

    /**
     * @return the Direction corresponding to the given angle in degrees (0-360). Out of bounds values are wrapped
     * around. An angle of 0 is SOUTH, an angle of 90 would be WEST.
     */
    public static Direction fromYRot(double angle) {
        return from2DDataValue(MathHelper.floor_double(angle / 90.0D + 0.5D) & 3);
    }

    public static Direction fromAxisAndDirection(Direction.Axis axis, Direction.AxisDirection axisDirection) {
        switch(axis) {
            case X:
                return axisDirection == Direction.AxisDirection.POSITIVE ? EAST : WEST;
            case Y:
                return axisDirection == Direction.AxisDirection.POSITIVE ? UP : DOWN;
            case Z:
            default:
                return axisDirection == Direction.AxisDirection.POSITIVE ? SOUTH : NORTH;
        }
    }

    /**
     * @return the angle in degrees corresponding to this Direction.
     * @see #fromYRot
     */
    public float toYRot() {
        return (float)((this.data2d & 3) * 90);
    }

    public static Direction getRandom(Random rand) {
        return values()[rand.nextInt(values().length)];
    }

    public static Direction getNearest(double x, double y, double z) {
        return getNearest((float)x, (float)y, (float)z);
    }

    public static Direction getNearest(float pX, float pY, float pZ) {
        Direction direction = NORTH;
        float f = Float.MIN_VALUE;

        for(Direction direction1 : VALUES) {
            float f1 = pX * (float)direction1.normal.getX() + pY * (float)direction1.normal.getY() + pZ * (float)direction1.normal.getZ();
            if (f1 > f) {
                f = f1;
                direction = direction1;
            }
        }

        return direction;
    }

    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }

    public static Direction get(Direction.AxisDirection pAxisDirection, Direction.Axis pAxis) {
        for(Direction direction : VALUES) {
            if (direction.getAxisDirection() == pAxisDirection && direction.getAxis() == pAxis) {
                return direction;
            }
        }

        throw new IllegalArgumentException("No such direction: " + pAxisDirection + " " + pAxis);
    }

    /**
     * @return the normalized Vector that points in the direction of this Direction.
     */
    public Vec3i getNormal() {
        return this.normal;
    }

    public boolean isFacingAngle(float pDegrees) {
        float f = pDegrees * ((float)Math.PI / 180F);
        float f1 = -MathHelper.sin(f);
        float f2 = MathHelper.cos(f);
        return (float)this.normal.getX() * f1 + (float)this.normal.getZ() * f2 > 0.0F;
    }

    public static enum Axis implements Predicate<Direction>
    {
        X("x") {
            public int choose(int pX, int pY, int pZ) {
                return pX;
            }

            public double choose(double pX, double pY, double pZ) {
                return pX;
            }
        },
        Y("y") {
            public int choose(int pX, int pY, int pZ) {
                return pY;
            }

            public double choose(double pX, double pY, double pZ) {
                return pY;
            }
        },
        Z("z") {
            public int choose(int pX, int pY, int pZ) {
                return pZ;
            }

            public double choose(double pX, double pY, double pZ) {
                return pZ;
            }
        };

        private static final Direction.Axis[] VALUES = values();
        private static final Map<String, Direction.Axis> BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Direction.Axis::getName, (p_199785_0_) -> {
            return p_199785_0_;
        }));
        private final String name;

        private Axis(String pName) {
            this.name = pName;
        }

        /**
         * @return the Axis specified by the given name or {@code null} if no such Axis exists
         */
        @Nullable
        public static Direction.Axis byName(String p_176717_0_) {
            return BY_NAME.get(p_176717_0_.toLowerCase(Locale.ROOT));
        }

        /**
         * Like getName but doesn't override the method from Enum.
         */
        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        /**
         * @return whether this Axis is on the horizontal plane (true for X and Z)
         */
        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public String toString() {
            return this.name;
        }

        public static Direction.Axis getRandom(Random pRandom)
        {   return VALUES[pRandom.nextInt(VALUES.length)];
        }

        public boolean test(@Nullable Direction p_test_1_) {
            return p_test_1_ != null && p_test_1_.getAxis() == this;
        }

        /**
         * @return this Axis' Plane (VERTICAL for Y, HORIZONTAL for X and Z)
         */
        public Direction.Plane getPlane() {
            switch(this) {
                case X:
                case Z:
                    return Direction.Plane.HORIZONTAL;
                case Y:
                    return Direction.Plane.VERTICAL;
                default:
                    throw new Error("Someone's been tampering with the universe!");
            }
        }

        public String getSerializedName() {
            return this.name;
        }

        public abstract int choose(int pX, int pY, int pZ);

        public abstract double choose(double pX, double pY, double pZ);
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int pStep, String pName) {
            this.step = pStep;
            this.name = pName;
        }

        /**
         * @return the offset for this AxisDirection. 1 for POSITIVE, -1 for NEGATIVE
         */
        public int getStep() {
            return this.step;
        }

        public String toString() {
            return this.name;
        }

        public Direction.AxisDirection opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public static enum Plane implements Iterable<Direction>, Predicate<Direction> {
        HORIZONTAL(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}),
        VERTICAL(new Direction[]{Direction.UP, Direction.DOWN}, new Direction.Axis[]{Direction.Axis.Y});

        private final Direction[] faces;
        private final Direction.Axis[] axis;

        private Plane(Direction[] pFaces, Direction.Axis[] pAxis) {
            this.faces = pFaces;
            this.axis = pAxis;
        }

        /**
         * Choose a random Direction from this Plane using the given Random
         */
        public Direction getRandomDirection(Random pRandom) {
            return this.faces[pRandom.nextInt(this.faces.length)];
        }

        public Direction.Axis getRandomAxis(Random pRandom) {
            return this.axis[pRandom.nextInt(this.axis.length)];
        }

        public boolean test(@Nullable Direction p_test_1_) {
            return p_test_1_ != null && p_test_1_.getAxis().getPlane() == this;
        }

        public Iterator<Direction> iterator() {
            return Iterators.forArray(this.faces);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.faces);
        }
    }
}