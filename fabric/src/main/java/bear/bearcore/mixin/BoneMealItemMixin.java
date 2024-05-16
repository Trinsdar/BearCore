package bear.bearcore.mixin;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void injectItemUse(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir){
        BlockState state = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos());
        if(state.getBlock() instanceof BonemealableBlock block && !useOnContext.getLevel().isClientSide() && useOnContext.getPlayer() != null) {
            useOnContext.getPlayer().displayClientMessage(new TextComponent("Stop Cheating!"), false);
            ItemStack stack = useOnContext.getItemInHand();
            if(block.isValidBonemealTarget(useOnContext.getLevel(), useOnContext.getClickedPos(), state, false) && !stack.isEmpty()) {
                if (stack.isDamageableItem()){
                    stack.hurtAndBreak(1, useOnContext.getPlayer(), p -> {
                        p.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                    });
                } else {
                    stack.shrink(1);
                }
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
