package RainStarSynergy;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import RainStarEffect.DimensionDistortion;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.SubscribeEvent.Priority;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.minecraft.nms.PickupStatus;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;
import daybreak.google.common.collect.ImmutableSet;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;

@AbilityManifest(name = "���� ����", rank = Rank.S, species = Species.HUMAN, explain = {
		"��7�нú� ��8- ��b���� �����f: � �����δ��� ���� �̵��� �� 5�� �ȿ� ��ũ�� ��",
		" ���� �̵� ���� ��ġ�� �ǵ��ƿɴϴ�. �� �ɷ��� ����� ���Ŀ��� $[SPEED_DURATION]�ʰ�",
		" �ż� ������ ȹ���մϴ�. $[RETURN_COOLDOWN]",
		" ���� ���� �̵��� �� ������ $[INV_DURATION]�ʰ� ������ �˴ϴ�.",
		"��7ȭ�� ���� ��8- ��a���´�� �º�!��f: Ȱ �߻� �� ������ ��ġ�� ǥ���� ����� ���� ������",
		" ���� ���� �̵����� ���� �̵� ��ġ�� �ش� ��ҷ� ������Ű�� ǥ���� �����մϴ�.",
		" ����� $[DEBUFF_DURATION]�ʰ� ���ݷ��� �����ϸ�, �̵� �ӵ��� �������ϴ�. $[ARROW_COOLDOWN]",
		"��7ö�� ��Ŭ�� ��8- ��bDive!��f: �ٶ󺸴� ������� ���� �̵��մϴ�. $[TELEPORT_COOLDOWN]",
		" ����, ��ü�� �����ϸ�, �ִ� $[RANGE_CONFIG]ĭ �ָ��� ��ϱ��� ī��Ʈ�մϴ�."
		})

public class MomentaryTrip extends Synergy implements ActiveHandler {

	public MomentaryTrip(Participant participant) {
		super(participant);
	}
	
