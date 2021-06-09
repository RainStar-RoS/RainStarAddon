package RainStarAbility;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.util.Vector;

import RainStarEffect.Stiffen;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.google.common.base.Predicate;

@AbilityManifest(name = "����!", rank = Rank.A, species = Species.HUMAN, explain = {
		"��7ä�� ��8- ��c���� ����!��f: �ڽ��� 3�� ���� �������� ���� ��󿡰�",
		" ������ ���� ���ظ� �ް� �ִ� ���� ��c����!��f��� ��ĥ ���,",
		" �ٹ� $[LOOK_RANGE]��� ���� ��� �÷��̾ ����� �ٶ󺸸� ���縦 ��ġ��,",
		" ����� ���� �����̻� �ɸ��� �˴ϴ�. $[STOP_COOLDOWN]",
		" �� �� ������ �����Դ� ���� ���ظ� �ֱ� ������ �������� �ʽ��ϴ�.",
		"��7ä�� ��8- ��c�ɷ� ����!��f: ��c�ɷ� ����!��f�� ��ġ�� �ֺ� $[ABILITY_STOP_RANGE]ĭ ����",
		" ��� �÷��̾��� �ɷ� Ÿ�̸Ӱ� $[ABILITY_STOP_DURATION]�ʰ� ���߰� �˴ϴ�. $[ABILITY_STOP_COOLDOWN]",
		"��7�����̻� ��8- ��c������f: �̵�, ����, ü�� ȸ��, �ɷ� ����� �Ұ��մϴ�.",
		" ���� �޴� ��� ���ظ� 80% �氨�Ͽ� �޽��ϴ�."})

@SuppressWarnings("deprecation")
public class Stop extends AbilityBase {

	public Stop(Participant participant) {
		super(participant);
	}
	
	public static final SettingObject<Integer> ABILITY_STOP_COOLDOWN = abilitySettings.new SettingObject<Integer>(Stop.class,
			"ability-stop-cooldown", 100, "# �ɷ� ����! ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> STOP_COOLDOWN = abilitySettings.new SettingObject<Integer>(Stop.class,
			"stop-cooldown", 170, "# ����! ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> LOOK_RANGE = abilitySettings.new SettingObject<Integer>(Stop.class,
			"look-range", 10, "# ����!�� �������ִ� �÷��̾� ����") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public static final SettingObject<Integer> ABILITY_STOP_RANGE = abilitySettings.new SettingObject<Integer>(Stop.class,
			"ability-stop-range", 8, "# �ɷ� ����! ����") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public static final SettingObject<Integer> ABILITY_STOP_DURATION = abilitySettings.new SettingObject<Integer>(Stop.class,
			"ability-stop-duration", 5, "# ���ӽð�") {
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
	
	private final Predicate<Entity> notarget = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))) {
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
	
	private int stoprange = ABILITY_STOP_RANGE.getValue();
	private int lookrange = LOOK_RANGE.getValue();
	private int duration = ABILITY_STOP_DURATION.getValue();
	private Player damager;
	
	private Set<Player> players = new HashSet<>();
	
	private final Cooldown astopcooldown = new Cooldown(ABILITY_STOP_COOLDOWN.getValue());
	private final Cooldown stopcooldown = new Cooldown(STOP_COOLDOWN.getValue());
	
	private final AbilityTimer damaged = new AbilityTimer(80) {
		
		@Override
		public void run(int count) {
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer attacked = new AbilityTimer(60) {
		
		@Override
		public void run(int count) {
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	@SubscribeEvent(ignoreCancelled = false)
	public void onPlayerChat(PlayerChatEvent e) {
		if (e.getPlayer().equals(getPlayer()) && !stopcooldown.isRunning()) {
			if (e.getMessage().equals("����!") || e.getMessage().equals("���� ����!")) {
				e.setCancelled(true);
				if (damaged.isRunning() && !attacked.isRunning()) {
					for (Player p : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), lookrange, lookrange, predicate)) {
						Vector direction = damager.getEyeLocation().toVector().subtract(p.getEyeLocation().toVector());
						float yaw = LocationUtil.getYaw(direction), pitch = LocationUtil.getPitch(direction);
						for (Player allplayer : Bukkit.getOnlinePlayers()) {
							if (!p.equals(damager)) NMS.rotateHead(allplayer, p, yaw, pitch);	
						}
						if (!p.equals(damager)) {
							p.chat("��6[��e�ɷ¡�6] ��c����!");	
						}
					}
					Stiffen.apply(getGame().getParticipant(damager), TimeUnit.SECONDS, 10);
					getPlayer().chat("��6[��e�ɷ¡�6] ��c����!");
					players.add(damager);
					stopcooldown.start();
				} else {
					getPlayer().sendMessage("��4[��c!��4] ��f���ظ� ���� ���� ���ų� ���ε� ������ ����Ͽ����ϴ�.");
				}	
			}
		}
		if (e.getPlayer().equals(getPlayer()) && e.getMessage().equals("�ɷ� ����!")) {
			if (!astopcooldown.isCooldown()) {
				for (Player player : LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), stoprange, stoprange, notarget)) {
					Participant p = getGame().getParticipant(player);
					if (p.hasAbility() && !p.getAbility().isRestricted()) {
						AbilityBase ab = p.getAbility();
						for (GameTimer t : ab.getTimers()) {
							new Stopper(t).start();
						}
					}
				}
				getPlayer().chat("��6[��e�ɷ¡�6] ��c�ɷ� ����!");
				astopcooldown.start();
			}
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer())) {
			if (attacked.isRunning()) attacked.setCount(60);
			else attacked.start();
			if (e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				if (players.contains(p) && !getGame().getParticipant(p).hasEffect(Stiffen.registration)) {
					players.remove(e.getEntity());
				}	
			}
		} else if (e.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) e.getDamager();
			if (getPlayer().equals(projectile.getShooter())) {
				if (attacked.isRunning()) attacked.setCount(60);
				else attacked.start();
				if (players.contains(e.getEntity())) {
					players.remove(e.getEntity());
				}
			}
		}
		if (e.getEntity().equals(getPlayer())) {
			if (e.getDamager() instanceof Player) {
				if (players.contains(e.getDamager())) {
					e.setCancelled(true);
				} else {
					damager = (Player) e.getDamager();
					if (damaged.isRunning()) {
						damaged.setCount(60);
					} else {
						damaged.start();	
					}
				}
			} else if (e.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) e.getDamager();
				if (!getPlayer().equals(projectile.getShooter())) {
					if (players.contains(projectile.getShooter())) {
						e.setCancelled(true);
					} else {
						damager = (Player) projectile.getShooter();
						if (damaged.isRunning()) {
							damaged.setCount(60);
						} else {
							damaged.start();	
						}
					}
				}
			}
		}
	}
	
	private class Stopper extends AbilityTimer {
		
		private GameTimer t;
		private int getcount;
		
		private Stopper(GameTimer t) {
			super(TaskType.NORMAL, duration * 20);
			setPeriod(TimeUnit.TICKS, 1);
			this.t = t;
		}
		
		@Override
		protected void onStart() {
			getcount = t.getCount();
		}
		
		@Override
		protected void run(int count) {
			t.setCount(getcount);
		}
		
	}
	
}
