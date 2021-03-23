package RainStarSynergy;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.math.geometry.vector.VectorIterator;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;

@AbilityManifest(
		name = "����Ʈ ���̴�", rank = Rank.L, species = Species.HUMAN, explain = {
		"��7�нú� ��8- ��c������ ���֡�f: �ſ� ���� �ӵ��� �̵��ϸ�, �������� �ڸ��� �������ϴ�.",
		" ȭ�� �迭 ���ظ� �����ϰ�, �ڽ� �ֺ� $[RANGE]ĭ �̳��� ȭ���� ���ظ� �޴� ����ü����",
		" �ش� ȭ�� ���ظ� 2��� �޽��ϴ�. �޸��� �ٸ� �÷��̾ ���� ��������",
		" �ִ� 3�ܰ���� �� ������ �����ϰ� ����� ���� ��ȭ �ð��� 1.5��� �ø��ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��c��ȸ�� �ü���f: ���� �ڽ��� ���� ���ֺ��� ���� ��",
		" ����� �ٸ� �÷��̾�� ���� ���ط��� ����� ����� ����������,",
		" ����� ���� �ٸ� �÷��̾��� ���� ����� ����� ������ŵ�ϴ�. $[COOLDOWN]",
		"��b[��7���̵�� �����ڡ�b] ��dhorn1111"
		})

public class GhostRider extends Synergy implements ActiveHandler {

