package RainStarSynergy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import RainStarEffect.Madness;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.nms.PickupStatus;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;

@AbilityManifest(
		name = "���� ��", rank = Rank.L, species = Species.HUMAN, explain = {
		"��7ȭ�� ���� ��8- ��3Ʈ�����͡�f: ��ũ�� ä�� Ȱ �߻� �� ȭ���� ������ ��ġ��",
		" ǥ���� ����� ���� ������� �޴� �ʵ带 �����մϴ�. $[ArrowcoolConfig]",
		" ǥ���� 1���� �����ϸ� ���� �ø��� ���� �� �÷��̾��� ��ġ�� �ڹٲߴϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��5��Ʈ���f: ǥ���� �ִ� ��ġ�� �����̵��մϴ�. $[CooldownConfig]",
		" �̶� ������ �ڽ��� �ִ� ��ġ�κ��� $[TeleportConfig]ĭ �̳��� ��� �÷��̾��",
		" ���� ����� �����ڸ� ���� �����̵���Ű�� 3�ʰ� ������ŵ�ϴ�.",
		" ���� �̵� �� ��ġ���� ������ ����ŵ�ϴ�. ���� ����� ǥ���� ���� �� �־��ٸ�",
		" �� ȿ������ ���� �����ϰ� ������ ���� �ִ� ��ġ�� �̵���Ų �� 6�ʰ�",
		" �ߵ� ������� ������ ȿ���� ���� �� �ٽ� ǥ���� ��ġ�� �̵���ŵ�ϴ�.",
		" �����̵� ����, �ڽ��� 3�ʰ� ���� ���°� �˴ϴ�.",
		"��7�����̻� ��8- ��c������f: �� �ʸ��� �������� �������� ƨ�ܳ����ϴ�.",
		" ���� ��ƼƼ�� ���� ���� �̿��� ��� ���ظ� 1.5��� �ް� �˴ϴ�.",
		})

public class MagicShow extends Synergy implements ActiveHandler {

	public MagicShow(Participant participant) {
		super(participant);
	}
	
