package RainStarAbility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerVelocityEvent;

import RainStarEffect.TimeSlowdown;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;

@AbilityManifest(name = "ũ�γ뽺", rank = Rank.S, species = Species.GOD, explain = {
		"�ð��� �� ũ�γ뽺.",
		"��7�нú� ��8- ��3�ð� �����f: ��e�㳷��f�� ���� �ð� ������ ����ϴ�.",
		" �� ���ȿ� �ð��� ���ӽ��� ������� �������� ��Ÿ���� ���� �پ��ϴ�.",
		" �� ���ȿ� �ð��� ���ӽ��� �ֺ� 6ĭ ���� �߻�ü �� �̵� ȿ���� ������ ����ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��3�ð� ������f: �ð��� �������� ���̶�� ������,",
		" ���̶�� ������ ����ϴ�. �� �ɷ��� 3���� �� �� �ֽ��ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��3�ð� ���ۡ�f: �ֺ� 6ĭ �� �÷��̾��� �ð��� ������",
		" ��Ƽ�� �ɷ��� ��� �ߵ���Ű�� �ϸ� ��Ÿ���� 15�� ������Ű��,",
		" 10�ʰ� �ð� ��ȭ ���·� ����ϴ�. $[COOLDOWN]",
		"��7�����̻� ��8- ��3�ð� ��ȭ��f: ��Ÿ���� �� �ʸ��� 1�ʾ� �þ�ϴ�."
		},
		summarize = {
		"��e�㳷��f�� ���� ��� ȸ�� / �ֺ� �߻�ü ������ ������ ������ ����ϴ�.",
		"��7ö�� ��Ŭ����f���� ��e����f�� ��8���f�� �ڹٲ� �� �ֽ��ϴ�.",
		"��7ö�� ��Ŭ����f���� �ֺ� �÷��̾��� ��Ƽ�� �ɷ��� ���� ����Ű�� 10�ʰ�",
		"��Ÿ���� �帣�� �ʰ� �մϴ�. $[COOLDOWN]"
		})

@Tips(tip = {
        "�нú��� ��� ȸ������ �������� ���̰�, �ֺ� �÷��̾��� ��Ƽ�� �ɷ�",
        "����� ���ߴ� ������ ������ ���� �������� �÷��̰� �����մϴ�.",
        "�ٸ� �ð� ������ ��Ÿ�� ������ ����� ��Ÿ���� �ƴϰų�, �нú� �ɷ���",
        "��쿡�� ������ ����Ǳ� ������ �����ϴٸ� ����� �ɷ��� ����ϰ� ����",
        "����ϴ� ���� �����ϴ�."
}, strong = {
        @Description(subject = "ª�� �ɷ� ��Ÿ���� ���", explain = {
                "ª�� ��Ÿ���� �ɷ��� ��κ� �ɷ��� �����ؼ� ���� �������",
                "���� ������ ���ϳ�, �ð� ��ȭ �� ��Ÿ�� �߰��� 20����",
                "��Ÿ�� �ս��� ������ ����� �����ϱ� ����!"
        })
}, weak = {
        @Description(subject = "�нú� �ɷ��� ���", explain = {
                "�нú� �ɷ¿��Դ� �ð� ������ �ƹ��� ������ ��ġ�� ����,",
                "�ð� ���� �нú�θ� �ο��� �մϴ�."
        })
}, stats = @Stats(offense = Level.ZERO, survival = Level.FOUR, crowdControl = Level.FOUR, mobility = Level.ZERO, utility = Level.TWO), difficulty = Difficulty.EASY)

public class Chronos extends AbilityBase implements ActiveHandler {
	
	public Chronos(Participant participant) {
		super(participant);
	}

	private final Cooldown cool = new Cooldown(COOLDOWN.getValue());
	private int number = 3;
	private static final RGB color = RGB.of(25, 147, 168);
	private long worldtime = 0;
	private static final Circle circle = Circle.of(6, 70);
	
