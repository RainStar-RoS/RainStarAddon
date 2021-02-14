package RainStarAbility;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

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
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.ProgressBar;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.PotionEffects;
import daybreak.abilitywar.utils.library.item.EnchantLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.google.common.base.Predicate;
import daybreak.google.common.collect.ImmutableSet;

@AbilityManifest(name = "��ȣõ��", rank = Rank.S, species = Species.OTHERS, explain = {
		"��7Ȱ ��Ŭ�� ��8- ��c������Ʈ��f: �ٶ󺸴� ������ �������� ������ ȭ���� �߻��մϴ�.",
		" ������ ȭ���� ������ �� �ٽ� ƨ�� �������� ��ġ���� �ǵ��ƿ���,",
		" ��ȣ ���ÿ� ����� ���� ����� ������ŵ�ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��b������f: $[DurationConfig]�ʰ� ����� �þ�� ������ �����մϴ�.",
		" ���� ������ �ڽ��� ������ �� �� �ɷ����� ���� ����� ȸ�� ȿ���� �޽��ϴ�.",
		" �ٸ� �÷��̾ �𵥵峪 ��Ÿ ������ �ƴ� �� 50%�� ȸ��ȿ���� ������",
		" �𵥵��� ��� ���������� ���ظ� �����ϴ�. $[CooldownConfig]",
		" �� ȿ���� ü���� 20% ���Ͽ��� �÷��̾ ȸ���� �� �� ��ȣ ������ ����ϴ�."
		})

@Tips(tip = {
        "��ȣõ��� ������ �Ŵ� Ȱ ���ݰ� ü�� ȸ�� �ʵ��� ������",
        "���������� ���� ���� �� �ִ� ���Ÿ� �����Դϴ�. ������ ȭ����",
        "�ٶ󺸴� �������� �������θ� �̵��Ѵٴ� ���� ���������� ���",
        "Ȱ������ ��� �ʿ� ���� ����ϸ�, ���� ��ġ���� ���� ���ƿ���",
        "1.2���� �߰� ���ظ� ���� �� �ִٴ� ���� �پ�ϴ�.",
        "���� ���� �ɷ����� ȸ�� �ʵ带 ������, ü���� ȸ���ϰ� �𵥵� ��������",
        "������ ���ظ� ���� �� �ֽ��ϴ�. �̷� ������ �ƹ��ɷ� �� �ɷ�",
        "�����ڵ��� �� ���Ѿ� �ϴ� ��ȣõ���Դϴ�."
}, strong = {
        @Description(subject = "���Ÿ���", explain = {
                "���� �� ���� ������ ���� ������ ȭ����",
                "���Ÿ� ������ �ٽ��Դϴ�."
        }),
        @Description(subject = "���� �� ����", explain = {
                "���� �� �� ������ ������ �������� ������",
                "ȸ���� �� ���ط��� �����մϴ�."
        }),
        @Description(subject = "�𵥵���� ����", explain = {
                "�𵥵忡�Դ� ���� �ְ� ���Դ� ���� �ָ�",
                "������ �����ϰ� ����� �� �ֽ��ϴ�."
        })
}, weak = {
        @Description(subject = "���� ���̰� ���� ����", explain = {
                "��ȣõ���� ������ ȭ���� ���� �������θ� �̵���",
                "�ٽ� ƨ�� ���ƿ������ y�������� �̵��� �� �����ϴ�.",
                "���� ���� ���� ���̴� ��ȣõ�翡�� ū ���Դϴ�."
        }),
        @Description(subject = "ȸ�� �ʵ� ����", explain = {
                "50%�� �����ϱ� ������ �ٸ� �÷��̾ ȸ���� ��",
                "�ִٴ� ���� �����ؾ� �մϴ�."
        })
}, stats = @Stats(offense = Level.FOUR, survival = Level.THREE, crowdControl = Level.THREE, mobility = Level.ZERO, utility = Level.THREE), difficulty = Difficulty.HARD)

