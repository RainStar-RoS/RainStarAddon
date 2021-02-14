package RainStarSynergy;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import RainStarAbility.TimeStop;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "���� ��ź", rank = Rank.S, species = Species.HUMAN, explain = {
		"��7�нú� ��8- ��c���ı���f: ��� ���� ���ظ� �����ϰ�, ���� ���ظ�ŭ ��ȸ���մϴ�.",
		" ������ �Ͼ ������ ���� ��ġ�� �����̵��ϰ� $[DURATION_CONFIG]�ʰ� �� 1 ������ ����ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��c���� ��ź��f: �ڽ����κ��� $[RANGE_CONFIG]ĭ ���� �� $[BOMB_AMOUNT]���� ��ź�� ",
		" �������� ��ġ�մϴ�. ��ź���� ������ ���� �ð��� �ٸ��� �� ��ź�� ������",
		" ���߱����� ���� �ð��� �� �� �ֽ��ϴ�. $[COOLDOWN_CONFIG]",
		" ���� ��ź�� ������, ���߿� �ָ��� ����� �ð��� $[TIMESTOP_DURATION]�ʰ� ������ŵ�ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��c���� ������f: ���ı����� �����̵��� �� ���θ� �����մϴ�."
		})

public class TimeBomb extends Synergy implements ActiveHandler {

