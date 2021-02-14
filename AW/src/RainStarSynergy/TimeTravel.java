package RainStarSynergy;

import java.util.Collection;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;

@AbilityManifest(name = "�ð� ����", rank = Rank.L, species = Species.HUMAN, explain = {
		"��7ö�� ��Ŭ�� ��8- ��d�ð� �����f: ü��, ����ȿ��, ��ġ, �κ��丮 ��",
		" ���� ���� ��� ���¸� �����մϴ�. $[RCooldownConfig]",
		"��7�нú� ��8- ��a���� �����f: ġ������ ���ظ� �Ծ��� �� �� �� �� ������� �ʰ�",
		" ����� �ð����� �ڽ��� ���¸� �ǵ����ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��b�̷� �����f: $[DurationConfig]�ʰ� ��c���� ��f/ ��d���� �Ҵ� ��f��",
		" ��3Ÿ���� �Ұ� ��f���°� �Ǹ� �ߵ��� �ٽ� ��Ŭ���Ͽ�",
		" ��� ��b�̷� �����f�� �׸��� �� �ֽ��ϴ�.",
		" ��b�̷� �����f���� ���� ��, �ֺ� $[RangeConfig]ĭ �� �÷��̾��� �ð��� ��7�ְ��f�Ͽ�",
		" $[EffectDuration]�ʰ� �̵� �ӵ��� ���� �ӵ��� ������ �ϰ�, �ڽ��� �������ϴ�.",
		"��2[��a!��2] ��b_Daybreak_��f���� �ð� ��ƼŬ ���ۿ� �����ּ̽��ϴ�."
})

public class TimeTravel extends Synergy implements ActiveHandler {
	
	public TimeTravel(Participant participant) {
		super(participant);
	}
	
	private final Cooldown PastTravel = new Cooldown(LCooldownConfig.getValue(), "����");
	private final Cooldown FutureTravel = new Cooldown(RCooldownConfig.getValue(), "�̷�");
	private final int effect = EffectDuration.getValue();
	private final int range = RangeConfig.getValue();
	private final ActionbarChannel ac = newActionbarChannel();
	private static final Circle circle = Circle.of(7, 100);
	private static final RGB color = RGB.of(36, 252, 254);
	
