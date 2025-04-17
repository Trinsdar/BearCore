package bear.bearcore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod("bearcore")
public class BearCore {

    public BearCore() {

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(this::onBonemeal);
        bus.addListener(this::onTrySleeping);
        bus.addListener(this::onSleepTime);
        bus.addListener(this::onTimeChange);
        bus.addListener(this::onWakeUp);
        
    }

    public void onBonemeal(BonemealEvent event) {
        if(event.getBlock().getBlock() instanceof BonemealableBlock block && !event.getLevel().isClientSide() && event.getEntity() != null) {
            event.getEntity().displayClientMessage(Component.literal("Stop Cheating!"), false);
            ItemStack stack = event.getStack();
            if(block.isValidBonemealTarget(event.getLevel(), event.getPos(), event.getBlock(), false) && !stack.isEmpty()) {
                if (stack.isDamageableItem()){
                    stack.hurtAndBreak(1, event.getEntity(), p -> {
                        p.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                    });
                } else {
                    stack.shrink(1);
                }
            }
        }
        event.setCanceled(true);
    }

    public void onTrySleeping(PlayerSleepInBedEvent event) {
        if(event.getEntity() == null) return;
        CompoundTag data = event.getEntity().getPersistentData();
        if(event.getEntity().level().getGameTime() - data.getLong("bear_last_slept") > 10000) {
            return;
        }
        if(!event.getEntity().level().isClientSide()) event.getEntity().displayClientMessage(Component.literal("Get Back to work!"), false);
        event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
    }

    public void onSleepTime(SleepingTimeCheckEvent event) {
        if(event.getEntity() == null) return;
        CompoundTag data = event.getEntity().getPersistentData();
        if(event.getEntity().level().getGameTime() - data.getLong("bear_last_slept") > 10000) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    public void onTimeChange(SleepFinishedTimeEvent event) {
        event.setTimeAddition(event.getLevel().dayTime() + 1000);
    }

    public void onWakeUp(PlayerWakeUpEvent event) {
        if(event.getEntity() == null) return;
        event.getEntity().getPersistentData().putLong("bear_last_slept", event.getEntity().level().getGameTime());
        if(!event.getEntity().level().isClientSide()) event.getEntity().displayClientMessage(Component.literal("Get Back to work!"), false);
    }
}