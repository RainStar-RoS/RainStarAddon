package RainStarAbility;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;

@AbilityManifest(name = "������", rank = Rank.A, species = Species.UNDEAD, explain = {
		"��7�нú� ��8- ��c���Ρ�f: ���� �� ��� ���ظ� 25%�� �ݴϴ�.",
		"��7��� ��8- ��c�ѡ�f: �÷��̾�� ��� �� ��� �޽����� ���� �ٽ� ��Ȱ�մϴ�.",
		" �� ���� ���簡 �� ������ ü���� ȸ���� �� ����, ���� ȿ���� ������ϴ�.",
		"��7�нú� ��8- ��c������f: ���� ���� ���� ���� �ο� �� ������,",
		" ����� ������ ��Ÿ����, ����� ������ �� ��� ���ظ� ��� �� ���� ���ط���",
		" ����ؼ� ������ŵ�ϴ�. ����� ������ ����� ���, �ڽŵ� �����մϴ�.",
		" ����� �ٶ� ������ 10�� �ֱ�� ����� �Ǹ� �ɸ��� ���� ���� ������ �ٶ󺾴ϴ�.",
		"��7�нú� ��8- ��c������ ����f: ����� �ٸ� �÷��̾�� ��� ��",
		" ���� ����� ����� �����Ų �÷��̾�� �Űܰ��ϴ�."})

public class Revenger extends AbilityBase {
	
	public Revenger(Participant participant) {
		super(participant);
	}
	
	private boolean checkdeath = true;
	private boolean checkilook = true;
	private double lastdmg;
	private Player target;
	private static final RGB trace = RGB.of(183, 1, 1);
	private Location lastlocation;
	private PotionEffect blind = new PotionEffect(PotionEffectType.BLINDNESS, 25, 0, true, false);
	private final ActionbarChannel ac = newActionbarChannel();
	
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			getlocation.start();
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
	
	@SubscribeEvent
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (!checkdeath && e.getEntity().equals(getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	private final AbilityTimer passive = new AbilityTimer() {
	    	
    	@Override
    	public void run(int count) {
        	for (Location loc : Line.between(target.getLocation(), getPlayer().getLocation(), 150).toLocations(target.getLocation())) {
    	   		ParticleLib.REDSTONE.spawnParticle(getPlayer(), loc, trace);
        	}	
	    }
    	
	}.setPeriod(TimeUnit.SECONDS, 2).register();
	
	private final AbilityTimer ilook = new AbilityTimer(200) {
		
		@Override
		public void run(int count) {
			Player p = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 100, predicate);
    		if (p != null && p.equals(target)) {
    			ac.update("��c�ֽ� ��");
    			if (checkilook == true) {
        			target.addPotionEffect(blind);
        			SoundLib.AMBIENT_CAVE.playSound(target, 1, 1.5f);
        			NMS.rotateHead(target, target, getPlayer().getLocation().getYaw(), getPlayer().getLocation().getPitch());
        			checkilook = false;
    			}
    		} else {
    			ac.update(null);
    		}
		}
		
		@Override
		public void onEnd() {
			onSilentEnd();
		}
		
		@Override
		public void onSilentEnd() {
			checkilook = true;
			ilook.start();
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer getlocation = new AbilityTimer() {
		
    	@Override
    	public void run(int count) {
    		lastlocation = getPlayer().getLocation();
	    }
		
	}.setPeriod(TimeUnit.SECONDS, 20).register();
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		
		if (!e.isCancelled()) {
			if (e.getEntity().equals(getPlayer()) && checkdeath) {
				ParticleLib.SPELL_WITCH.spawnParticle(getPlayer().getLocation(), 0.5, 0.5, 0.5, 1, 30);
			}
			
			if (e.getDamager().equals(getPlayer()) && checkdeath) {
				e.setDamage(e.getDamage() / 4);
			}
		
			if (e.getDamager() instanceof Arrow) {
				Arrow arrow = (Arrow) e.getDamager();
				if (arrow.getShooter().equals(getPlayer())) {
					if (checkdeath) {
						e.setDamage(e.getDamage() / 4);
					} else {
						if (e.getEntity().equals(target) && target != null) {
							e.setDamage(e.getDamage() + lastdmg);
						} else {
							e.setCancelled(true);
						}
					}
				} else if (!arrow.getShooter().equals(target) && e.getEntity().equals(getPlayer()) && target != null) {
					e.setCancelled(true);
				}
			}
		
			if (getPlayer().getHealth() - e.getFinalDamage() <= 0 && getPlayer().getKiller() instanceof Player && checkdeath == true) {
				target = getPlayer().getKiller();
				if (target != null) {
					Bukkit.broadcastMessage("��f[��c�ɷ¡�f] ��c" + getPlayer().getName() + "��f���� �ɷ��� ��e�����͡�f�����ϴ�.");
					Bukkit.broadcastMessage("��c" + getPlayer().getName() + "��f�� ��a" + getPlayer().getKiller().getName() + "��f���� ���ش��߽��ϴ�. ��7��!");
					Bukkit.broadcastMessage("��c" + getPlayer().getName() + "��f�� ���� ��a" + getPlayer().getKiller().getName() + "��f���� ��c������f�� �غ��մϴ�...");
					lastdmg = Math.min((2 * (e.getFinalDamage() / 3)), 4);
					getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
					new BukkitRunnable() {
						@Override
						public void run() {
							if (lastlocation != null) {
								getPlayer().teleport(lastlocation);	
							}
						}	
					}.runTaskLater(AbilityWar.getPlugin(), 1L);
					new BukkitRunnable() {
						@Override
						public void run() {
							if (!target.isDead()) target.sendMessage("��7�������� �Ĵٺ��� �� ���� ����� ��ϴ�...");
						}	
					}.runTaskLater(AbilityWar.getPlugin(), 200L);
					checkdeath = false;
					ilook.start();
					e.setCancelled(true);
				}
			}
		
			if (e.getDamager().equals(getPlayer()) && e.getEntity().equals(target) && checkdeath == false) {
				e.setDamage(e.getDamage() + lastdmg);
			}
		
			if (e.getDamager().equals(getPlayer()) && !e.getEntity().equals(target) && checkdeath == false) {
				e.setCancelled(true);
			}	
		
			if (e.getEntity().equals(getPlayer()) && !e.getDamager().equals(target) && checkdeath == false
					&& e.getDamager() instanceof Player) {
				e.setCancelled(true);
			}
		}	
	}
	
	@SubscribeEvent
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (target != null && !checkdeath) {
			if (e.getEntity().equals(target)) {
				if (target.getKiller() == null) {
					getPlayer().setHealth(0);
					passive.stop(false);
					ilook.stop(false);
				} else {
					if (!target.getKiller().equals(getPlayer()) && target.getKiller() instanceof Player) {
						Bukkit.broadcastMessage("��c" + getPlayer().getName() + "��f�� ���� ��a" + target.getName() + "��f�� ���� ��a" + target.getKiller().getName() + "��f���� ��c������f�� �غ��մϴ�...");
						target = target.getKiller();
					}
					if (target.getKiller().equals(getPlayer())) {
						target = null;
						getPlayer().setHealth(0);
						passive.stop(false);
						ilook.stop(false);
					}
					if (!(target.getKiller() instanceof Player)) {
						target = null;
						getPlayer().setHealth(0);
						passive.stop(false);
						ilook.stop(false);
					}
				}
			}	
		}
	}

}
