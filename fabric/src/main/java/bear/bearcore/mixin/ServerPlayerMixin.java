package bear.bearcore.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Unique
    long bearLastSlept = 0;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
    private void injectStartSleeping(BlockPos blockPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        if (level.getGameTime() - bearLastSlept > 10000) return;
        if(!level.isClientSide()) displayClientMessage(new TextComponent("Get Back to work!"), false);
        cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
    }

    @Inject(method = "stopSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;stopSleepInBed(ZZ)V", shift = At.Shift.BEFORE))
    private void injectStopSleepInBed(boolean bl, boolean bl2, CallbackInfo ci){
        bearLastSlept = level.getGameTime();
        if (!level.isClientSide()) {
            displayClientMessage(new TextComponent("Get Back to work!"), false);
        }
    }


    @WrapOperation(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    private boolean wrapSleepCheck(Level instance, Operation<Boolean> original){
        return level.getGameTime() - bearLastSlept <= 10000;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void injectAddAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci){
        compoundTag.putLong("bearLastSlept", bearLastSlept);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void injectReadAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci){
        bearLastSlept = compoundTag.getLong("bearLastSlept");
    }
}
