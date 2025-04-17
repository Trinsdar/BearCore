package bear.bearcore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
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
        if(event.getBlock().getBlock() instanceof BonemealableBlock block && !event.getWorld().isClientSide() && event.getPlayer() != null) {
            event.getPlayer().displayClientMessage(new TextComponent("Stop Cheating!"), false);
            ItemStack stack = event.getStack();
            if(block.isValidBonemealTarget(event.getWorld(), event.getPos(), event.getBlock(), false) && !stack.isEmpty()) {
                if (stack.isDamageableItem()){
                    stack.hurtAndBreak(1, event.getPlayer(), p -> {
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
        if(event.getPlayer() == null) return;
        CompoundTag data = event.getPlayer().getPersistentData();
        if(event.getPlayer().level.getGameTime() - data.getLong("bear_last_slept") > 10000) {
            return;
        }
        if(!event.getPlayer().level.isClientSide()) event.getPlayer().displayClientMessage(new TextComponent("Get Back to work!"), false);
        event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
    }

    public void onSleepTime(SleepingTimeCheckEvent event) {
        if(event.getPlayer() == null) return;
        CompoundTag data = event.getPlayer().getPersistentData();
        if(event.getPlayer().level.getGameTime() - data.getLong("bear_last_slept") > 10000) {
            event.setResult(Event.Result.ALLOW);
        }
    }

    public void onTimeChange(SleepFinishedTimeEvent event) {
        event.setTimeAddition(event.getWorld().dayTime() + 1000);
    }

    public void onWakeUp(PlayerWakeUpEvent event) {
        if(event.getPlayer() == null) return;
        event.getPlayer().getPersistentData().putLong("bear_last_slept", event.getPlayer().level.getGameTime());
        if(!event.getPlayer().level.isClientSide()) event.getPlayer().displayClientMessage(new TextComponent("Get Back to work!"), false);
    }
}