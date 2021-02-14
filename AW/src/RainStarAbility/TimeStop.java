package RainStarAbility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "�ð� ����", rank = Rank.L, species = Species.HUMAN, explain = {
		"��7ö�� ��Ŭ�� ��8- ��bŸ�� �����f: 5���� �ð��� ��� $[DURATION_CONFIG]�ʰ� �ð��� ����ϴ�.",
		" �ð��� ���� ���� ��� �÷��̾�� �ൿ �Ҵ��� �Ǿ� ���� ���� �� ������ �ð�����",
		" ���� ���ظ� �� ���� 1/5�� �ٿ��� �޽��ϴ�. �ð��� �����Ǳ� �� ���� ���̴�",
		" �ɷ��� ������ ���� ���� �����ؼ� �����ϰ� �˴ϴ�. $[COOLDOWN_CONFIG]",
		"��7�нú� ��8- ��aŸ�� �÷ο��f: �ð� ������ ������ ���� �ʽ��ϴ�.",
		"��7�нú� ��8- ��3Ÿ�� �������f: �ð� ���� ������ �� ��� ���ӽð��� 30%�� �����մϴ�.",
		" �پ�� ���ӽð��� �Ҽ����� �ø� ó���Ǹ�, 1�� �̸����δ� ���� �ʽ��ϴ�.",
		" ���� �� ��� ���� ���� �� ���� ���ҷ��� �پ��ϴ�."})

@Tips(tip = {
        "������ �ð��� ����, ��� �÷��̾ �ൿ���� ���ϰ� �����",
        "���� �ð����� ����, ����, ȸ�� �� �����̵� �� �� �ִ� �ɷ��Դϴ�.",
        "�ð��� ��� ����� ���� ����� �����Դϴ�."
}, strong = {
        @Description(subject = "�ð� ����", explain = {
        		"�ٸ� �÷��̾��� ������ ������ �����ϰ� �ð��� ������",
        		"�� ������ �����, ��� �ɷ��� ī������ �� �ֽ��ϴ�.",
        })
}, weak = {
		@Description(subject = "Ÿ���� �Ұ�", explain = {
        		"Ÿ���� �Ұ��� ��󿡰� �� �ɷ��� ������ �ʽ��ϴ�."
        }),
		@Description(subject = "�ð� ���� �ɷ� ������", explain = {
        		"�ð� ���� �ɷ��� �����ϰ� �ִ� ��󿡰�",
        		"�� �ɷ��� ������ ������ �����ϼ���."
        })
}, stats = @Stats(offense = Level.TWO, survival = Level.EIGHT, crowdControl = Level.TEN, mobility = Level.ZERO, utility = Level.SEVEN), difficulty = Difficulty.VERY_EASY)

public class TimeStop extends AbilityBase implements ActiveHandler {
	
	public TimeStop(Participant participant) {
		super(participant);
	}
	