	public static final SettingObject<Integer> ArrowcoolConfig = synergySettings.new SettingObject<Integer>(MagicShow.class,
			"Arrow Cooldown", 30, "# ȭ�� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};	
	
	public static final SettingObject<Integer> CooldownConfig = synergySettings.new SettingObject<Integer>(MagicShow.class,
			"Cooldown", 70, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> RangeConfig = synergySettings.new SettingObject<Integer>(MagicShow.class,
			"Field Range", 7, "# ǥ�� �ʵ� ����", "��2[��c!��2] ��7����! ��ƼŬ�� ������� �ʽ��ϴ�.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	public static final SettingObject<Integer> TeleportConfig = synergySettings.new SettingObject<Integer>(MagicShow.class,
			"Teleport Range", 5, "# �ڷ���Ʈ ����") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
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
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	private final Predicate<Entity> predicate2 = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	private final Circle circle = Circle.of(7, 100);
	private final Cooldown teleCool = new Cooldown(CooldownConfig.getValue(), "���� ����");
	private final Cooldown arrowCool = new Cooldown(ArrowcoolConfig.getValue(), "ǥ��");
	private final int range = RangeConfig.getValue();
	private final int teleport = TeleportConfig.getValue();
	private static final RGB color = RGB.of(25, 147, 168);
	private final ActionbarChannel ac = newActionbarChannel();
	private Arrow arrow;
	private Arrow shootarrow;
	private Location firstlocation;
	private Location arrowlocation;
	private final Set<Player> fallcancel = new HashSet<>();
	
	protected void onUpdate(AbilityBase.Update update) {
	    if (update == AbilityBase.Update.RESTRICTION_SET || update == AbilityBase.Update.ABILITY_DESTROY) {
	    	if (arrow != null) {
				arrow.setGlowing(false);
				NMS.setPickupStatus(arrow, PickupStatus.ALLOWED);
				arrow = null;
				ac.unregister();
	    	}
	    } 
	}
	
	private final AbilityTimer passive = new AbilityTimer() {
		
		@Override
		public void run(int count) {
			if (arrow != null) {
				if (arrow.getWorld().getWorldBorder().isInside(arrow.getLocation())) {
					for (Location loc : circle.toLocations(arrow.getLocation()).floor(arrow.getLocation().getY())) {
						ParticleLib.REDSTONE.spawnParticle(getPlayer(), loc, color);
					}
					ParticleLib.SMOKE_LARGE.spawnParticle(arrow.getLocation(), 0, 0, 0, 3, 0);
					for (Player p : LocationUtil.getEntitiesInCircle(Player.class, arrow.getLocation(), range, predicate)) {
						if (!getGame().getParticipant(p).hasEffect(Madness.registration)) {
							Madness.apply(getGame().getParticipant(p), TimeUnit.SECONDS, 5, 20);
						} else {
							if (getGame().getParticipant(p).getPrimaryEffect(Madness.registration).getDuration() <= 80) {
								getGame().getParticipant(p).getPrimaryEffect(Madness.registration).setCount(5);	
							}
						}
					}		
					ac.update("��3ǥ�� ��ġ��f: ��5" + arrow.getLocation().getBlockX() + "��f, ��5" + arrow.getLocation().getBlockY() + "��f, ��5" + arrow.getLocation().getBlockZ());
				} else {
					getPlayer().sendMessage("[��c!��f] ��3ǥ�ġ�f�� ���� ��輱 ���� ���� ��������ϴ�.");
					arrowCool.setCount(0);
					activecool.stop(false);
					getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 1));
					arrow.remove();
					arrow = null;
					ac.unregister();
				}
			}
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer activecool = new AbilityTimer(60) {
		
		@Override
		public void run(int count) {
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer inv = new AbilityTimer(60) {
		
		@Override
		public void run(int count) { }
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if (e.getHitEntity() == null && !arrowCool.isRunning() && NMS.isArrow(e.getEntity()) && getPlayer().equals(e.getEntity().getShooter())) {
			Arrow hitarrow = (Arrow) e.getEntity();
			if (hitarrow == shootarrow) {
				if (arrow != null) {
					arrow.setGlowing(false);
					NMS.setPickupStatus(arrow, PickupStatus.ALLOWED);	
				}
				arrow = (Arrow) e.getEntity();
				NMS.setPickupStatus(arrow, PickupStatus.DISALLOWED);
				arrow.setGlowing(true);
				if (!passive.isRunning()) {
					passive.start();
				}
				
				final HashMap<Player, Location> locationMap = new HashMap<>();
				final List<Player> list = LocationUtil.getNearbyEntities(Player.class, arrow.getLocation(), range, range, predicate);
				for (Player p : list) {
					locationMap.put(p, p.getLocation());
				}
				
				Collections.shuffle(list);
				ArrayList<Player> keySet = new ArrayList<>(locationMap.keySet());
				for (int i = 0; i < list.size(); i++) {
					list.get(i).teleport(locationMap.get(keySet.get(i)));
				}
				
				arrowCool.start();
				activecool.start();	
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityShootBow(EntityShootBowEvent e) {
		if (getPlayer().isSneaking()) {
			shootarrow = (Arrow) e.getProjectile();
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if ((e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION) && e.getEntity().equals(getPlayer())) e.setCancelled(true);
		if (e.getEntity().equals(getPlayer()) && inv.isRunning()) e.setCancelled(true);
		if (fallcancel.contains(e.getEntity()) && e.getCause() == DamageCause.FALL) {
			e.getEntity().sendMessage("��a���� ������� ���� �ʽ��ϴ�.");
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound((Player) e.getEntity());
			fallcancel.remove(e.getEntity());
			e.setCancelled(true);
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
		if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.LEFT_CLICK) && !teleCool.isCooldown()) {
			if (arrow != null) {
				firstlocation = getPlayer().getLocation();
				arrowlocation = arrow.getLocation();
				for (Player player : getPlayer().getWorld().getPlayers()) {
					if (LocationUtil.isInCircle(arrow.getLocation(), player.getLocation(), range)) {
						if (!getPlayer().equals(player)) {
							ParticleLib.PORTAL.spawnParticle(player.getLocation(), 0, 0, 0, 200, 0.5);
							player.teleport(firstlocation);
							SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(player, 1, 0.7f);
							if (player != null) {
								new AbilityTimer(6) {
									
									ActionbarChannel actionbar = newActionbarChannel();
									
									@Override
									protected void onStart() {
										if (player != null) {
											actionbar = getGame().getParticipant(player).actionbar().newChannel();
										}
									}
									
									@Override
									public void run(int count) {
										actionbar.update("��2�ߵ���f: " + count + "��");
										PotionEffects.POISON.addPotionEffect(player, 99999, 2, true);
										ParticleLib.ITEM_CRACK.spawnParticle(player.getLocation(), 0.3, 1, 0.3, 50, 0, MaterialX.SLIME_BLOCK);
									}
									
									@Override
									public void onEnd() {
										onSilentEnd();
									}
									
									@Override
									public void onSilentEnd() {
										actionbar.unregister();
										player.removePotionEffect(PotionEffectType.POISON);
										ParticleLib.PORTAL.spawnParticle(player.getLocation(), 0, 0, 0, 200, 0.5);
										player.teleport(arrowlocation);
										SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(player, 1, 0.7f);
										if (!fallcancel.contains(player)) fallcancel.add(player);
									}
									
								}.setPeriod(TimeUnit.SECONDS, 1).start();
							}
						}
					} else {
						for (Player p : LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), teleport, predicate2)) {
							if (!getGame().getParticipant(p).hasEffect(Madness.registration)) {
								ParticleLib.PORTAL.spawnParticle(p.getLocation(), 0, 0, 0, 200, 0.5);
								p.teleport(arrowlocation);
								getPlayer().setVelocity(new Vector(0, 0, 0));
								SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(p, 1, 1.3f);
								if (!getPlayer().equals(p)) Stun.apply(getGame().getParticipant(p), TimeUnit.TICKS, 60);
								new BukkitRunnable() {
									@Override
									public void run() {
										ParticleLib.EXPLOSION_HUGE.spawnParticle(getPlayer().getLocation());
										getPlayer().getWorld().createExplosion(getPlayer().getLocation(), 1.9f, false, false);
										getPlayer().setVelocity(new Vector(0, 0, 0));
									}	
								}.runTaskLater(AbilityWar.getPlugin(), 1L);	
								if (!fallcancel.contains(p)) fallcancel.add(p);
							}
						}
						if (getGame().getParticipant(player).hasEffect(Madness.registration)) {
							ParticleLib.PORTAL.spawnParticle(player.getLocation(), 0, 0, 0, 200, 0.5);
							player.teleport(arrowlocation);
							SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(player, 1, 1.3f);
							Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, 60);
							if (!fallcancel.contains(player)) fallcancel.add(player);
						}
					}
				}
				arrow.setGlowing(false);
				NMS.setPickupStatus(arrow, PickupStatus.ALLOWED);
				arrow = null;
				ac.unregister();
				return teleCool.start();
			} else {
				getPlayer().sendMessage("[��c!��f] ǥ���� �������� �ʾ� ��3���� �����f�� �� �� �����ϴ�.");
			}
		}
		return false;
	}
	
}