	public static final SettingObject<Integer> COOLDOWN = 
			abilitySettings.new SettingObject<Integer>(Chronos.class, "cooldown", 120,
            "# ��Ÿ��") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };
	
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
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
    
	private static boolean isNight(long worldtime) {
		return worldtime > 12300 && worldtime < 23850;
	}
	
	private void updateTime(World world) {
		if (worldtime > 12300 && worldtime < 23850) {
			world.setTime(1000);
		} else {
			world.setTime(13000);
		}
	}
	
	private final Set<Projectile> myprojectiles = new HashSet<Projectile>();
	
    private final AbilityTimer passive = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
    		final Location playerLoc = getPlayer().getLocation();
			worldtime = getPlayer().getWorld().getTime();
    		
    		if (!getPlayer().isDead()) {
    			if (isNight(getPlayer().getWorld().getTime())) {
    				for (Projectile projectile : LocationUtil.getNearbyEntities(Projectile.class, playerLoc, 6, 6, null)) {
    					if (!projectile.isOnGround() && !myprojectiles.contains(projectile)) {
    						projectile.setVelocity(projectile.getVelocity().multiply(0.65));
    					}
    				}
    				cooldel.stop(false);
    			} else {
    				final double maxHP = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    				if (getPlayer().getHealth() < maxHP) {
    					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), 0.025, RegainReason.CUSTOM);
    					Bukkit.getPluginManager().callEvent(event);
    					if (!event.isCancelled()) {
    						getPlayer().setHealth(Math.min(getPlayer().getHealth() + 0.025, maxHP));
    					}
    				}
    				cooldel.start();
    			}
			}
    		if (count % 2 == 0) {
				for (Location loc : circle.toLocations(playerLoc).floor(playerLoc.getY())) {
					ParticleLib.REDSTONE.spawnParticle(getPlayer(), loc, color);
				}
    		}
    	}
    }.setPeriod(TimeUnit.TICKS, 1).register();
    
    private final AbilityTimer cooldel = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
    		cool.setCount(Math.max(cool.getCount() - 1, 0));
    	}
    	
    }.setPeriod(TimeUnit.TICKS, 40).register();
    
    @SubscribeEvent
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
    	if (getPlayer().equals(e.getEntity().getShooter())) {
        	myprojectiles.add(e.getEntity());
    	}
    }
    
    @SubscribeEvent
    public void onPlayerVelocity(PlayerVelocityEvent e) {
    	if (!cooldel.isRunning()) {
    		if (LocationUtil.isInCircle(getPlayer().getLocation(), e.getPlayer().getLocation(), 6)) {
    			e.setVelocity(VectorUtil.validateVector(e.getPlayer().getVelocity().multiply(0.8)));
			}
    	}
    }
    
	public boolean ActiveSkill(Material material, ClickType clicktype) {
		if (material == Material.IRON_INGOT && clicktype == ClickType.RIGHT_CLICK && !cool.isCooldown()) {
			List<Player> players = LocationUtil.getNearbyEntities(Player.class, getPlayer().getLocation(), 6, 6,
					predicate);
			for (Player p : players) {
				Participant participant = getGame().getParticipant(p);
				if (participant.hasAbility() && participant.getAbility() instanceof ActiveHandler) {
					ActiveHandler active = (ActiveHandler) participant.getAbility();
					active.ActiveSkill(Material.IRON_INGOT, ClickType.RIGHT_CLICK);
					active.ActiveSkill(Material.IRON_INGOT, ClickType.LEFT_CLICK);
					active.ActiveSkill(Material.GOLD_INGOT, ClickType.RIGHT_CLICK);
					active.ActiveSkill(Material.GOLD_INGOT, ClickType.LEFT_CLICK);	
				}
				if (participant.hasAbility() && !participant.getAbility().isRestricted()) {
					AbilityBase ab = participant.getAbility();
					for (GameTimer t : ab.getTimers()) {
						if (t instanceof Cooldown.CooldownTimer) {
							t.setCount(t.getCount() + 15);
							TimeSlowdown.apply(participant, TimeUnit.SECONDS, 10);
						}
					}
				}
			}
			cool.start();
			return true;
		}
		if (material == Material.IRON_INGOT && clicktype == ClickType.LEFT_CLICK) {
			if (number != 0) {
				updateTime(getPlayer().getWorld());
				number--;
				getPlayer().sendMessage("�ð��� ��3������f���׽��ϴ�. ���� Ƚ�� : ��e" + number + "��fȸ");
				return true;
			} else {
				getPlayer().sendMessage("���̻� �ð���  ��3������f��ų �� �����ϴ�.");
			}
		}
		return false;
	}
}