	public static final SettingObject<Integer> INV_DURATION = synergySettings.new SettingObject<Integer>(MomentaryTrip.class,
			"invincibility-duration", 4, "# ���� ���� �� ���� �ð�", "# ����: ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public static final SettingObject<Integer> SPEED_DURATION = synergySettings.new SettingObject<Integer>(MomentaryTrip.class,
			"speed-duration", 10, "# ���� ���� �� �ż� �ð�", "# ����: ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public static final SettingObject<Integer> DEBUFF_DURATION = synergySettings.new SettingObject<Integer>(MomentaryTrip.class,
			"debuff-duration", 10, "# ǥ�� �ߵ� �� ����� �ð�", "# ����: ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public static final SettingObject<Integer> RANGE_CONFIG = synergySettings.new SettingObject<Integer>(MomentaryTrip.class,
			"range", 15, "# Dive �ڷ���Ʈ �ִ� ����", "# ����: ĭ") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public static final SettingObject<Integer> RETURN_COOLDOWN = synergySettings.new SettingObject<Integer>(MomentaryTrip.class,
			"return-cooldown", 15, "# ���� ���� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> ARROW_COOLDOWN = synergySettings.new SettingObject<Integer>(MomentaryTrip.class,
			"arrow-cooldown", 30, "# ǥ�� ���� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> TELEPORT_COOLDOWN = synergySettings.new SettingObject<Integer>(MomentaryTrip.class,
			"teleport-cooldown", 25, "# ö�� ��Ŭ�� Dive ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	private final Cooldown returnCool = new Cooldown(RETURN_COOLDOWN.getValue(), "���� ����", CooldownDecrease._25);
	private final Cooldown arrowCool = new Cooldown(ARROW_COOLDOWN.getValue(), "ǥ��");
	private final Cooldown teleCool = new Cooldown(TELEPORT_COOLDOWN.getValue(), "Dive", CooldownDecrease._50);	
	private Location previousLoc;
	private Arrow arrowmark;
	private final ActionbarChannel ac = newActionbarChannel();
	private final ActionbarChannel ac2 = newActionbarChannel();
	private static final Set<Material> nocheck;
	private Location targetblock;
	private int number = 1;
	
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
	
	static { nocheck = ImmutableSet.of(MaterialX.AIR.getMaterial(), MaterialX.GRASS.getMaterial(), MaterialX.WATER.getMaterial(),
			MaterialX.LAVA.getMaterial(), MaterialX.GLASS.getMaterial(), MaterialX.GLASS_PANE.getMaterial(), MaterialX.BLACK_STAINED_GLASS.getMaterial(),
			MaterialX.BLACK_STAINED_GLASS_PANE.getMaterial(), MaterialX.BLUE_STAINED_GLASS.getMaterial(), MaterialX.BLUE_STAINED_GLASS_PANE.getMaterial(),
			MaterialX.BROWN_STAINED_GLASS.getMaterial(), MaterialX.BROWN_STAINED_GLASS_PANE.getMaterial(), MaterialX.CYAN_STAINED_GLASS.getMaterial(),
			MaterialX.CYAN_STAINED_GLASS_PANE.getMaterial(), MaterialX.GRAY_STAINED_GLASS.getMaterial(), MaterialX.GRAY_STAINED_GLASS_PANE.getMaterial(),
			MaterialX.GREEN_STAINED_GLASS.getMaterial(), MaterialX.GREEN_STAINED_GLASS_PANE.getMaterial(), MaterialX.LIGHT_BLUE_STAINED_GLASS.getMaterial(),
			MaterialX.LIGHT_BLUE_STAINED_GLASS_PANE.getMaterial(), MaterialX.LIGHT_GRAY_STAINED_GLASS.getMaterial(), MaterialX.LIGHT_GRAY_STAINED_GLASS_PANE.getMaterial(),
			MaterialX.LIME_STAINED_GLASS.getMaterial(), MaterialX.LIME_STAINED_GLASS_PANE.getMaterial(), MaterialX.MAGENTA_STAINED_GLASS.getMaterial(),
			MaterialX.MAGENTA_STAINED_GLASS_PANE.getMaterial(), MaterialX.ORANGE_STAINED_GLASS.getMaterial(), MaterialX.ORANGE_STAINED_GLASS_PANE.getMaterial(),
			MaterialX.PINK_STAINED_GLASS.getMaterial(), MaterialX.PINK_STAINED_GLASS_PANE.getMaterial(), MaterialX.PURPLE_STAINED_GLASS.getMaterial(),
			MaterialX.PURPLE_STAINED_GLASS_PANE.getMaterial(), MaterialX.RED_STAINED_GLASS.getMaterial(), MaterialX.RED_STAINED_GLASS_PANE.getMaterial(),
			MaterialX.WHITE_STAINED_GLASS.getMaterial(), MaterialX.WHITE_STAINED_GLASS_PANE.getMaterial(), MaterialX.YELLOW_STAINED_GLASS.getMaterial(),
			MaterialX.YELLOW_STAINED_GLASS_PANE.getMaterial()); }
	
	protected void onUpdate(AbilityBase.Update update) {
	    if (update == AbilityBase.Update.RESTRICTION_SET || update == AbilityBase.Update.ABILITY_DESTROY) {
	    	if (arrowmark != null) {
				arrowmark.setGlowing(false);
				NMS.setPickupStatus(arrowmark, PickupStatus.ALLOWED);
				arrowmark = null;
	    	}
	    } 
	}
	
	private final AbilityTimer arrowchecker = new AbilityTimer() {
		
		@Override
		public void run(int count) {
			if (arrowmark != null) {
				if (arrowmark.getWorld().getWorldBorder().isInside(arrowmark.getLocation())) {
					ParticleLib.SMOKE_LARGE.spawnParticle(arrowmark.getLocation(), 0, 0, 0, 3, 0);
				} else {
					getPlayer().sendMessage("��4[��c!��4] ��3ǥ�ġ�f�� ���� ��輱 ���� ���� ��������ϴ�.");
					arrowCool.setCount(0);
					getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 1));
					arrowmark.remove();
					arrowmark = null;
				}
				if (arrowmark != null) {
					if (arrowmark.isDead()) {
						getPlayer().sendMessage("��4[��c!��4] ��bȭ���f�� ����� ��3ǥ�ġ�f�� ��������ϴ�.");
						arrowCool.setCount(0);
						getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 1));
						arrowmark = null;
					}
				}
			}
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer teleported = new AbilityTimer(100) {
		
		@Override
		public void run(int count) {
			ac.update("��5�ǵ��ư����f: ��b" + (count / 20) + "��f��");
			if (getPlayer().isSneaking()) {
				returnCool.start();
				getPlayer().teleport(previousLoc);
				SoundLib.ITEM_CHORUS_FRUIT_TELEPORT.playSound(getPlayer().getLocation(), 1, 1.5f);
				ParticleLib.DRAGON_BREATH.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 50, 0);
				PotionEffects.SPEED.addPotionEffect(getPlayer(), (SPEED_DURATION.getValue() * 20), 2, true);
				getPlayer().sendMessage("��5[��d!��5] ��b���� �����f�� �Ͽ� ������ ��ġ�� �ǵ��ư����ϴ�.");
				if (!music.isRunning()) music.start();
				teleported.stop(false);
			}
		}
		
		@Override
		public void onEnd() {
			ac.update(null);
		}
		
		@Override
		public void onSilentEnd() {
			onEnd();
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer inv = new AbilityTimer(INV_DURATION.getValue() * 20) {
		
		@Override
		public void run(int count) {
			ac2.update("��d������f: " + (count / 20) + "��");
		}
		
		@Override
		public void onEnd() {
			ac2.update(null);
		}
		
		@Override
		public void onSilentEnd() {
			onEnd();
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	@SubscribeEvent(priority = Priority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getPlayer().equals(getPlayer()) && !returnCool.isCooldown()) {
			previousLoc = e.getFrom();
			if (teleported.isRunning()) {
				teleported.setCount(100);
			} else {
				teleported.start();
			}
		}
		if (e.getPlayer().equals(getPlayer())) {
			if (inv.isRunning()) {
				inv.setCount(INV_DURATION.getValue() * 20);
			} else {
				inv.start();	
			}
		}
		if (predicate.test(e.getPlayer())) {
			if (!e.getPlayer().equals(getPlayer()) && arrowmark != null) {
				e.setTo(arrowmark.getLocation().setDirection(e.getPlayer().getLocation().getDirection()));
				e.getPlayer().sendMessage("��5[��d!��5] ��f����� ��5���� �̵���f�� ��b���� �����f���� �����Ǿ� �ְ�Ǿ����ϴ�.");
				DimensionDistortion.apply(getGame().getParticipant(e.getPlayer()), TimeUnit.SECONDS, DEBUFF_DURATION.getValue());
				SoundLib.ENTITY_ZOMBIE_VILLAGER_CONVERTED.playSound(getPlayer().getLocation(), 1, 0.7f);
				arrowmark.setGlowing(false);
				NMS.setPickupStatus(arrowmark, PickupStatus.ALLOWED);
				arrowmark = null;
			}	
		}
	}
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && inv.isRunning()) {
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
	
	@SubscribeEvent
	public void onProjectileHit(ProjectileHitEvent e) {
		if (NMS.isArrow(e.getEntity())) {
			Arrow arrow = (Arrow) e.getEntity();
			if (getPlayer().equals(arrow.getShooter()) && e.getHitEntity() == null && !arrowCool.isRunning()) {
				if (arrowmark != null) {
					arrowmark.setGlowing(false);
					NMS.setPickupStatus(arrow, PickupStatus.ALLOWED);
				}
				arrowmark = arrow;
				arrowmark.setGlowing(true);
				NMS.setPickupStatus(arrow, PickupStatus.DISALLOWED);
				if (!arrowchecker.isRunning()) {
					arrowchecker.start();
				}
				arrowCool.start();
			}
		}
	}
	
	public boolean ActiveSkill(Material material, ClickType clicktype) {
		if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK) && !teleCool.isCooldown()) {
			targetblock = getPlayer().getTargetBlock(nocheck, RANGE_CONFIG.getValue()).getLocation();
			Block lastEmpty = null;
			try {
				for (BlockIterator iterator = new BlockIterator(targetblock, 0, 2); iterator.hasNext(); ) {
					final Block block = iterator.next();
					if (!block.getType().isSolid()) {
						lastEmpty = block;
					}
				}
			} catch (IllegalStateException ignored) {
			}
			if (lastEmpty != null) {
				this.targetblock = lastEmpty.getLocation();
				getPlayer().teleport(targetblock.setDirection(getPlayer().getLocation().getDirection()));
				SoundLib.ENTITY_ZOMBIE_VILLAGER_CONVERTED.playSound(getPlayer().getLocation(), 1, 1.8f);
				return teleCool.start();
			} else {
				getPlayer().sendMessage("��4[��c!��4] ��f�ٶ󺸴� ���⿡ �̵��� �� �ִ� ���� �����ϴ�.");
			}
		}
		return false;
	}
	
	private final AbilityTimer music = new AbilityTimer() {

    	@Override
		public void run(int count) {
    		switch(number) {
    		case 1:
    			switch(count) {
    			case 1:
    			case 9: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.F));
    					break;
    			case 2: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.G));
    					break;
    			case 3: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
    					SoundLib.BELL.playInstrument(getPlayer().getLocation(), 0, Note.sharp(0, Tone.C));
    					break;
    			case 5:	SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(1, Tone.C));
						SoundLib.BELL.playInstrument(getPlayer().getLocation(), 0, Note.sharp(0, Tone.C));
						break;
    			case 11: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
    					 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(0, Tone.D));
    					 break;
    			case 13: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(1, Tone.C));
						 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 0, Note.sharp(0, Tone.D));
				 		 stop(false);
				 		 number++;
						 break;
    			}
    			break;
    		case 2:
    			switch(count) {
    			case 1: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
    					SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.F));
    					SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
						break;
    			case 4: 
    			case 12: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.G));
						 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
						 break;
    			case 6:
    			case 10: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
				 		 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
				 		 break;
    			case 8:	SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(1, Tone.C));
						SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
						break;
    			case 14: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.F));
					 	 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
				 		 stop(false);
				 		 number++;
					 	 break;
    			}
    			break;
    		case 3:
    			switch(count) {
    			case 1: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.F));
    			 		SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
    					break;
    			case 2:
    			case 6:
    			case 10:
    			case 17:
    			case 21: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.G));
						 break;
    			case 3:
    			case 7: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
		 				SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(0, Tone.C));
				 		break;
    			case 5:
    			case 9: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.F));
		 				break;
    			case 11: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
 						 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(0, Tone.D));
 						 break;
    			case 13: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.D));
    			 		 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(0, Tone.D));
 						 break;
    			case 15: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(1, Tone.C));
    					 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(0, Tone.D));
    					 break;
    			case 19: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
		 		 		 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(0, Tone.D));
		 		 		 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
		 		 		 break;
    			case 23:
    			case 24: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.A));
    					 break;
    			case 25: SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(1, Tone.C));
    			 		 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.sharp(1, Tone.G));
						 SoundLib.BELL.playInstrument(getPlayer().getLocation(), 1, Note.natural(0, Tone.C));
    					 stop(false);
    					 number = 1;
		 				 break;
    			}
    			break;
    		}
    	}
	}.setPeriod(TimeUnit.TICKS, 2).register();
}
