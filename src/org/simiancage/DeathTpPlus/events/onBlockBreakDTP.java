package org.simiancage.DeathTpPlus.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.simiancage.DeathTpPlus.DeathTpPlus;
import org.simiancage.DeathTpPlus.helpers.ConfigDTP;
import org.simiancage.DeathTpPlus.helpers.LoggerDTP;
import org.simiancage.DeathTpPlus.objects.TombStoneBlockDTP;
import org.simiancage.DeathTpPlus.objects.TombDTP;
import org.simiancage.DeathTpPlus.workers.TombWorkerDTP;

/**
 * PluginName: DeathTpPlus
 * Class: onBlockBreakDTP
 * User: DonRedhorse
 * Date: 19.11.11
 * Time: 20:32
 */

public class onBlockBreakDTP {

    private LoggerDTP log;
    private ConfigDTP config;
    private DeathTpPlus plugin;
    private TombWorkerDTP tombWorker;

    public onBlockBreakDTP() {
        this.log = LoggerDTP.getLogger();
        this.config = ConfigDTP.getInstance();
        this.tombWorker = TombWorkerDTP.getInstance();
    }

    public void oBBTombStone (DeathTpPlus plugin, BlockBreakEvent event){

        log.debug("onBlockBreak TombStone executing");
        Block b = event.getBlock();
        Player p = event.getPlayer();

        if (b.getType() == Material.WALL_SIGN) {
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign) b
                    .getState().getData();
            TombStoneBlockDTP tStoneBlockDTP = plugin.tombBlockList.get(b.getRelative(
                    signData.getAttachedFace()).getLocation());
            if (tStoneBlockDTP == null)
                return;

            if (tStoneBlockDTP.getLocketteSign() != null) {
                Sign sign = (Sign) b.getState();
                event.setCancelled(true);
                sign.update();
                return;
            }
        }

        if (b.getType() != Material.CHEST && b.getType() != Material.SIGN_POST)
            return;

        TombStoneBlockDTP tStoneBlockDTP = plugin.tombBlockList.get(b.getLocation());

        if (tStoneBlockDTP == null)
            return;
        Location location = b.getLocation();
        String loc = location.getWorld().getName();
        loc = loc +", x=" + location.getBlock().getX();
        loc = loc +", y=" + location.getBlock().getY();
        loc = loc +", z=" + location.getBlock().getZ();
        if (!config.isAllowTombStoneDestroy() && !plugin.hasPerm(p, "admin", false)) {

            log.debug(p.getName() + " tried to destroy tombstone at "
                    + loc);
            plugin.sendMessage(p, "Tombstone unable to be destroyed");
            event.setCancelled(true);
            return;
        }

        if (plugin.lwcPlugin != null && config.isEnableLWC()
                && tStoneBlockDTP.getLwcEnabled()) {
            if (tStoneBlockDTP.getOwner().equals(p.getName())
                    || plugin.hasPerm(p, "admin", false)) {
                plugin.deactivateLWC(tStoneBlockDTP, true);
            } else {
                event.setCancelled(true);
                return;
            }
        }
        log.debug(p.getName() + " destroyed tombstone at "
                + loc);
        plugin.removeTomb(tStoneBlockDTP, true);


    }

    public void oBBTomb (DeathTpPlus plugin, BlockBreakEvent event){

        log.debug("onBlockBreak Tomb executing");
        Block b = event.getBlock();
        Player p = event.getPlayer();

        if (b.getState() instanceof Sign) {
            Block block = b;
            String playerName = event.getPlayer().getName();
            Sign sign = (Sign) block.getState();
            if (sign.getLine(0).indexOf(config.getTombKeyWord()) == 0) {
                TombDTP TombDTP;
                if (event.getPlayer().hasPermission("deathtpplus.admin.tomb")) {
                    if ((TombDTP = tombWorker.getTomb(block)) != null) {
                        TombDTP.removeSignBlock(block);
                        if (config.isResetTombRespawn()) {
                            TombDTP.setRespawn(null);
                            event.getPlayer().sendMessage(
                                    tombWorker.graveDigger + TombDTP.getPlayer()
                                            + "'s respawn point has been reset.");
                        }
                    }
                    return;
                }
                if (tombWorker.hasTomb(playerName)) {
                    if (!tombWorker.getTomb(playerName).hasSign(block))
                        event.setCancelled(true);
                    else {
                        TombDTP = tombWorker.getTomb(playerName);
                        TombDTP.removeSignBlock(block);
                        if (config.isResetTombRespawn()) {
                            TombDTP.setRespawn(null);
                            event.getPlayer().sendMessage(
                                    tombWorker.graveDigger + TombDTP.getPlayer()
                                            + "'s respawn point has been reset.");
                        }
                    }
                } else
                    event.setCancelled(true);
            }

        }

    }


}