	public GhostRider(Participant participant) {
		super(participant);
	}
	
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			speed.start();
		    running.start();
		    getPlayer().setWalkSpeed(0.3f);
		    getPlayer().setFlySpeed(0.2f);
		    actionbarChannel.update("��3���� ��b0��3�ܰ�");
		    circle.start();
		} else if (update == AbilityBase.Update.RESTRICTION_SET || update == Update.ABILITY_DESTROY) {
			for (Block block : blocks) {
				if (block.getType() == Material.FIRE) block.setType(Material.AIR);
			}
			getPlayer().setSprinting(false);
		    getPlayer().setWalkSpeed(0.2F);
		    getPlayer().setFlySpeed(0.1F);
			getPlayer().setFireTicks(0);
			circle.stop(false);
		}
	}

	public static final SettingObject<Integer> RANGE = synergySettings.new SettingObject<Integer>(GhostRider.class,
			"range", 5, "# ȭ�� ���� ���� ����") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	public static final SettingObject<Integer> COOLDOWN = synergySettings.new SettingObject<Integer>(GhostRider.class, 
			"COOLDOWN", 100, "# ��Ÿ��") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
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
			if (entity instanceof ArmorStand) {
				return false;
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	private final Predicate<Entity> subpredicate = new Predicate<Entity>() {
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
			if (entity instanceof ArmorStand) {
				return false;
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	private final AbilityTimer speed = new AbilityTimer() {
		@Override
		protected void run(int count) {
			PotionEffects.SPEED.addPotionEffect(getPlayer(), 20, 1, true);
		}
	}.register();

	private Map<Player, Double> attackCounter = new HashMap<>();
	private Map<Player, Integer> killCounter = new HashMap<>();
	
	private final Deque<Block> blocks = new LinkedList<>();
	private final Set<Block> blockSet = new HashSet<>();
	private int range = RANGE.getValue();
	private final Cooldown cool = new Cooldown(COOLDOWN.getValue());
	
	private LivingEntity pentity = null;
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	
	private final Attacking attacking = new Attacking();
	
	public boolean ActiveSkill(Material material, ClickType clicktype) {
		if (material == Material.IRON_INGOT && clicktype == ClickType.RIGHT_CLICK && !cool.isCooldown()) {
			if (LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 30, predicate) != null) {
				Player player = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 30, predicate);
				if (LocationUtil.getEntityLookingAt(Player.class, player, 30, subpredicate) != null) {
					if (LocationUtil.getEntityLookingAt(Player.class, player, 30, subpredicate).equals(getPlayer())) {
						if (killCounter.containsKey(player)) {
							Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, killCounter.get(player) * 50);
						}
						if (attackCounter.containsKey(player)) {
							player.damage(Math.min(20, (attackCounter.get(player) * 0.05)), getPlayer());
						}
						for (Location loc : Line.between(getPlayer().getEyeLocation(), player.getEyeLocation(), (int) Math.min(250, 5 * Math.sqrt(getPlayer().getEyeLocation().distanceSquared(player.getEyeLocation())))).toLocations(getPlayer().getEyeLocation())) {
							ParticleLib.FLAME.spawnParticle(loc, 0, 0, 0, 1, 0);
							ParticleLib.DRIP_LAVA.spawnParticle(loc, 0, 0, 0, 1, 0);
							ParticleLib.LAVA.spawnParticle(loc, 0, 0, 0, 1, 0);
						}
						return cool.start();	
					}
				}
			}
		}
		return false;
	}
	
	private final AbilityTimer running = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (getPlayer().isSprinting()) {
				attacking.start();
			} else if (!getPlayer().isSprinting() && attacking.isRunning() && attacking.stack >= 1) {
				attacking.stack--;
				attacking.stop(true);
				actionbarChannel.update("��3���� ��b" + attacking.stack + "��3�ܰ�, ��6���� �ð���f: ��f����");
				if (attacking.stack == 0) {
					getPlayer().setWalkSpeed(0.3f);
					getPlayer().setFlySpeed(0.2f);
				} else if (attacking.stack == 1) {
					getPlayer().setWalkSpeed(0.35f);
					getPlayer().setFlySpeed(0.25f);
				} else if (attacking.stack == 2) {
					getPlayer().setWalkSpeed(0.4f);
					getPlayer().setFlySpeed(0.3f);
				} else if (attacking.stack == 3) {
					getPlayer().setWalkSpeed(0.45f);
					getPlayer().setFlySpeed(0.35f);
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	
	private final AbilityTimer circle = new AbilityTimer() {
		
		private VectorIterator iterator;
		
    	@Override
		public void onStart() {
    		this.iterator = Circle.infiniteIteratorOf(range, (range * 10));
    	}
		
    	@Override
		public void run(int i) {
    		for (int j = 0; j < 5; j++) {
    			Location loc = getPlayer().getLocation().clone().add(iterator.next());
    			loc.setY(LocationUtil.getFloorYAt(loc.getWorld(), getPlayer().getLocation().getY(), loc.getBlockX(), loc.getBlockZ()) + 0.1);
    			ParticleLib.FLAME.spawnParticle(loc, 0, 0, 0, 1, 0.2);	
    		}
    	}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();

	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerMove(PlayerMoveEvent e) {
		final Block to = e.getTo().getBlock(), below = to.getRelative(BlockFace.DOWN);
		if ((to.isEmpty() || to.getType() == Material.SNOW) && below.getType().isSolid()) {
			to.setType(Material.FIRE);
			if (to.getType() == Material.FIRE) {
				blocks.add(to);
				blockSet.add(to);
				if (blocks.size() >= 20) {
					final Block removed = blocks.removeFirst();
					blockSet.remove(removed);
					removed.setType(Material.AIR);
				}
			}
		}
		if (below.getType().equals(Material.SNOW_BLOCK)) {
			below.setType(Material.DIRT);
		} else if (below.getType().equals(Material.PACKED_ICE) || below.getType().equals(Material.ICE) || MaterialX.FROSTED_ICE.compare(below) || MaterialX.BLUE_ICE.compare(below)) {
			below.setType(Material.WATER);
		}
	}

	@SubscribeEvent
	private void onBlockSpread(final BlockSpreadEvent e) {
		if (blockSet.contains(e.getSource()) && e.getNewState().getType() == Material.FIRE) {
			e.setCancelled(true);
		}
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		final DamageCause cause = e.getCause();
		if (cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK) || cause.equals(DamageCause.LAVA) || cause.equals(DamageCause.HOT_FLOOR)) {
			if (e.getEntity().equals(getPlayer())) {
				e.setCancelled(true);	
			} else {
				for (Damageable damageable : LocationUtil.getNearbyEntities(Damageable.class, getPlayer().getLocation(), range, range, predicate)) {
					if (e.getEntity().equals(damageable)) {
						e.setDamage(e.getDamage() * 2);
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
		if (e.getDamager() instanceof Player) {
			if (!e.getDamager().equals(getPlayer())) {
				if (attackCounter.containsKey(e.getDamager())) {
					attackCounter.put((Player) e.getDamager(), attackCounter.get(e.getDamager()) + e.getFinalDamage());
				} else {
					attackCounter.put((Player) e.getDamager(), e.getFinalDamage());
				}
			}	
		}
	}
	
	@SubscribeEvent
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.getEntity().getKiller() != null && !e.getEntity().getKiller().equals(getPlayer())) {
			if (killCounter.containsKey(e.getEntity().getKiller())) {
				killCounter.put(e.getEntity().getKiller(), killCounter.get(e.getEntity().getKiller()) + 1);
			} else {
				killCounter.put(e.getEntity().getKiller(), 1);
			}
		}
	}
	
	private class Attacking extends AbilityTimer {
		
		int stack = 0;
		
		private Attacking() {
			super(TaskType.REVERSE, 200);
			setPeriod(TimeUnit.TICKS, 1);
		}
		
		@Override
		protected void run(int count) {
			actionbarChannel.update("��3���� ��b" + stack + "��3�ܰ�, ��6���� �ð���f: " + getFixedCount() + "��");
			
			if (!getPlayer().isDead() && getPlayer().isSprinting()) {		
				if (pentity == null) {
					for (LivingEntity livingentity : LocationUtil.getConflictingEntities(LivingEntity.class, getPlayer(), predicate)) {
						pentity = livingentity;
						break;
					}
				} else {
					if (!LocationUtil.isConflicting(getPlayer(), pentity)) {
						Damages.damageMagic(pentity, getPlayer(), false, 3);
						pentity.setFireTicks((int) (pentity.getFireTicks() * 1.5));
						pentity = null;
						addStack();
					}
				}
			}
 			
		}
		
		private void addStack() {
			if (stack < 3) {
				stack++;
			}
			if (isRunning()) {
				setCount(200);
				actionbarChannel.update("��3���� ��b" + stack + "��3�ܰ�, ��6���� �ð���f: " + getFixedCount() + "��");
			}
			if (stack == 1) {
				getPlayer().setWalkSpeed(0.325f);
				getPlayer().setFlySpeed(0.225f);
				SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1);
				ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.3);
			} else if (stack == 2) {
				getPlayer().setWalkSpeed(0.35f);
				getPlayer().setFlySpeed(0.25f);
				SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1.3f);
				ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.4);
			} else if (stack == 3) {
				getPlayer().setWalkSpeed(0.4f);
				getPlayer().setFlySpeed(0.3f);
				SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1.5f);
				ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.5);
			}
		}
		
		@Override
		protected void onEnd() {
			actionbarChannel.update("��6���� �ð� ��f����");
			stack = 0;
			getPlayer().setWalkSpeed(0.3f);
			getPlayer().setFlySpeed(0.2f);
		}
		
		@Override
		protected void onSilentEnd() {
		}	
	}
	
}