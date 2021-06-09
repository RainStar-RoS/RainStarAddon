package RainStarAbility;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Crescent;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;
import daybreak.google.common.collect.ImmutableSet;

@AbilityManifest(
		name = "��Ρ����ȡ�", rank = Rank.S, species = Species.OTHERS, explain = {
		"��7�нú� ��c- ��8��� ���֡�f: �ڽ��� ��ġ�� ��ο���� ��ų ���ط��� �������ϴ�.",
		"��7�� ��� F ��8- ��b�ð� ���ܡ�f: ��ų ��� �� ��� �÷��̾ �̵���",
		" �ð��� ������ ����, �ٽ� �ɷ��� ��� Ȥ�� �ڵ� �ߴ��� �� ���� ����",
		" �ð��� ������ ���ε鿡�� ���ظ� �����ϴ�. $[COOLDOWN]",
		"��7�нú� ��8- ��c���� ���֡�f: ȸ�� ȿ���� ���� �� �����ϴ�.",
		" ȸ������ 10%��ŭ �߰� ���ݷ��� �ִ� $[MAX_DAMAGE]���� ȹ���� �� �ֽ��ϴ�."
		},
		summarize = {
		"ü�� ȸ���� ���� ���ϴ� ��� ȸ������ 10%��ŭ �߰� ���ݷ��� ȹ���մϴ�.",
		"��7���� ��� FŰ��f�� ������ ��� �÷��̾ �������� ������ ����� ����",
		"�÷��̾��� ������ ����� ���� ���� ���� �� �ٽ� ��7���� ��� FŰ��f�� �����ų�",
		"���� �ð��� ����� �� ��󿡰� ū ���ظ� ���� �� �ֽ��ϴ�.",
		" $[COOLDOWN]"
		})

public class KuroEye extends AbilityBase {

	public KuroEye(Participant participant) {
		super(participant);
	}
	
	public static final SettingObject<Integer> COOLDOWN 
	= abilitySettings.new SettingObject<Integer>(KuroEye.class,
			"cooldown", 60, "# �ð� ���� ��Ÿ��",
			"# ��Ÿ�� ���� ȿ���� �ִ� 50%���� �޽��ϴ�.") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> MAX_DAMAGE 
	= abilitySettings.new SettingObject<Integer>(KuroEye.class,
			"max-damage", 10, "# �ִ� �߰� ���ط�") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	protected void onUpdate(AbilityBase.Update update) {
	    if (update == Update.RESTRICTION_CLEAR) {
	    	ac.update("��c���� ���֡�f: ��7" + df.format(stack));
	    }
	}
	