	public TimeBomb(Participant participant) {
		super(participant);
	}
	
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame().getParticipant(entity.getUniqueId()).hasAbility()) {
					AbilityBase ab = getGame().getParticipant(entity.getUniqueId()).getAbility();
					if (ab.getClass().equals(TimeStop.class)) {
						return false;
					} else if (ab.getClass().equals(Mix.class)) {
						Mix mix = (Mix) ab;
						if (mix.hasAbility()) {
							if (!mix.hasSynergy()) {
								if (mix.getFirst().getClass().equals(TimeStop.class)
										|| mix.getSecond().getClass().equals(TimeStop.class)) {
										return false;
									}
							}
						}
					}
				}
			}
			return true;
		}
	};
	
	public static final SettingObject<Integer> COOLDOWN_CONFIG = 
			synergySettings.new SettingObject<Integer>(TimeBomb.class, "cooldown", 200,
            "# ö�� ��Ŭ�� ��Ÿ��", "# ����: ��") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };
    
	public static final SettingObject<Integer> DURATION_CONFIG = 
			synergySettings.new SettingObject<Integer>(TimeBomb.class, "duration", 10,
            "# �� ���� ���ӽð�", "# ����: ��") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
    };
    
	public static final SettingObject<Integer> RANGE_CONFIG = 
			synergySettings.new SettingObject<Integer>(TimeBomb.class, "range", 20,
            "# ��ź ��ġ �ִ� ����") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
    };
    
	public static final SettingObject<Integer> BOMB_AMOUNT = 
			synergySettings.new SettingObject<Integer>(TimeBomb.class, "bomb-amount", 30,
            "# ��ġ�� ������ź�� ����") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
    };
    
	public static final SettingObject<Integer> TIMESTOP_DURATION = 
			synergySettings.new SettingObject<Integer>(TimeBomb.class, "timestop-duration", 2,
            "# �ð� ���� ���ӽð�", "# ����: ��") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
    };
	
	private PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (DURATION_CONFIG.getValue() * 20), 0, true, false);
	private final Cooldown cool = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._50);
	private final int count = BOMB_AMOUNT.getValue();
	private final int timestopduration = TIMESTOP_DURATION.getValue();
	private boolean teleport = true;
	private Set<Player> stopped = new HashSet<>();
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		if (stopped.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityPickupItem(EntityPickupItemEvent e) {
		if (stopped.contains(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onPlayerDrop(PlayerDropItemEvent e) {
		if (stopped.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onClick(InventoryClickEvent e) {
		if (stopped.contains(e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityShootBow(EntityShootBowEvent e) {
		if (stopped.contains(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (stopped.contains(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.LOW)
	public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
		if (stopped.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.LOW)
	public void onEntityInteract(EntityInteractEvent e) {
		if (stopped.contains(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (stopped.contains(e.getEntered())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onVehicleDestory(VehicleDestroyEvent e) {
		if (stopped.contains(e.getAttacker())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
		if (stopped.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (stopped.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if (stopped.contains(e.getEntity().getShooter())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	public void onEntityExplode(EntityExplodeEvent e) {
		if (teleport) {
			getPlayer().teleport(e.getLocation());
			if (getPlayer().hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
				if (!(getPlayer().getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() > 1) && !(getPlayer().getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getDuration() > 10)) {
					getPlayer().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
					getPlayer().addPotionEffect(strength);
				}
			} else {
				getPlayer().addPotionEffect(strength);		
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockExplode(BlockExplodeEvent e) {
		if (teleport) {
			getPlayer().teleport(e.getBlock().getLocation());
			if (getPlayer().hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
				if (!(getPlayer().getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() > 1) && !(getPlayer().getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getDuration() > 10)) {
					getPlayer().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
					getPlayer().addPotionEffect(strength);
				}
			} else {
				getPlayer().addPotionEffect(strength);		
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getCause().equals(DamageCause.BLOCK_EXPLOSION) || e.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
			if (e.getEntity().equals(getPlayer())) {
				e.setCancelled(true);
				Healths.setHealth(getPlayer(), getPlayer().getHealth() + (e.getFinalDamage() * 0.75));
			}
			if (!e.getEntity().equals(getPlayer())) {
				if (teleport) {
					getPlayer().teleport(e.getEntity().getLocation());
					if (getPlayer().hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
						if (!(getPlayer().getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() > 1) && !(getPlayer().getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getDuration() > 10)) {
							getPlayer().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
							getPlayer().addPotionEffect(strength);
						}
					} else {
						getPlayer().addPotionEffect(strength);		
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
	}
	
	public boolean ActiveSkill(Material material, ClickType clicktype) {
		if (material == Material.IRON_INGOT && clicktype == ClickType.RIGHT_CLICK && !cool.isCooldown()) {
			Location center = getPlayer().getLocation();
			for (Location l : LocationUtil.getRandomLocations(center, RANGE_CONFIG.getValue(), count)) {
				Random random = new Random();
				TNTPrimed tntprimed = l.getWorld().spawn(l, TNTPrimed.class);
				tntprimed.setFuseTicks(((random.nextInt(15) + 1) * 2) * 20);
				new BombTimer(tntprimed, tntprimed.getFuseTicks()).start();
			}
			cool.start();
			return true;
		}
		
		if (material == Material.IRON_INGOT && clicktype == ClickType.LEFT_CLICK) {
			if (teleport) {
				teleport = false;
				getPlayer().sendMessage("��4[��c!��4] ��f���� ������ �Ͼ�� �����̵����� �ʽ��ϴ�.");
			} else {
				teleport = true;
				getPlayer().sendMessage("��4[��c!��4] ��f���� ������ �Ͼ�� �����̵��մϴ�.");
			}
		}
		return false;
	}
	
	
	public class BombTimer extends AbilityTimer {
		
		private TNTPrimed tntprimed;
		private int time;
		private ArmorStand hologram;
		
		private BombTimer(TNTPrimed tntprimed, int time) {
			super(time);
			this.tntprimed = tntprimed;
			final Location location = tntprimed.getLocation();
			this.hologram = location.getWorld().spawn(location.clone().add(0, 1.2, 0), ArmorStand.class);
			hologram.setVisible(false);
			hologram.setGravity(false);
			hologram.setInvulnerable(true);
			NMS.removeBoundingBox(hologram);
			hologram.setCustomNameVisible(true);
			hologram.setCustomName("��c��l" + (time / 20));
			setPeriod(TimeUnit.TICKS, 1);
		}
		
		@Override
		protected void onStart() {
			hologram.setCustomName("��c��l" + (time / 20));
		}
		
		@Override
		protected void run(int count) {
			hologram.setCustomName("��c��l" + (count / 20));
			hologram.teleport(tntprimed.getLocation().clone().add(0, 1.2, 0));
		}
		
		@Override
		protected void onEnd() {
			onSilentEnd();
		}
		
		@Override
		protected void onSilentEnd() {
			hologram.remove();
			tntprimed.setFuseTicks(0);
			for (Player p : LocationUtil.getNearbyEntities(Player.class, tntprimed.getLocation(), 6.5, 6.5, predicate)) {
				if (!stopped.contains(p)) {
					new TimeStopped(p).start();
				}
			}
		}
		
	}
	
	public class TimeStopped extends AbilityTimer {
		
		Player player;
		
		private TimeStopped(Player player) {
			super(timestopduration * 20);
			this.player = player;
			setPeriod(TimeUnit.TICKS, 1);
		}
		
		@Override
		protected void onStart() {
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(player);
			stopped.add(player);
			player.leaveVehicle();
				if (getGame().getParticipant(player).hasAbility()) {
					AbilityBase ab = getGame().getParticipant(player).getAbility();
					for (GameTimer t : ab.getRunningTimers()) {
						if (t instanceof Duration.DurationTimer || t instanceof Cooldown.CooldownTimer) {
							t.pause();
						} else {
							if (!ab.isRestricted()) {
								ab.setRestricted(true);	
							}
						}
					}
				}
			PotionEffects.SLOW_DIGGING.addPotionEffect(player, timestopduration * 20, 30, true);
			PotionEffects.BLINDNESS.addPotionEffect(player, timestopduration * 20, 1, true);
			NMS.sendTitle(player, "��b�ð��� �����.", "", 0, 40, 20);	
		}
		
		@Override
		protected void onEnd() {
			onSilentEnd();
		}
		
		@Override
		protected void onSilentEnd() {
			stopped.remove(player);
			if (getGame().getParticipant(player).hasAbility()) {
				AbilityBase ab = getGame().getParticipant(player).getAbility();
				for (GameTimer t : ab.getTimers()) {
					t.resume();
				}
				if (ab.isRestricted()) {
					ab.setRestricted(false);	
				}
			}
			NMS.clearTitle(player);
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING);	
		}
		
	}
	
}