public class GuardianAngel extends AbilityBase implements ActiveHandler {

	public GuardianAngel(Participant participant) {
		super(participant);
	}
	
	private static final Set<Material> bows;
	private final Set<Participant> gods = new HashSet<>();
	private final Set<Player> healed = new HashSet<>();
	private Bullet bullet = null;
	private Bullet2 bullet2 = null;
	private AbilityTimer reload = null;
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final ActionbarChannel ac = newActionbarChannel();
	private static final RGB COLOR = RGB.of(254, 252, 206);
	private static final RGB COLOR2 = RGB.of(206, 237, 244);
	private int stack = 0;
	private Location center = null;
	private double currentRadius;
	private int sound = 0;
	private static final Note F = Note.natural(0, Tone.F), FS = Note.sharp(0, Tone.F), HF = Note.natural(1, Tone.F), HGS = Note.sharp(1, Tone.G), HAS = Note.sharp(1, Tone.A), HC = Note.natural(1, Tone.C), HCS = Note.sharp(1, Tone.C),
			HDS = Note.sharp(1, Tone.D), HFS = Note.sharp(2, Tone.F);

	static {
		if (MaterialX.CROSSBOW.isSupported()) {
			bows = ImmutableSet.of(MaterialX.BOW.getMaterial(), MaterialX.CROSSBOW.getMaterial());
		} else {
			bows = ImmutableSet.of(MaterialX.BOW.getMaterial());
		}
	}
	
	protected void onUpdate(AbilityBase.Update update) {
	    if (update == AbilityBase.Update.RESTRICTION_CLEAR) {
	    	checkgod.start();
	    	ac.update("��b��ȣ ���á�f: " + stack);
	    }
	}
	
