package RainStarSynergy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import RainStarEffect.Agro;
import RainStarEffect.BackseatGaming;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;

@AbilityManifest(
		name = "�μ���", rank = Rank.C, species = Species.HUMAN, explain = {
		"�� �ù޳�? ��������������",
		"��7ö�� ��Ŭ�� ��8- ��c���ߡ�f: ��� �÷��̾ �� ��ġ�� ���� �����ϸ�",
		" ���߿� �ɸ� �÷��̾�� ���� Ÿ���� �� �߰� ���ظ� �ݴϴ�. $[RIGHT_COOL]",
		"��7ö�� ��Ŭ�� ��8- ��c�����f: $[DURATION]�ʰ� ��� ���°� �Ǿ� �������� ���� Ÿ�� ��",
		" ���ظ� ���� �ʰ� ������ ��ġ�� �ڷ���Ʈ�մϴ�. ���� Ÿ���� ����",
		" �κ��丮 ������ �ڼ��̰� �˴ϴ�. $[LEFT_COOL]",
		" �� ���µ��� �ڽŵ� �ٸ� �÷��̾ ���� �� �����ϴ�.",
		"��7�нú� ��8- ��cƼ����f: �������� ���� �ٶ� �� ��ũ���⸦ ��Ÿ�� ������",
		" ����� 0.5�ʰ� ������Ű�� ���ظ� �����ϴ�.",
		"��7��� ��8- ��c�Ƽ���f: ���� ���� �÷��̾ Ư�� �ൿ�� ���� ������",
		" �װ� �׷��� �ϴ� �� �ƴѵ�~�� ������ ��� �Ǹ���մϴ�."
		})

public class BadManner extends Synergy implements ActiveHandler {
	
	public BadManner(Participant participant) {
		super(participant);
	}
	
	protected void onUpdate(Update update) {
	    if (update == Update.RESTRICTION_CLEAR) {
	    	passive.start();
	    } 
	}
	
	private final Cooldown rightcool = new Cooldown(RIGHT_COOL.getValue(), "����", CooldownDecrease._25);
	private final Cooldown leftcool = new Cooldown(LEFT_COOL.getValue(), "���", CooldownDecrease._50);
	
	private Map<Player, Integer> teabaggingcount = new HashMap<>();
	private Set<Player> bagged = new HashSet<>();
	
