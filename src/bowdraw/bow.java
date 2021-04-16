package bowdraw;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.RegisteredAttack;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.MMOPlayerData;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class bow implements Listener {
	private HashMap<UUID, Long> cooldown = new HashMap<UUID, Long>();


	@EventHandler
	public void onClick(final PlayerInteractEvent e) {
		Vector shoot = new Vector(1, 0, 0);
		Vector direction = e.getPlayer().getLocation().getDirection();
		Vector finalshoot = shoot.multiply(direction);

		final PlayerData data = PlayerData.get(e.getPlayer().getUniqueId()); //get uuid
		final String Classname = data.getProfess().getName(); //get class name

		final int Classlevel = data.getLevel(); //get main level

		final NBTItem realitemNBT = NBTItem.get(e.getPlayer().getInventory().getItemInMainHand());
		int level = realitemNBT.getInteger("MMOITEMS_REQUIRED_LEVEL");//mmmoitem interger


		final boolean bow = e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BOW;
		if (bow && Classname.equalsIgnoreCase("Marksman") && Classlevel >= level && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if(cooldown.containsKey(e.getPlayer().getUniqueId()) && cooldown.get(e.getPlayer().getUniqueId()) > System.currentTimeMillis()) {
				e.setCancelled(true);
			}
			else{
				Player p = e.getPlayer();
				p.launchProjectile(Arrow.class); //shoot bow without arrow in inventory
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1.5f);
				cooldown.put(e.getPlayer().getUniqueId(), System.currentTimeMillis() + (7 * 100));
			}
		}

		if (bow && !Classname.equalsIgnoreCase("Marksman")) {
			Player player = e.getPlayer();
		//	player.sendMessage("You Dont Have Right Class To Use This Item");
			e.setCancelled(true);
		}

		if (bow && Classname.equalsIgnoreCase("Marksman") && !(Classlevel >= level)) {
		//	e.getPlayer().sendMessage("You dont have enought Level To Use This Item");
			e.setCancelled(true);
		}
		if (!bow){
			return;
		}
	}

	@EventHandler
	public void onPick(final PlayerPickupArrowEvent e){
		e.getArrow().setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED); //disable arrow pickup
	}


	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void a(EntityDamageByEntityEvent event){
		if(!(event.getEntity() instanceof Damageable))
			return;


		RegisteredAttack attack = getAttack(event);

		if (attack == null || attack.getDamager() == null)
			return;

		if(attack.getDamager() instanceof Player && !attack.getDamager().hasMetadata("NPC")){
			PlayerAttackEvent attackEvent = new PlayerAttackEvent(MMOPlayerData.get((Player) attack.getDamager()), event, attack.getResult());
			Bukkit.getPluginManager().callEvent(attackEvent);
			if (attackEvent.isCancelled())
				return;

				event.setDamage(attack.getResult().getDamage());
			}
	}

	private RegisteredAttack getAttack(EntityDamageByEntityEvent event){
		RegisteredAttack custom = MythicLib.plugin.getDamage().findInfo(event.getEntity());
		if(custom != null){
			custom.getResult().setDamage(event.getDamage());
			return custom;
		}
		if(event.getDamager() instanceof Projectile) {
			Projectile arrow = (Projectile) event.getDamager();
			if(arrow.getShooter() instanceof LivingEntity){
				return new RegisteredAttack(new AttackResult(event.getDamage(), DamageType.WEAPON, DamageType.PHYSICAL, DamageType.PROJECTILE), (LivingEntity) arrow.getShooter());
			}
		}
		return null;
	}
}