	public static final SettingObject<Integer> RCooldownConfig = synergySettings.new SettingObject<Integer>(TimeTravel.class,
			"Cooldown", 30, "# ���� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};	
	
	public static final SettingObject<Integer> LCooldownConfig = synergySettings.new SettingObject<Integer>(TimeTravel.class,
			"Cooldown", 60, "# �̷� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};	
	
	public static final SettingObject<Integer> DurationConfig = synergySettings.new SettingObject<Integer>(TimeTravel.class,
			"Duration", 10, "# �̷� ���� ���� �ð�") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	public static final SettingObject<Integer> EffectDuration = synergySettings.new SettingObject<Integer>(TimeTravel.class,
			"Effect", 7, "# ȿ�� ���� �ð�") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	public static final SettingObject<Integer> RangeConfig = synergySettings.new SettingObject<Integer>(TimeTravel.class,
			"Range", 7, "# ȿ�� ����", "��2[��c!��2] ��7����! ��ƼŬ�� ������� �ʽ��ϴ�.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	public boolean checkdeath = true;
	
	private Location saveloc = null;
	private Collection<PotionEffect> savepotion;
	private int savefiretick = 0;
	private float savefall = 0;
	private double savehp = 0;
	private ItemStack[] saveinv;
	private float flyspeed = 0;
	private GameMode orgGM = null;
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
	    if (material.equals(Material.IRON_INGOT)) {
	    	if (clicktype.equals(ClickType.RIGHT_CLICK)) {
	    		if (PastTravel.isCooldown()) return false;
	    	savehp = getPlayer().getHealth();
	    	saveloc = getPlayer().getLocation();
	    	savefiretick = getPlayer().getFireTicks();
	    	savefall = getPlayer().getFallDistance();
	    	savepotion = getPlayer().getActivePotionEffects();
	    	saveinv = getPlayer().getInventory().getContents();
	    	PastTravel.start();
	    	getPlayer().sendMessage("�ð��� ��a�����f�Ͽ����ϴ�.");
	      return true;
	    } else if (clicktype.equals(ClickType.LEFT_CLICK)) {
	    	if (FutureTravel.isCooldown()) return false;
	    	if (!traveling.isDuration()) {
	    		traveling.start();
	    	} else if (traveling.isDuration()) {
	    		traveling.stop(false);
	    	}
	      return true;
	    }
	    }
	    return false;
	}
	
	@SubscribeEvent
	private void onPlayerTeleport(PlayerTeleportEvent e) {
		if (traveling.isRunning() && getPlayer().equals(e.getPlayer())) {
			if (e.getCause() == TeleportCause.SPECTATE) e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = 6)
	private void onEntityDamage(EntityDamageEvent e) {
		if (checkdeath && e.getEntity().equals(getPlayer())) {
			if (getPlayer().getHealth() - e.getFinalDamage() <= 0) {
				ParticleLib.ITEM_CRACK.spawnParticle(getPlayer().getLocation(), 0, 1, 0, 50, 0.3, MaterialX.CLOCK);
				SoundLib.BLOCK_END_PORTAL_SPAWN.playSound(getPlayer().getLocation(), 1, 1.5f);
				getPlayer().setHealth(savehp);
				getPlayer().setFireTicks(savefiretick);
				getPlayer().setFallDistance(savefall);
				for (PotionEffect effect : getPlayer().getActivePotionEffects()) {
					getPlayer().removePotionEffect(effect.getType());
				}
				getPlayer().addPotionEffects(savepotion);
				
				new BukkitRunnable() {
					
					@Override
					public void run() {
						getPlayer().teleport(saveloc);
						getPlayer().getInventory().setContents(saveinv);
						ParticleLib.ITEM_CRACK.spawnParticle(getPlayer().getLocation(), 0, 1, 0, 100, 0.3, MaterialX.CLOCK);
						SoundLib.BLOCK_END_PORTAL_SPAWN.playSound(getPlayer().getLocation(), 1, 1.5f);
					}
				}.runTaskLater(AbilityWar.getPlugin(), 1L);
				
				checkdeath = false;
				e.setCancelled(true);
				}
		}
	}
	
	@SubscribeEvent(priority = 6)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
	}
	
	@SubscribeEvent(priority = 6)
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
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
	
	private final Duration traveling = new Duration(DurationConfig.getValue() * 20, FutureTravel) {
		
		@Override
		protected void onDurationStart() {
			flyspeed = getPlayer().getFlySpeed();
			orgGM = getPlayer().getGameMode();
		}
		
		@Override
		protected void onDurationProcess(int arg0) {
			getPlayer().setGameMode(GameMode.SPECTATOR);
			getParticipant().attributes().TARGETABLE.setValue(false);
			ac.update("��b�̷� ���� ��");
			getPlayer().setFlySpeed(0.15f);
		}
		
		@Override
		protected void onDurationEnd() {
			onDurationSilentEnd();
		}
		
		@Override
		protected void onDurationSilentEnd() {
			getPlayer().setGameMode(orgGM);
			SoundLib.ITEM_TOTEM_USE.playSound(getPlayer(), 1, 1.7f);
			PotionEffects.FAST_DIGGING.addPotionEffect(getPlayer(), effect * 20, 2, true);
			PotionEffects.SPEED.addPotionEffect(getPlayer(), effect * 20, 2, true);
			getParticipant().attributes().TARGETABLE.setValue(true);
			clockeffect.start();
			getPlayer().setFlySpeed(flyspeed);
			
			for (Player p : LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), range, predicate)) {
				SoundLib.ENTITY_EVOKER_FANGS_ATTACK.playSound(p, 1, 0.7f);
				PotionEffects.SLOW.addPotionEffect(p, effect * 20, 2, true);
				PotionEffects.SLOW_DIGGING.addPotionEffect(p, effect * 20, 2, true);
			}
			
			new AbilityTimer(20) {
				@Override
				protected void run(int count) {
					Location center = getPlayer().getLocation().clone().add(0, 2 - count * 0.1, 0);
					for (Location loc : circle.toLocations(center).floor(center.getY())) {
						ParticleLib.REDSTONE.spawnParticle(loc, color);
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
			
			ac.update(null);
		}
		
	}.setPeriod(TimeUnit.TICKS, 1);
	
	private static final EulerAngle DEFAULT_EULER_ANGLE = new EulerAngle(Math.toRadians(270), 0, 0);
	private static final ItemStack CLOCK = MaterialX.CLOCK.createItem();

	private final AbilityTimer clockeffect = new AbilityTimer(TaskType.NORMAL, 100) {
		private ArmorStand[] armorStands;

		@Override
		protected void onStart() {
			this.armorStands = new ArmorStand[] {
					getPlayer().getWorld().spawn(getPlayer().getLocation(), ArmorStand.class),
					getPlayer().getWorld().spawn(getPlayer().getLocation(), ArmorStand.class)
			};
			for (ArmorStand armorStand : armorStands) {
				armorStand.setVisible(false);
				armorStand.setInvulnerable(true);
				armorStand.setGravity(false);
				armorStand.setRightArmPose(DEFAULT_EULER_ANGLE);
				armorStand.getEquipment().setItemInMainHand(CLOCK);
				NMS.removeBoundingBox(armorStand);
			}
		}

		@Override
		protected void run(int count) {
			for (int i = 0; i < 5; i++) {
				final int index = (count - 1) * 5 + i;
				final double t = index * 0.0155;
				armorStands[0].teleport(adjustLocation(getPlayer().getLocation().clone().add(FastMath.cos(t) * 0.8, count * 0.0155, FastMath.sin(t) * 0.8)));
				armorStands[1].teleport(adjustLocation(getPlayer().getLocation().clone().add(-FastMath.cos(t) * 0.8, count * 0.0155, -FastMath.sin(t) * 0.8)));
			}
		}

		@Override
		protected void onEnd() {
			onSilentEnd();
		}

		@Override
		protected void onSilentEnd() {
			for (ArmorStand armorStand : armorStands) {
				armorStand.remove();
			}
			this.armorStands = null;
		}
	}.setPeriod(TimeUnit.TICKS, 1);

	private static Location adjustLocation(final Location location) {
		final Vector direction = location.getDirection().setY(0).normalize();
		return location.clone().subtract(0, 1, 0).subtract(direction.clone().multiply(0.75)).add(VectorUtil.rotateAroundAxisY(direction.clone(), 90).multiply(0.4));
	}
	
}
