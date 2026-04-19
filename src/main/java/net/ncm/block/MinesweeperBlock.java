package net.ncm.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
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
import net.ncm.network.OpenMinesweeperPayload;

import java.util.EnumMap;
import java.util.Map;

public class MinesweeperBlock extends Block {
    public static final Property<Direction> FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape BASE_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
            Block.createCuboidShape(3.0, 10.0, 3.0, 13.0, 10.1, 13.0),
            Block.createCuboidShape(-0.5, 0.0, 10.0, 16.5, 7.0, 11.0),
            Block.createCuboidShape(-0.5, 0.0, 5.0, 16.5, 7.0, 6.0),
            Block.createCuboidShape(5.0, 0.0, -0.5, 6.0, 7.0, 16.5),
            Block.createCuboidShape(10.0, 0.0, -0.5, 11.0, 7.0, 16.5)
    );

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            SHAPES.put(direction, rotateShape(Direction.NORTH, direction, BASE_SHAPE));
        }
    }

    public MinesweeperBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(FACING));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new OpenMinesweeperPayload());
        }
        return ActionResult.SUCCESS;
    }

    private static int getDirIndex(Direction dir) {
        switch (dir) {
            case EAST: return 1;
            case SOUTH: return 2;
            case WEST: return 3;
            default: return 0;
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