	private final DecimalFormat df = new DecimalFormat("0.00");
	private double stack = 0;
	private final ActionbarChannel ac = newActionbarChannel();
	private static final Set<Material> swords;
	private final Cooldown cool = new Cooldown(COOLDOWN.getValue(), "�ð� ����", CooldownDecrease._50);
	private static final Circle circle = Circle.of(6, 70);
	private Map<Player, LogParticle> logMap = new HashMap<>();
	private Set<Player> damaged = new HashSet<>();
	private final Crescent crescent = Crescent.of(3, 50);
	private Random random = new Random();
	
	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
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
		if (e.getEntity().equals(getPlayer())) {
			stack = Math.min(MAX_DAMAGE.getValue(), stack + (e.getAmount() * 0.1));
			ac.update("��c���� ���֡�f: ��7" + df.format(stack));
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	private void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		if (e.getRightClicked().hasMetadata("TimeCutter")) e.setCancelled(true);
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer())) {
			e.setDamage(e.getDamage() + stack);
		}
		if (e.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) e.getDamager();
			if (getPlayer().equals(projectile.getShooter())) {
				e.setDamage(e.getDamage() + stack);
			}
		}
	}
	
    @SubscribeEvent(onlyRelevant = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
    	if (swords.contains(e.getOffHandItem().getType())) {
    		if (!cool.isCooldown()) {
        		if (skill.isRunning()) {
        			if (skill.getCount() >= 7) {
        				getPlayer().sendMessage("��4[��c!��4] ��f���� �ּ����� ���� ���� ������� �ʾҽ��ϴ�.");
        				getPlayer().sendMessage("��4[��c!��4] ��3�ּ� ��� �ð���f: ������ ��e" + Math.abs(6 - skill.getCount()) + "��f��");
        			} else {
        				skill.stop(false);
        			}
        		} else {
            		skill.start();	
        		}	
    		}
    		e.setCancelled(true);
    	}
    }
    
	private final AbilityTimer skillcircle = new AbilityTimer(50) {
		
		@Override
		public void run(int count) {
			for (Location loc : circle.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
				ParticleLib.REDSTONE.spawnParticle(getPlayer(), loc, RGB.of(77, 77, 77));
			}
		}
		
	}.setPeriod(TimeUnit.TICKS, 4).register();
    
	private final AbilityTimer skill = new AbilityTimer(TaskType.REVERSE, 10) {
		
		@Override
		public void onStart() {
			SoundLib.BLOCK_END_PORTAL_SPAWN.playSound(getPlayer(), 0.75f, 0.75f);
			for (Participant participant : getGame().getParticipants()) {
				if (predicate.test(participant.getPlayer())) {
					new LogParticle(participant.getPlayer()).start();	
				}
			}
			skillcircle.start();
		}
		
		@Override
		public void run(int count) {
			if (count <= 3) {
				getPlayer().sendMessage("��4[��c!��4] ��e" + skill.getCount() + "��f�� �� ��� ��3����f�˴ϴ�!");
				SoundLib.BLOCK_NOTE_BLOCK_SNARE.playSound(getPlayer(), 1, 1.7f);
			}
		}
		
		@Override
		public void onEnd() {
			onSilentEnd();
		}
		
		@Override
		public void onSilentEnd() {
			new CutParticle(180).start();
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer().getLocation(), 1, 1.2f);
			skillcircle.stop(false);
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
			for (Participant participant : getGame().getParticipants()) {
				if (predicate.test(participant.getPlayer()) && logMap.containsKey(participant.getPlayer())) {
					List<Location> locations = logMap.get(participant.getPlayer()).getLog();
					for (int a=0; a<(locations.size() - 1); a++) {
						if (LocationUtil.isInCircle(getPlayer().getLocation(), locations.get(a), 6)) {
							if (!damaged.contains(participant.getPlayer())) {
								ArmorStand armorstand = participant.getPlayer().getWorld().spawn(participant.getPlayer().getEyeLocation().clone().add(0, 20, 0), ArmorStand.class);
				            	armorstand.setRightArmPose(new EulerAngle(Math.toRadians(80), 0, 0));
				            	armorstand.setMetadata("TimeCutter", new FixedMetadataValue(AbilityWar.getPlugin(), null));
				            	armorstand.setVisible(false);
				            	armorstand.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
				            	armorstand.setGravity(true);
				            	NMS.removeBoundingBox(armorstand);
				            	armorstand.setVelocity(new Vector(0, -0.75, 0));
				            	
								ArmorStand clock = locations.get(a).getWorld().spawn(locations.get(a), ArmorStand.class);
								clock.setRightArmPose(new EulerAngle(Math.toRadians(270), 0, 0));
								clock.setMetadata("TimeCutter", new FixedMetadataValue(AbilityWar.getPlugin(), null));
								clock.setVisible(false);
								clock.getEquipment().setItemInMainHand(new ItemStack(MaterialX.CLOCK.getMaterial()));
								clock.setGravity(false);
				            	NMS.removeBoundingBox(clock);
				    			new BukkitRunnable() {
				    				@Override
				    				public void run() {
				    					participant.getPlayer().damage(10 + ((15 - getPlayer().getLocation().getBlock().getLightLevel()) * 0.4), getPlayer());
				    					ParticleLib.ITEM_CRACK.spawnParticle(clock.getLocation().clone().add(0, 1, 0), 0, 0, 0, 10, 0.5f, MaterialX.CLOCK);
				    					ParticleLib.ITEM_CRACK.spawnParticle(participant.getPlayer().getEyeLocation(), 0.4, 0.5, 0.4, 50, 0.35, MaterialX.CLOCK);
				    					SoundLib.ENTITY_ENDER_EYE_DEATH.playSound(clock.getLocation().clone().add(0, 1, 0), 1, 0.7f);
				    					SoundLib.ENTITY_GENERIC_EXPLODE.playSound(participant.getPlayer().getLocation());
				    					clock.remove();
				    					armorstand.remove();
				    				}
		    					}.runTaskLater(AbilityWar.getPlugin(), 20L);
								damaged.add(participant.getPlayer());
							}
						}
					}
					logMap.get(participant.getPlayer()).stop(false);
				}
			}
			damaged.clear();
			cool.start();
		}
		
	}.setPeriod(TimeUnit.SECONDS, 1).register();
	
	private class CutParticle extends AbilityTimer {

		private final Vector vector;
		private final Vectors crescentVectors;

		private CutParticle(double angle) {
			super(4);
			setPeriod(TimeUnit.TICKS, 1);
			this.vector = getPlayer().getLocation().getDirection().setY(0).normalize().multiply(0.5);
			this.crescentVectors = crescent.clone()
					.rotateAroundAxisY(-getPlayer().getLocation().getYaw())
					.rotateAroundAxis(getPlayer().getLocation().getDirection().setY(0).normalize(), (180 - angle) % 180);
		}

		@Override
		protected void run(int count) {
			Location baseLoc = getPlayer().getLocation().clone().add(vector).add(0, 1, 0);
			for (Location loc : crescentVectors.toLocations(baseLoc)) {
				ParticleLib.REDSTONE.spawnParticle(loc, RGB.of((int) (146 + (((random.nextDouble() * 2) - 1) * 20)), 254 - (random.nextInt(20)), 254 - (random.nextInt(20))));
			}
		}

	}
	
	private class LogParticle extends AbilityTimer {
		
		private List<Location> locations = new ArrayList<>();
		private final Player player;
		private Random random = new Random();
		private final RGB color = RGB.of(random.nextInt(254) + 1, random.nextInt(254) + 1, random.nextInt(254) + 1);
		
		private LogParticle(Player player) {
			super(200);
			setPeriod(TimeUnit.TICKS, 1);
			this.player = player;
			logMap.put(player, this);
		}
		
		public List<Location> getLog() {
			return locations;
		}
		
    	@Override
    	protected void run(int count) {
    		locations.add(player.getLocation());
    		if (count % 4 == 0) {
        		int momentCount = 0;
        		final ListIterator<Location> listIterator = locations.listIterator(locations.size() - 1);
        		if (!listIterator.hasPrevious()) return;
    			listIterator.previous();
    			while (listIterator.hasPrevious()) {
    				final Location base;
    				if (momentCount == 1) {
    					base = getPlayer().getLocation();
    					listIterator.previous();
    				} else {
    					base = listIterator.previous();
    				}
    				listIterator.next();
    				final Location previous = listIterator.next();
    				if (base.getWorld() != previous.getWorld() || base.distanceSquared(previous) > 36) return;
    				for (Iterator<Location> iterator = new Iterator<Location>() {
    					private final Vector vectorBetween = previous.toVector().subtract(base.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
    					private final int amount = (int) (vectorBetween.length() / 0.25);
    					private int cursor = 0;

    					@Override
    					public boolean hasNext() {
    						return cursor < amount;
    					}

    					@Override
    					public Location next() {
    						if (cursor >= amount) throw new NoSuchElementException();
    						cursor++;
    						return base.clone().add(unit.clone().multiply(cursor));
    					}
    				}; iterator.hasNext(); ) {
    					ParticleLib.REDSTONE.spawnParticle(getPlayer(), iterator.next().add(0, 1, 0), color);
    				}
    				listIterator.previous();
    				listIterator.previous();
    			}	
    		}
    	}
    	
    	@Override
    	protected void onEnd() {
    		onSilentEnd();
    	}
    	
    	@Override
    	protected void onSilentEnd() {
    		locations.clear();
    	}

	}
	
}