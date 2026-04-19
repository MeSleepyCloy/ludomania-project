package net.ncm.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.ncm.block.entity.PedestalBlockEntity;
import net.ncm.item.CaseItem;
import net.ncm.network.OpenCasePayload;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends Block implements BlockEntityProvider {

    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),  // Нижняя плита-основание
            Block.createCuboidShape(2.0, 1.0, 2.0, 14.0, 13.0, 14.0), // Центральная колонна
            Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0) // Широкая верхняя часть
    );

    public PedestalBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PedestalBlockEntity pedestal) {
            ItemStack stored = pedestal.getInventory().get(0);

            // 1. Если пьедестал пустой, а в руке что-то есть
            if (stored.isEmpty() && !stack.isEmpty()) {
                // Если игрок пытается поставить CaseItem, проверяем, в креативе ли он
                if (stack.getItem() instanceof CaseItem && !player.isCreative()) {
                    return ActionResult.PASS; // Обычным игрокам нельзя ставить кейсы
                }

                pedestal.setInventory(stack.split(1));
                return ActionResult.SUCCESS;
            }

            // 2. Если на пьедестале уже что-то лежит
            else if (!stored.isEmpty()) {
                // Если это кейс
                if (stored.getItem() instanceof CaseItem caseItem) {
                    if (player.isCreative() && player.isSneaking()) {
                        // Креативщик на шифте (Sneaking) может ЗАБРАТЬ кейс
                        player.getInventory().offerOrDrop(stored.copy());
                        pedestal.setInventory(ItemStack.EMPTY);
                    } else {
                        // Любой игрок (и креативщик без шифта) ОТКРЫВАЕТ кейс
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                            ServerPlayNetworking.send(serverPlayer, new OpenCasePayload(caseItem.getCaseId()));
                        }
                    }
                }
                // Если это обычный предмет
                else {
                    player.getInventory().offerOrDrop(stored.copy());
                    pedestal.setInventory(ItemStack.EMPTY);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof PedestalBlockEntity pedestal) {
            ItemStack stored = pedestal.getInventory().get(0);

            if (!stored.isEmpty()) {
                // Если это кейс
                if (stored.getItem() instanceof CaseItem caseItem) {
                    if (player.isCreative() && player.isSneaking()) {
                        // Креативщик на шифте забирает кейс
                        player.getInventory().offerOrDrop(stored.copy());
                        pedestal.setInventory(ItemStack.EMPTY);
                    } else {
                        // Открываем кейс
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                            ServerPlayNetworking.send(serverPlayer, new OpenCasePayload(caseItem.getCaseId()));
                        }
                    }
                }
                // Если это обычный предмет
                else {
                    player.getInventory().offerOrDrop(stored.copy());
                    pedestal.setInventory(ItemStack.EMPTY);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}