	public boolean randomTeleport() {
		final Location playerLocation = getPlayer().getLocation().clone();
		final Random random = new Random();
		double radian = Math.toRadians(random.nextDouble()*360);
		double sin = FastMath.sin(radian), cos = FastMath.cos(radian);
		double x = playerLocation.getX()+(cos * random.nextDouble() * 15);
		double z = playerLocation.getZ()+(sin * random.nextDouble() * 15);

		Location l = new Location(getPlayer().getWorld(), x, 100, z);
		l.setY(LocationUtil.getFloorYAt(getPlayer().getWorld(), l.getY(), l.getBlockX(), l.getBlockZ()) + 0.1);

		if (!getPlayer().getWorld().getWorldBorder().isInside(l)) {
			return randomTeleport();
		}
		return getPlayer().teleport(l);
	}
	
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler &&
								((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(
							entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant)
							|| (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
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
						|| (getGame() instanceof DeathManager.Handler &&
								((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(
							entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant)
							|| (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	public static final SettingObject<Integer> RIGHT_COOL 
	= synergySettings.new SettingObject<Integer>(BadManner.class,
			"right-cooldown", 20, "# ��Ŭ�� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> LEFT_COOL
	= synergySettings.new SettingObject<Integer>(BadManner.class,
			"left-cooldown", 80, "# ��Ŭ�� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> DURATION
	= synergySettings.new SettingObject<Integer>(BadManner.class,
			"duration", 10, "# ��� ���ӽð�") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	@Override
	public boolean ActiveSkill(Material material, ClickType clicktype) {
		if (material == Material.IRON_INGOT) {
			if (clicktype == ClickType.RIGHT_CLICK) {
				if (!rightcool.isCooldown()) {
					for (Participant participant : getGame().getParticipants()) {
		    			if (predicate.test(participant.getPlayer())) {
		        			Player player = participant.getPlayer();
		        			Vector direction = getPlayer().getEyeLocation().toVector().subtract(player.getEyeLocation().toVector());
		        			float yaw = LocationUtil.getYaw(direction), pitch = LocationUtil.getPitch(direction);
		        			for (Player p : Bukkit.getOnlinePlayers()) {
								NMS.rotateHead(p, player, yaw, pitch);	
		        			}
							Agro.apply(participant, TimeUnit.TICKS, 100, getPlayer(), 2);
		    			}
					}
					return rightcool.start();
				}
			} else {
				if (!leftcool.isCooldown() && !skill.isDuration()) {
					SoundLib.ENTITY_ZOMBIE_VILLAGER_CONVERTED.playSound(getPlayer(), 1, 1.5f);
					return skill.start();
				}
			}
		}
		return false;
	}
	
    private final AbilityTimer passive = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
    		for (Participant participant : getGame().getParticipants()) {
    			if (predicate.test(participant.getPlayer())) {
        			Player player = participant.getPlayer();
        			if (getPlayer().equals(LocationUtil.getEntityLookingAt(Player.class, player, 10, predicate2))) {
        				if (getPlayer().isSneaking() && !bagged.contains(player)) {
        					bagged.add(player);
        					if (teabaggingcount.containsKey(player)) {
        						teabaggingcount.put(player, teabaggingcount.get(player) + 1);
        					} else {
        						teabaggingcount.put(player, 1);
        					}
        					if (teabaggingcount.get(player) >= 5) {
            					player.damage(3, getPlayer());
            					Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, 10);
        						Random random = new Random();
        						if (random.nextInt(10) == 0) {
        							SoundLib.ENTITY_WITCH_AMBIENT.playSound(player, 1, 1.5f);
        							switch(random.nextInt(8)) {
        							case 0: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c���� �� �ù޳�?");
        							break;
        							case 1: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��cez");
        							break;
        							case 2: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c������?");
        							break;
        							case 3: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c��");
        							break;
        							case 4: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c��~");
        							break;
        							case 5: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c���ƾ��? ��¼����~");
        							break;
        							case 6: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c��������~");
        							break;
        							case 7: player.sendMessage("��3[��b�ɷ¡�3] ��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��cũũ��滧��~");
        							break;
        							}
        						}
        						teabaggingcount.put(player, 0);
        					}
        				}
        			}
    			}
    		}
    		if (!getPlayer().isSneaking()) {
    			bagged.clear();
    		}
    	}
    
    }.setPeriod(TimeUnit.TICKS, 1).register();
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (skill.isRunning()) {
			if (e.getEntity().equals(getPlayer()) && e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				e.setCancelled(true);
				SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer().getLocation(), 1, 1.5f);
				ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 50, 0);
				randomTeleport();
				SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer().getLocation(), 1, 1.5f);
				ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 50, 0);
				p.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + p.getName() + "��7: ��c�ȵ��� �ȵ���~ ���� �� ����~");
				Inventory inv = p.getPlayer().getInventory();
				List<ItemStack> slots = new ArrayList<>();
				slots.add(inv.getItem(0));
				slots.add(inv.getItem(1));
				slots.add(inv.getItem(2));
				slots.add(inv.getItem(3));
				slots.add(inv.getItem(4));
				slots.add(inv.getItem(5));
				slots.add(inv.getItem(6));
				slots.add(inv.getItem(7));
				slots.add(inv.getItem(8));
				Collections.shuffle(slots);
				for (int a=0;a<9;a++){ inv.setItem(a, slots.get(a)); }
			}
			if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player) {
				e.setCancelled(true);
			}
			if (e.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) e.getDamager();
				if (getPlayer().equals(projectile.getShooter())) {
					e.setCancelled(true);
				}
				if (e.getEntity().equals(getPlayer())) {
					if (projectile.getShooter() instanceof Player) {
						Player p = (Player) projectile.getShooter();
						e.setCancelled(true);
						SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer().getLocation(), 1, 1.5f);
						ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 50, 0);
						randomTeleport();
						SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer().getLocation(), 1, 1.5f);
						ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 50, 0);
						p.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + p.getName() + "��7: ��c�ȵ��� �ȵ���~ ���� �� ����~");
						Inventory inv = p.getPlayer().getInventory();
						List<ItemStack> slots = new ArrayList<>();
						slots.add(inv.getItem(0));
						slots.add(inv.getItem(1));
						slots.add(inv.getItem(2));
						slots.add(inv.getItem(3));
						slots.add(inv.getItem(4));
						slots.add(inv.getItem(5));
						slots.add(inv.getItem(6));
						slots.add(inv.getItem(7));
						slots.add(inv.getItem(8));
						Collections.shuffle(slots);
						for (int a=0;a<9;a++){ inv.setItem(a, slots.get(a)); }
					}
				}
			}
		}
		if (e.getEntity().equals(getPlayer()) && getPlayer().getHealth() - e.getFinalDamage() <= 0 && e.getDamager() instanceof Player) {
			BackseatGaming.apply(getGame().getParticipant((Player) e.getDamager()), TimeUnit.SECONDS, 1);
		}
	}
	
	private final Duration skill = new Duration(200, leftcool, "���") {

		@Override
		protected void onDurationProcess(int seconds) {
		}
		
	}.setPeriod(TimeUnit.TICKS, 1);
	
}
