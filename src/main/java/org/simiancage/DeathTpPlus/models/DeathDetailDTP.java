package org.simiancage.DeathTpPlus.models;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.simiancage.DeathTpPlus.helpers.ConfigDTP;
import org.simiancage.DeathTpPlus.helpers.DeathMessagesDTP.DeathEventType;
import org.simiancage.DeathTpPlus.helpers.LoggerDTP;
import org.simiancage.DeathTpPlus.helpers.UtilsDTP;

/**
 * PluginName: DeathTpPlus
 * Class: DeathDetailDTP
 * User: DonRedhorse
 * Date: 25.11.11
 * Time: 19:35
 */

public class DeathDetailDTP {
	private Player player;
	private DeathEventType causeOfDeath;
	private Player killer;
	private String murderWeapon;
	private EntityDeathEvent entityDeathEvent;
	private static final ConfigDTP config = ConfigDTP.getInstance();
	private static final LoggerDTP log = LoggerDTP.getLogger();

	public DeathDetailDTP() {
	}

	public DeathDetailDTP(EntityDeathEvent event) {
		player = (Player) event.getEntity();
		entityDeathEvent = event;
		// Support for setHealth(0) which is used by essentials to do a suicide
		try {
			EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
			log.debug("damageEvent", damageEvent.getType());

			if (damageEvent instanceof EntityDamageByEntityEvent) {
				Entity damager = ((EntityDamageByEntityEvent) damageEvent).getDamager();
				log.debug("damager", damager.toString());
				if (damager instanceof Player) {
					log.debug("Killed by an other player");
					if (((Player) damager).getItemInHand().getType().equals(Material.AIR)) {
						causeOfDeath = DeathEventType.PVP_FISTS;
					} else {
						causeOfDeath = DeathEventType.PVP;
					}
					murderWeapon = ((Player) damager).getItemInHand().getType().toString();
					killer = (Player) damager;
				} else if (damager instanceof Creature || damager instanceof Slime) {
					log.debug("We have a creature or slime");
					if (damager instanceof Tameable && ((Tameable) damager).isTamed()) {
						causeOfDeath = DeathEventType.PVP_TAMED;
						murderWeapon = UtilsDTP.getCreatureType(damager).toString();
						killer = (Player) ((Tameable) damager).getOwner();

					} else {

						causeOfDeath = DeathEventType.valueOf(UtilsDTP.getCreatureType(damager).toString());
						log.debug("and it is: " + causeOfDeath);
					}
				} else if (damager instanceof Projectile) {
					log.debug("this is a projectile");
					log.debug("shooter", ((Projectile) damager).getShooter());
					// TODO: find out why we never get damager instance of
					// Projectile
					if (((Projectile) damager).getShooter() instanceof Player) {
						causeOfDeath = DeathEventType.PVP;
						murderWeapon = ((Projectile) damager).toString().replace("Craft", "");
						killer = (Player) ((Projectile) damager).getShooter();
					}
					if (((Projectile) damager).getShooter() == null) {
						//let's assume that null will only be caused by a dispenser!
						causeOfDeath = DeathEventType.DISPENSER;
						murderWeapon = ((Projectile) damager).toString().replace("Craft", "");
					}

				} else if (damager instanceof TNTPrimed) {
					causeOfDeath = DeathEventType.BLOCK_EXPLOSION;
				} else {
					log.info("unknown enitity damager" + damager);
				}
			} else if (damageEvent != null) {
				log.debug("DamageEvent is not by Entity");
				try {
					causeOfDeath = DeathEventType.valueOf(damageEvent.getCause().toString());
				} catch (IllegalArgumentException e) {
					causeOfDeath = DeathEventType.UNKNOWN;
				}
			}
		} catch (NullPointerException npe) {
			log.debug("normal detection of damageevent failed", npe);
			log.debug("assuming you did use essentials or similar");
			log.debug("which uses setHealth(0) to kill people");
			log.info("Deathcause is being set to SUICIDE!");
			causeOfDeath = DeathEventType.SUICIDE;
			murderWeapon = "Essentials";
		}


		if (causeOfDeath == null) {
			causeOfDeath = DeathEventType.UNKNOWN;
			murderWeapon = "unknown";
		}
		log.debug("causeOfDeath", causeOfDeath);
		log.debug("murderWeapon", murderWeapon);
		log.debug("killer", killer);
	}

	public World getWorld() {
		return player.getWorld();
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public DeathEventType getCauseOfDeath() {
		return causeOfDeath;
	}

	public void setCauseOfDeath(DeathEventType causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}

	public Player getKiller() {
		return killer;
	}

	public void setKiller(Player killer) {
		this.killer = killer;
	}

	public String getMurderWeapon() {
		return toCamelCase(murderWeapon);
	}

	public void setMurderWeapon(String murderWeapon) {
		this.murderWeapon = murderWeapon;
	}

	public EntityDeathEvent getEntityDeathEvent() {
		return entityDeathEvent;
	}

	public void setEntityDeathEvent(EntityDeathEvent entityDeathEvent) {
		this.entityDeathEvent = entityDeathEvent;
	}

	public Boolean isPVPDeath() {
		return causeOfDeath == DeathEventType.PVP || causeOfDeath == DeathEventType.PVP_FISTS || causeOfDeath == DeathEventType.PVP_TAMED;
	}

	private static String toCamelCase(String rawItemName) {
		String[] rawItemNameParts = rawItemName.split("_");
		String itemName = "";

		for (String itemNamePart : rawItemNameParts) {
			itemName = itemName + " " + toProperCase(itemNamePart);
		}

		if (itemName.trim().equals("Air")) {
			itemName = "Fists";
		}

		if (itemName.trim().equals("Bow")) {
			itemName = "Bow & Arrow";
		}

		return itemName.trim();
	}

	private static String toProperCase(String str) {
		if (str.length() < 1) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}
}

