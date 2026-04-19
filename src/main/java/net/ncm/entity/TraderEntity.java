package net.ncm.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.ncm.screen.TraderScreenHandler;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

public class TraderEntity extends PathAwareEntity {
    private static final TrackedData<String> TRADER_ID = DataTracker.registerData(TraderEntity.class, TrackedDataHandlerRegistry.STRING);
    public float prevHeadYaw;

    public TraderEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TRADER_ID, "trader1");
    }

    public void setTraderId(String id) {
        this.dataTracker.set(TRADER_ID, id);
    }

    public String getTraderId() {
        return this.dataTracker.get(TRADER_ID);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(1, new LookAroundGoal(this));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    public static DefaultAttributeContainer.Builder createTraderAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 100.0D)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("trader_id", this.getTraderId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("trader_id")) {
            this.setTraderId(nbt.getString("trader_id").orElse("trader1"));
        }
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            if (!this.getWorld().isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.openHandledScreen(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<net.ncm.network.OpenTraderPayload>() {
                    @Override
                    public net.ncm.network.OpenTraderPayload getScreenOpeningData(ServerPlayerEntity player) {
                        return new net.ncm.network.OpenTraderPayload(getTraderId(), getId());
                    }

                    @Override
                    public Text getDisplayName() {
                        return Text.translatable("name.ludomania." + getTraderId());
                    }

                    @Override
                    public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inv, PlayerEntity player) {
                        return new TraderScreenHandler(syncId, inv, getTraderId());
                    }
                });
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}