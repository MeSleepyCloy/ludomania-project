package net.ncm.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.ncm.network.AtmPlayersPayload;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AtmBlock extends Block {
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    public static final Property<Direction> FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape LOWER_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(1, 0, 2, 15, 16, 16)
    );

    private static final VoxelShape UPPER_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(1, 0, 2, 15, 2, 16),
            Block.createCuboidShape(1, 2, 8, 15, 4, 16),
            Block.createCuboidShape(1, 4, 10, 15, 13, 16),
            Block.createCuboidShape(1, 13, 3, 15, 15, 16)
    );

    private static final Map<Direction, VoxelShape> LOWER_SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> UPPER_SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            LOWER_SHAPES.put(direction, rotateShape(Direction.NORTH, direction, LOWER_SHAPE));
            UPPER_SHAPES.put(direction, rotateShape(Direction.NORTH, direction, UPPER_SHAPE));
        }
    }

    public AtmBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(HALF, DoubleBlockHalf.LOWER)
                .with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction facing = state.get(FACING);
        return state.get(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPES.get(facing) : UPPER_SHAPES.get(facing);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (pos.getY() < world.getTopYInclusive() && world.getBlockState(pos.up()).canReplace(ctx)) {
            return this.getDefaultState()
                    .with(HALF, DoubleBlockHalf.LOWER)
                    .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
        }
        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER).with(FACING, state.get(FACING)), 3);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, net.minecraft.world.WorldView world, net.minecraft.world.tick.ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.math.random.Random random) {
        DoubleBlockHalf half = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            return neighborState.isOf(this) && neighborState.get(HALF) != half ? state : Blocks.AIR.getDefaultState();
        }
        return half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(HALF) != DoubleBlockHalf.UPPER) {
            return super.canPlaceAt(state, world, pos);
        }
        BlockState lowerState = world.getBlockState(pos.down());
        return lowerState.isOf(this) && lowerState.get(HALF) == DoubleBlockHalf.LOWER;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            List<String> onlinePlayers = world.getServer().getPlayerManager().getPlayerList().stream()
                    .filter(p -> !p.getUuid().equals(serverPlayer.getUuid()))
                    .map(p -> p.getName().getString())
                    .toList();

            ServerPlayNetworking.send(serverPlayer, new AtmPlayersPayload(onlinePlayers));
        }
        return ActionResult.SUCCESS;
    }

    // --- Математика для вращения хитбоксов ---

    private static int getDirIndex(Direction dir) {
        switch (dir) {
            case EAST: return 1;
            case SOUTH: return 2;
            case WEST: return 3;
            default: return 0; // NORTH
        }
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

        int fromIdx = getDirIndex(from);
        int toIdx = getDirIndex(to);
        int times = (toIdx - fromIdx + 4) % 4;

        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX))
            );
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }
        return buffer[0];
    }
}