    private final AbilityTimer checkgod = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
			for (Participant participants : getGame().getParticipants()) {
				if (participants.hasAbility()) {
					AbilityBase ab = participants.getAbility();
					if (ab.getClass().equals(Mix.class)) {
						Mix mix = (Mix) ab;
						final Mix myAbility = (Mix) getParticipant().getAbility();
						if (mix.hasSynergy()) {
							if (mix.getSynergy().getSpecies().equals(Species.GOD) && !gods.contains(participants)) gods.add(participants);
							else if (!mix.getSynergy().getSpecies().equals(Species.GOD) && gods.contains(participants)) gods.remove(participants);
						} else {
							if (mix.getFirst() != null && mix.getSecond() != null) {
								if (GuardianAngel.this.equals(myAbility.getFirst())) {
									if (mix.getFirst().getSpecies().equals(Species.GOD) && !gods.contains(participants)) gods.add(participants);
									else if (!mix.getFirst().getSpecies().equals(Species.GOD) && gods.contains(participants)) gods.remove(participants);
								} else if (GuardianAngel.this.equals(myAbility.getSecond())) {
									if (mix.getSecond().getSpecies().equals(Species.GOD) && !gods.contains(participants)) gods.add(participants);
									else if (!mix.getSecond().getSpecies().equals(Species.GOD) && gods.contains(participants)) gods.remove(participants);
								}		
							}
						} 
					} else if (ab.getSpecies().equals(Species.GOD) && !gods.contains(participants)) gods.add(participants);
					else if (!ab.getSpecies().equals(Species.GOD) && gods.contains(participants)) gods.remove(participants);
				} else if (!participants.hasAbility() && gods.contains(participants)) {
					gods.remove(participants);
				}
			}
    	}
    
    }.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final Predicate<Entity> mainpredicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity instanceof Player) {
				if (entity.equals(getPlayer())) return true;
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	private final Cooldown cool = new Cooldown(CooldownConfig.getValue());
	
	private final Duration sanctuary = new Duration(DurationConfig.getValue() * 20, cool) {
		
		@Override
		public void onDurationStart() {
			center = getPlayer().getLocation();
			currentRadius = 1;
		}
		
		@Override
		public void onDurationProcess(int count) {
			double playerY = getPlayer().getLocation().getY();
			if (currentRadius < 15) currentRadius += 0.1;
			for (Iterator<Location> iterator = Circle.iteratorOf(center, currentRadius, (int) Math.min(currentRadius * 10, 80)); iterator.hasNext(); ) {
				Location loc = iterator.next();
				loc.setY(LocationUtil.getFloorYAt(loc.getWorld(), playerY, loc.getBlockX(), loc.getBlockZ()) + 0.1);
				ParticleLib.SNOW_SHOVEL.spawnParticle(loc, 0, 0, 0, 1, 0);
			}
			if (count % 4 == 0 && sound <= 15) {
				final Note note;
				sound++;
				switch(sound) {
				case 1: note = FS;
						break;
				case 2:
				case 13:
						note = HAS;
						break;
				case 3:
				case 7:
						note = HC;
						break;
				case 4:
				case 8:
				case 10:
						note = HCS;
						break;
				case 5: note = F;
						break;
				case 6:
				case 9:
						note = HGS;
						break;
				case 11:
				case 16:
						note = HDS;
						break;
				case 12:
				case 15:
						note = HF;
						break;
				case 14:
						note = HFS;
						break;
				default:
						note = null;
						break;
				}
				for (Player player : LocationUtil.getNearbyEntities(Player.class, center, 20, 20, mainpredicate)) {
					if (getGame().getParticipant(player.getUniqueId()).hasAbility()) {
						AbilityBase ab = getGame().getParticipant(player.getUniqueId()).getAbility();
						if (ab.getClass().equals(Mix.class)) {
							Mix mix = (Mix) ab;
							final Mix myAbility = (Mix) getParticipant().getAbility();
							if (GuardianAngel.this.equals(myAbility.getFirst())) {
								if (player.equals(getPlayer()) || mix.getFirst().getSpecies().equals(Species.GOD) || mix.getFirst().getSpecies().equals(Species.SPECIAL)) SoundLib.CHIME.playInstrument(player, note);
								if (mix.getFirst().getSpecies().equals(Species.DEMIGOD)) SoundLib.PIANO.playInstrument(player, note);
								if (mix.getFirst().getSpecies().equals(Species.HUMAN)) SoundLib.BELL.playInstrument(player, note);
								if (mix.getFirst().getSpecies().equals(Species.ANIMAL)) SoundLib.GUITAR.playInstrument(player, note);
								if (mix.getFirst().getSpecies().equals(Species.UNDEAD)) SoundLib.BASS_GUITAR.playInstrument(player, note);
							} else if (GuardianAngel.this.equals(myAbility.getSecond())) {
								if (player.equals(getPlayer()) || mix.getSecond().getSpecies().equals(Species.GOD)) SoundLib.CHIME.playInstrument(player, note);
								if (mix.getSecond().getSpecies().equals(Species.DEMIGOD)) SoundLib.PIANO.playInstrument(player, note);
								if (mix.getSecond().getSpecies().equals(Species.HUMAN)) SoundLib.BELL.playInstrument(player, note);
								if (mix.getSecond().getSpecies().equals(Species.ANIMAL)) SoundLib.GUITAR.playInstrument(player, note);
								if (mix.getSecond().getSpecies().equals(Species.UNDEAD)) SoundLib.BASS_GUITAR.playInstrument(player, note);
							}
						} else {
							if (player.equals(getPlayer()) || ab.getSpecies().equals(Species.GOD)) SoundLib.CHIME.playInstrument(player, note);
							if (ab.getSpecies().equals(Species.DEMIGOD)) SoundLib.PIANO.playInstrument(player, note);
							if (ab.getSpecies().equals(Species.HUMAN)) SoundLib.BELL.playInstrument(player, note);
							if (ab.getSpecies().equals(Species.ANIMAL)) SoundLib.GUITAR.playInstrument(player, note);
							if (ab.getSpecies().equals(Species.UNDEAD)) SoundLib.BASS_GUITAR.playInstrument(player, note);
						}
					} else SoundLib.CHIME.playInstrument(player, note);
				}
			}
			for (Player player : LocationUtil.getEntitiesInCircle(Player.class, center, currentRadius, mainpredicate)) {
				PotionEffects.GLOWING.addPotionEffect(player, 4, 0, true);
				double health = Math.min(0.01 + (gods.size() * 0.01), 0.1);
				if (getGame().getParticipant(player.getUniqueId()).hasAbility()) {
					AbilityBase ab = getGame().getParticipant(player.getUniqueId()).getAbility();
					if (ab.getClass().equals(Mix.class)) {
						Mix mix = (Mix) ab;
						final Mix myAbility = (Mix) getParticipant().getAbility();
						if (GuardianAngel.this.equals(myAbility.getFirst())) {
							if (player.equals(getPlayer())) {
		    					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), health, RegainReason.CUSTOM);
		    					Bukkit.getPluginManager().callEvent(event);
		    					if (!event.isCancelled()) {
		    						Healths.setHealth(getPlayer(), health + getPlayer().getHealth());
		    					}
							} else if (mix.getFirst().getSpecies().equals(Species.GOD) || mix.getFirst().getSpecies().equals(Species.DEMIGOD) || mix.getFirst().getSpecies().equals(Species.HUMAN) || mix.getFirst().getSpecies().equals(Species.ANIMAL)) {
		    					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), (health / 2), RegainReason.CUSTOM);
		    					Bukkit.getPluginManager().callEvent(event);
		    					if (!event.isCancelled()) {
		    						Healths.setHealth(player, (health / 2) + player.getHealth());
		    						if (!healed.contains(player) && player.getHealth() <= (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 5)) {
		    							stack++;
		    							ac.update("��b��ȣ ���á�f: " + stack);
		    							healed.add(player);
		    						}
		    					}
							} else if (mix.getFirst().getSpecies().equals(Species.UNDEAD)) {
								if (count % 20 == 0) player.damage((health * 100) + 1, getPlayer());
							}
						} else if (GuardianAngel.this.equals(myAbility.getSecond())) {
							if (player.equals(getPlayer())) {
		    					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), health, RegainReason.CUSTOM);
		    					Bukkit.getPluginManager().callEvent(event);
		    					if (!event.isCancelled()) {
		    						Healths.setHealth(getPlayer(), health + getPlayer().getHealth());
		    					}
							} else if (mix.getSecond().getSpecies().equals(Species.GOD) || mix.getSecond().getSpecies().equals(Species.DEMIGOD) || mix.getSecond().getSpecies().equals(Species.HUMAN) || mix.getSecond().getSpecies().equals(Species.ANIMAL)) {
		    					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), (health / 2), RegainReason.CUSTOM);
		    					Bukkit.getPluginManager().callEvent(event);
		    					if (!event.isCancelled()) {
		    						Healths.setHealth(player, (health / 2) + player.getHealth());
		    						if (!healed.contains(player) && player.getHealth() <= (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 5)) {
		    							stack++;
		    							ac.update("��b��ȣ ���á�f: " + stack);
		    							healed.add(player);
		    						}
		    					}
							} else if (mix.getSecond().getSpecies().equals(Species.UNDEAD)) {
								if (count % 20 == 0) player.damage((health * 100) + 1, getPlayer());
							}
						}
					} else {
						if (player.equals(getPlayer())) {
	    					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), health, RegainReason.CUSTOM);
	    					Bukkit.getPluginManager().callEvent(event);
	    					if (!event.isCancelled()) {
	    						Healths.setHealth(getPlayer(), health + getPlayer().getHealth());
	    					}
						} else if (ab.getSpecies().equals(Species.GOD) || ab.getSpecies().equals(Species.DEMIGOD) || ab.getSpecies().equals(Species.HUMAN) || ab.getSpecies().equals(Species.ANIMAL)) {
	    					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), (health / 2), RegainReason.CUSTOM);
	    					Bukkit.getPluginManager().callEvent(event);
	    					if (!event.isCancelled()) {
	    						Healths.setHealth(player, (health / 2) + player.getHealth());
	    						if (!healed.contains(player) && player.getHealth() <= (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 5)) {
	    							stack++;
	    							ac.update("��b��ȣ ���á�f: " + stack);
	    							healed.add(player);
	    						}
	    					}
						} else if (ab.getSpecies().equals(Species.UNDEAD)) {
							if (count % 20 == 0) player.damage((health * 100) + 1, getPlayer());
						}
					}
				}
			}
		}
		
		@Override
		public void onDurationEnd() {
			onDurationSilentEnd();
		}
		
		@Override
		public void onDurationSilentEnd() {
			sound = 0;
			center = null;
			healed.clear();
		}
		
	}.setPeriod(TimeUnit.TICKS, 1);
	
	public static final SettingObject<Integer> CooldownConfig 
	= abilitySettings.new SettingObject<Integer>(GuardianAngel.class,
			"Cooldown", 80, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> DurationConfig 
	= abilitySettings.new SettingObject<Integer>(GuardianAngel.class,
			"Duration", 10, "# ���� �ð�") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK) &&
	    		!cool.isCooldown() && !sanctuary.isDuration()) {
	    	sanctuary.start();
	    	return true;
	    }
	    return false;
	}
	
	@SubscribeEvent(onlyRelevant = true)
	private void onPlayerInteract(PlayerInteractEvent e) {
		if ((e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) && e.getItem() != null && bows.contains(e.getItem().getType())) {
			getPlayer().updateInventory();
			if (bullet == null && bullet2 == null) {
				if (reload == null) {
					if (getPlayer().getInventory().contains(Material.ARROW)) {
						if (bows.contains(getPlayer().getInventory().getItemInMainHand().getType())) {
							final ItemStack mainhand = getPlayer().getInventory().getItemInMainHand();
							if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && !mainhand.containsEnchantment(Enchantment.ARROW_INFINITE)) ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
							new Bullet(getPlayer(), getPlayer().getLocation().clone().add(0, 1.5, 0), getPlayer().getLocation().getDirection().setY(0).normalize(), mainhand.getEnchantmentLevel(Enchantment.ARROW_DAMAGE), 10, COLOR).start();
						} else if (bows.contains(getPlayer().getInventory().getItemInOffHand().getType())) {
							final ItemStack offhand = getPlayer().getInventory().getItemInOffHand();
							if (!getPlayer().getGameMode().equals(GameMode.CREATIVE) && !offhand.containsEnchantment(Enchantment.ARROW_INFINITE)) ItemLib.removeItem(getPlayer().getInventory(), Material.ARROW, 1);
							new Bullet(getPlayer(), getPlayer().getLocation().clone().add(0, 1.5, 0), getPlayer().getLocation().getDirection().setY(0).normalize(), offhand.getEnchantmentLevel(Enchantment.ARROW_DAMAGE), 10, COLOR).start();
						}
					} else {
						getPlayer().sendMessage("ȭ���� �����մϴ�.");
					}
				} else {
					getPlayer().sendMessage("��b������ ��f���Դϴ�.");
				}
			} else {
				getPlayer().sendMessage("��b������ ȭ���f�� ȸ������ �ʾҽ��ϴ�.");
			}
		}
	}
	
	@SubscribeEvent(onlyRelevant = true)
	private void onEntityShootBow(EntityShootBowEvent e) {
		e.setCancelled(true);
	}
	
	public class Bullet extends AbilityTimer {
		
		private final LivingEntity shooter;
		private final CustomEntity entity;
		private final Vector forward;
		private final int powerEnchant;
		private final double damage;
		private final Predicate<Entity> predicate;
		private boolean checkhit = false;

		private final RGB color;
		private Location lastLocation;
		
		private Bullet(LivingEntity shooter, Location startLocation, Vector arrowVelocity, int powerEnchant, double damage, RGB color) {
			super(60);
			setPeriod(TimeUnit.TICKS, 1);
			GuardianAngel.this.bullet = this;
			this.shooter = shooter;
			this.entity = new Bullet.ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.forward = arrowVelocity.multiply(2.75);
			this.powerEnchant = powerEnchant;
			this.damage = damage;
			this.color = color;
			this.lastLocation = startLocation;
			this.predicate = new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity instanceof ArmorStand) return false;
					if (entity.equals(shooter)) return false;
					if (entity instanceof Player) {
						if (!getGame().isParticipating(entity.getUniqueId())
								|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
								|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
							return false;
						}
						if (getGame() instanceof Teamable) {
							final Teamable teamGame = (Teamable) getGame();
							final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(shooter.getUniqueId());
							if (participant != null) {
								return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
							}
						}
					}
					return true;
				}

				@Override
				public boolean apply(@Nullable Entity arg0) {
					return false;
				}
			};
		}
		
		@Override
		protected void run(int i) {
			final Location newLocation = lastLocation.clone().add(forward);
			for (Iterator<Location> iterator = new Iterator<Location>() {
				private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
				private final int amount = (int) (vectorBetween.length() / 0.1);
				private int cursor = 0;

				@Override
				public boolean hasNext() {
					return cursor < amount;
				}

				@Override
				public Location next() {
					if (cursor >= amount) throw new NoSuchElementException();
					cursor++;
					return lastLocation.clone().add(unit.clone().multiply(cursor));
				}
			}; iterator.hasNext(); ) {
				final Location location = iterator.next();
				entity.setLocation(location);
				final Block block = location.getBlock();
				final Material type = block.getType();
				if (type.isSolid()) {
					SoundLib.ENTITY_ARROW_HIT_PLAYER.playSound(getPlayer());
					checkhit = true;
					stop(false);
					return;
				}
				for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, entity.getWorld(), entity.getBoundingBox(), predicate)) {
					if (!shooter.equals(livingEntity)) {
						Damages.damageArrow(livingEntity, shooter, (float) ((EnchantLib.getDamageWithPowerEnchantment(damage, powerEnchant)) * 0.75));
						if (livingEntity instanceof Player) Stun.apply(getGame().getParticipant((Player) livingEntity), TimeUnit.TICKS, Math.min(10 + (stack * 5), 60));
						checkhit = true;
						SoundLib.ENTITY_ARROW_HIT_PLAYER.playSound(getPlayer());
						stop(false);
						return;
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}
		
		@Override
		protected void onEnd() {
			if (checkhit) {
				new Bullet2(getPlayer(), entity.getLocation(), (float) (EnchantLib.getDamageWithPowerEnchantment(damage, powerEnchant)), COLOR2).start();
				checkhit = false;
			}
			entity.remove();
			GuardianAngel.this.bullet = null;
		}

		@Override
		protected void onSilentEnd() {
			entity.remove();
			GuardianAngel.this.bullet = null;
		}

		public class ArrowEntity extends CustomEntity implements Deflectable {

			public ArrowEntity(World world, double x, double y, double z) {
				getGame().super(world, x, y, z);
			}

			@Override
			public Vector getDirection() {
				return forward.clone();
			}

			@Override
			public void onDeflect(Participant deflector, Vector newDirection) {
				stop(false);
				final Player deflectedPlayer = deflector.getPlayer();
				new Bullet(deflectedPlayer, lastLocation, newDirection, powerEnchant, damage, color).start();
			}

			@Override
			public ProjectileSource getShooter() {
				return shooter;
			}

			@Override
			protected void onRemove() {
			}

		}
		
	}
	
	public class Bullet2 extends AbilityTimer {
		
		private final LivingEntity shooter;
		private final CustomEntity entity;
		private final double damage;
		private Vector velocity;
		private final Predicate<Entity> predicate;

		private final RGB color;
		private Location lastLocation;
		
		private Bullet2(LivingEntity shooter, Location startLocation, double damage, RGB color) {
			super(1200);
			setPeriod(TimeUnit.TICKS, 1);
			GuardianAngel.this.bullet2 = this;
			this.shooter = shooter;
			this.entity = new Bullet2.ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-.75, -.75, -.75, .75, .75, .75);
			this.velocity = getPlayer().getLocation().add(0, 1, 0).clone().subtract(startLocation.clone()).toVector().normalize().multiply(0.8);
			this.damage = damage;
			this.color = color;
			this.lastLocation = startLocation;
			this.predicate = new Predicate<Entity>() {
				@Override
				public boolean test(Entity entity) {
					if (entity instanceof ArmorStand) return false;
					if (entity instanceof Player) {
						if (entity.equals(getPlayer())) return true;
						if (!getGame().isParticipating(entity.getUniqueId())
								|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
								|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
							return false;
						}
						if (getGame() instanceof Teamable) {
							final Teamable teamGame = (Teamable) getGame();
							final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(shooter.getUniqueId());
							if (participant != null) {
								return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
							}
						}
					}
					return true;
				}

				@Override
				public boolean apply(@Nullable Entity arg0) {
					return false;
				}
			};
		}
		
		@Override
		protected void run(int i) {
			this.velocity = getPlayer().getLocation().add(0, 1, 0).clone().subtract(lastLocation.clone()).toVector().normalize().multiply(0.8);
			final Location newLocation = lastLocation.clone().add(velocity);
			for (Iterator<Location> iterator = new Iterator<Location>() {
				private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.1);
				private final int amount = (int) (vectorBetween.length() / 0.1);
				private int cursor = 0;

				@Override
				public boolean hasNext() {
					return cursor < amount;
				}

				@Override
				public Location next() {
					if (cursor >= amount) throw new NoSuchElementException();
					cursor++;
					return lastLocation.clone().add(unit.clone().multiply(cursor));
				}
			}; iterator.hasNext(); ) {
				final Location location = iterator.next();
				entity.setLocation(location);
				for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, entity.getWorld(), entity.getBoundingBox(), predicate)) {
					if (!shooter.equals(livingEntity)) {
						Damages.damageArrow(livingEntity, shooter, (float) (damage * 1.25));
						if (livingEntity instanceof Player) Stun.apply(getGame().getParticipant((Player) livingEntity), TimeUnit.TICKS, Math.min(5 + (stack * 2), 60));
					} else if (livingEntity.equals(shooter)) {
						stop(false);
						return;
					}
				}
				ParticleLib.REDSTONE.spawnParticle(location, color);
			}
			lastLocation = newLocation;
		}
		
		@Override
		protected void onEnd() {
			final int reloadCount = Wreck.isEnabled(GameManager.getGame()) ? (int) (Math.max(((100 - Settings.getCooldownDecrease().getPercentage()) / 100.0), 0.85) * 20) : 20;
			reload = new AbilityTimer(reloadCount) {
				private final ProgressBar progressBar = new ProgressBar(reloadCount, 15);

				@Override
				protected void run(int count) {
					progressBar.step();
					actionbarChannel.update("������: " + progressBar.toString());
				}

				@Override
				protected void onEnd() {
					GuardianAngel.this.reload = null;
					actionbarChannel.update(null);
					SoundLib.BLOCK_STONE_PRESSURE_PLATE_CLICK_ON.playSound(getPlayer());
				}
			}.setBehavior(RestrictionBehavior.PAUSE_RESUME).setPeriod(TimeUnit.TICKS, 2);
			reload.start();
			entity.remove();
			GuardianAngel.this.bullet2 = null;
		}

		@Override
		protected void onSilentEnd() {
			entity.remove();
			GuardianAngel.this.bullet2 = null;
		}

		public class ArrowEntity extends CustomEntity {

			public ArrowEntity(World world, double x, double y, double z) {
				getGame().super(world, x, y, z);
			}

			public ProjectileSource getShooter() {
				return shooter;
			}

			@Override
			protected void onRemove() {
			}

		}
		
	}
	
}