	private final Map<Player, Double> damageCounter = new HashMap<>();
	private final Map<Projectile, Vector> velocityMap = new HashMap<>();
	private Set<Player> custominv = new HashSet<>();
	private Set<Player> timestoppers = new HashSet<>();
	private final Cooldown timeStop = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._25);
	private boolean effectboolean = EFFECT_CONFIG.getValue();
	private Duration stopduration = null;
	private static final Vector zerov = new Vector(0, 0, 0);
	private long worldtime = 0;
	
	
	public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(TimeStop.class,
			"cooldown", 120, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};	
	
	public static final SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(TimeStop.class,
			"duration", 10, "# �ð��� �� �� ������ų�� �����մϴ�.") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public static final SettingObject<Boolean> EFFECT_CONFIG = abilitySettings.new SettingObject<Boolean>(TimeStop.class,
			"effect-config", true, "# ����Ʈ ����", "# �ð� ���� �� ���� ���� ������ �� �� ������ �����մϴ�.") {
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
	
	protected void onUpdate(AbilityBase.Update update) {
	    if (update == AbilityBase.Update.RESTRICTION_CLEAR) {
	    	if (!checkTimestoppers.isRunning()) checkTimestoppers.start();
	    }
	}
	
    private final AbilityTimer checkTimestoppers = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
			for (Participant participants : getGame().getParticipants()) {
				if (participants.hasAbility()) {
					AbilityBase ab = participants.getAbility();
					if (ab.getClass().equals(Mix.class)) {
						Mix mix = (Mix) ab;
						if (!mix.hasSynergy()) {
							if (mix.getFirst() != null && mix.getSecond() != null) {
								if (mix.getFirst().getClass().equals(TimeStop.class)) timestoppers.add(participants.getPlayer());
								if (mix.getSecond().getClass().equals(TimeStop.class)) timestoppers.add(participants.getPlayer());
								if (!mix.getFirst().getClass().equals(TimeStop.class) && !mix.getSecond().getClass().equals(TimeStop.class)) timestoppers.remove(participants.getPlayer());	
							} else timestoppers.remove(participants.getPlayer());
						} else timestoppers.remove(participants.getPlayer());
					} else {
						if (ab.getClass().equals(TimeStop.class)) timestoppers.add(participants.getPlayer());
						else timestoppers.remove(participants.getPlayer());
					}
				} else if (!participants.hasAbility()) timestoppers.remove(participants.getPlayer());
			}
    	}
    
    }.setPeriod(TimeUnit.TICKS, 1).register();
	
	private AbilityTimer timecount = new AbilityTimer(100) {
		
		@Override
		protected void run(int count) {
		}
		
		@Override
		protected void onStart() {
			timecount1.start();
		}
		
		@Override
		protected void onEnd() {
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
		      for (AbstractGame.Participant participants : getGame().getParticipants()) {
		    	  if (participants.equals(getParticipant()))
		    		  continue; 
		    	  if (getGame() instanceof DeathManager.Handler) {
		    		  DeathManager.Handler game = (DeathManager.Handler)getGame();
		    		  if (game.getDeathManager().isExcluded(participants.getPlayer().getUniqueId()))
		    			  continue; 
			      } 
		      } 
			  stopduration.start();
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	
	private final AbilityTimer timecount1 = new AbilityTimer(5) {
		@Override
		protected void run(int arg0) {
			for (Participant participants : getGame().getParticipants()) {
				if (arg0 % 2 == 0) {
					SoundLib.BLOCK_NOTE_BLOCK_SNARE.playSound(participants.getPlayer(), 1, 2f);
				} else {
					SoundLib.BLOCK_NOTE_BLOCK_SNARE.playSound(participants.getPlayer(), 1, 1.7f);
				}
			}
		}
	}.setPeriod(TimeUnit.SECONDS, 1);
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getPlayer())) {
				e.setTo(e.getFrom());
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityPickupItem(EntityPickupItemEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getEntity())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onPlayerDrop(PlayerDropItemEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getPlayer())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onClick(InventoryClickEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getWhoClicked())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityShootBow(EntityShootBowEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getEntity())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getEntity())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getPlayer())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityInteract(EntityInteractEvent e) {
		if (stopduration.isRunning() && predicate.test(e.getEntity())) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onVehicleEnter(VehicleEnterEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getEntered())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onVehicleDestory(VehicleDestroyEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getAttacker())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getPlayer())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getPlayer())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityDamage(PlayerItemHeldEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && predicate.test(e.getPlayer())) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning()) {
				if (predicate.test((Entity) e.getEntity().getShooter())) {
					e.setCancelled(true);
				} else {
					new AbilityTimer(2) {
						
						@Override
						protected void run(int count) {
						}
						
						@Override
						protected void onEnd() {
							velocityMap.put(e.getEntity(), e.getEntity().getVelocity());
							e.getEntity().setGravity(false);
							e.getEntity().setVelocity(zerov);
						}

						@Override
						protected void onSilentEnd() {
							velocityMap.put(e.getEntity(), e.getEntity().getVelocity());
							e.getEntity().setGravity(false);
							e.getEntity().setVelocity(zerov);
						}
					}.setPeriod(TimeUnit.TICKS, 1).start();
				}
			}
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && e.getEntity() instanceof Damageable) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && e.getEntity() instanceof Damageable) {
				e.setCancelled(true);
			}	
		}
	}
	
	@SubscribeEvent(priority = Priority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (stopduration != null) {
			if (stopduration.isRunning() && e.getEntity() instanceof Player) {
				e.setCancelled(true);
				if (!custominv.contains(e.getEntity())) {
					if (e.getDamager() instanceof Arrow) {
						Arrow arrow = (Arrow) e.getDamager();
						arrow.remove();
			      	}
					double damage = (damageCounter.getOrDefault(e.getEntity(), Double.valueOf(0.0D))).doubleValue();
					damageCounter.put((Player) e.getEntity(), Double.valueOf(damage + e.getFinalDamage()));
					new CustomInv((Player) e.getEntity()).start();
				}
		    }	
		}
	}
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
		if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK) && !timeStop.isCooldown() && 
			!timecount.isRunning()) {
			if (stopduration != null) {
				if (!stopduration.isDuration()) {
					timecount.start();
				    stopduration = new InvTimer();
				    return true;
				}
			} else {
				timecount.start();
			    stopduration = new InvTimer();
			    return true;	
			}
		} 
		return false;
	}
	
	class InvTimer extends Duration {

		public InvTimer() {
			super((int) Math.max(1, Math.ceil((DURATION_CONFIG.getValue() * Math.pow(0.7, timestoppers.size() - 1)))), timeStop);
			setPeriod(TimeUnit.SECONDS, 1);
		}
		
		@Override
		protected void onDurationProcess(int arg0) {
			getPlayer().getWorld().setTime(worldtime);
			for (Participant participants : getGame().getParticipants()) {
				if (predicate.test(participants.getPlayer())) {
					if (participants.hasAbility()) {
						AbilityBase ab = participants.getAbility();
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
				}
			}
		}
		
		@Override
		protected void onDurationStart() {
			worldtime = getPlayer().getWorld().getTime();
			for (Participant participants : getGame().getParticipants()) {
				if (predicate.test(participants.getPlayer())) {
					Enderman enderman = participants.getPlayer().getWorld().spawn(participants.getPlayer().getLocation(), Enderman.class);
					enderman.setSilent(true);
					enderman.setInvulnerable(true);
					enderman.setAI(false);
					PotionEffects.INVISIBILITY.addPotionEffect(enderman, 9999, 1, false);
					if (effectboolean) {
						new AbilityTimer((int) Math.max(1, Math.ceil((DURATION_CONFIG.getValue() * Math.pow(0.7, timestoppers.size() - 1)))) * 20) {
							
							@Override
							public void onStart() {
								NMS.setCamera(participants.getPlayer(), enderman);	
							}
							
							@Override
							public void onEnd() {
								onSilentEnd();
							}
							
							@Override
							public void onSilentEnd() {
								NMS.setCamera(participants.getPlayer(), participants.getPlayer());
								enderman.remove();
							}
							
						}.setPeriod(TimeUnit.TICKS, 1).start();		
					}
				}
				
				if (predicate.test(participants.getPlayer())) {
					participants.getPlayer().leaveVehicle();
					if (participants.hasAbility()) {
						AbilityBase ab = participants.getAbility();
						for (GameTimer t : ab.getTimers()) {
							t.pause();
						}
					}
					PotionEffects.SLOW_DIGGING.addPotionEffect(participants.getPlayer(), 400, 30, true);
					NMS.sendTitle(participants.getPlayer(), "��b�ð��� �����.", "", 0, 40, 20);	
				} else {
					getParticipant().attributes().TARGETABLE.setValue(false);
				}
			}
			
			for (Entity entity : getPlayer().getWorld().getEntities()) {
				if (entity instanceof Projectile) {
					velocityMap.put((Projectile) entity, entity.getVelocity());
					entity.setGravity(false);
					entity.setVelocity(zerov);
				}
			}
		}
		
		@Override
		protected void onDurationEnd() {
			onDurationSilentEnd();
		}
		
		@Override
		protected void onDurationSilentEnd() {
			for (Player p : damageCounter.keySet()) {
				Healths.setHealth((Player) p, Math.max(0.0, p.getHealth() - (damageCounter.get(p) / Math.max(1, 5 - (timestoppers.size() - 1)))));
			}
			damageCounter.clear();
			
			for (AbstractGame.Participant participants : getGame().getParticipants()) {
				if (predicate.test(participants.getPlayer())) {		
					if (participants.hasAbility()) {
						AbilityBase ab = participants.getAbility();
						for (GameTimer t : ab.getTimers()) {
							t.resume();
						}
						if (ab.isRestricted()) {
							ab.setRestricted(false);	
						}
					}
					
					NMS.clearTitle(participants.getPlayer());
					participants.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);	
				} else {
					getParticipant().attributes().TARGETABLE.setValue(true);
				}
			}
			
			for (Entity entity : getPlayer().getWorld().getEntities()) {
				if (entity instanceof Projectile) {
					velocityMap.forEach(Projectile::setVelocity);
					entity.setGravity(true);
					velocityMap.clear();
				}
			}
		}
	}
	
	class CustomInv extends AbilityTimer {
		
		Player player;
		
		public CustomInv(Player player) {
			super(7);
			setPeriod(TimeUnit.TICKS, 1);
			this.player = player;
		}
		
		@Override
		protected void onStart() {
			if (!custominv.contains(player)) {
				custominv.add(player);			
			}
		}
		
		@Override
		protected void onEnd() {
			onSilentEnd();
		}
		
		@Override
		protected void onSilentEnd() {
			if (custominv.contains(player)) {
				custominv.remove(player);			
			}
		}
		
	}
}