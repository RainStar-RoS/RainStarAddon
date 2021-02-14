package RainStarAbility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
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
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "ī�̷ν�", rank = Rank.S, species = Species.GOD, explain = {
		"��ȸ�� �� ī�̷ν�.",
		"��7ö�� ��Ŭ�� ��8- ��a��ȸ �ο���f: �ٸ� �÷��̾ 15ĭ ������ ��Ŭ�� �� ����� �ɷ���",
		" ��dS ��ޡ�f���� ���� ��� �ּ� �� ��� �̻��� �ɷ����� ����÷�մϴ�.",
		" �̶� ��󿡰� �ٲ��� �ɷ��� �� �� �ֽ��ϴ�. $[CooldownConfig]",
		" ���� �ٲ��� ��󿡰Լ� ��ȸ ü���� 1ĭ �����մϴ�.",
		"��7�нú� ��8- ��a������ ��ȸ��f: ġ������ ���ظ� �Ծ��� �� �� �� ��",
		" ������� �ʰ� ��� ü�� �� ĭ���� ��Ȱ�մϴ�. �̶�, �ٸ� �÷��̾��",
		" ������ ��ȸ ü�¸�ŭ �� ����� ü���� ������ ��� ȸ���ϰ�, ȸ���� ü����",
		" ��ü ü���� ���� �̻��� ��� �� �ɷ��� �������� ����÷�մϴ�.",
		" �ߵ� ���� ��ȸ �ο� �ɷ��� ����� �� �����ϴ�."})

@Tips(tip = {
        "ī�̷ν��� �ٸ� �÷��̾��� �ɷ��� �°ݽ��� �� �� ����",
        "��ȸ�� �����ν�, ���� �ڽ� �� �� �̵��� ���� �� �ִ�",
        "Ư���� �ɷ��Դϴ�. ���� ��ȸ ü���� ���� �̻� �����ϸ�",
        "ī�̷ν� ������ �ɷ��� ���ο� ��ȸ�� �ٲ�� ���� ���ٸ�",
        "��Ȳ�� Ÿ���� ������ �ɷ��� �Ǿ� ���ƿ� �� �ֽ��ϴ�.",
        "ī�̷ν��� �ɷ� ���� �ɷ��� �� ���ӿ��� �� ������ ���� �����ִµ�,",
        "������ ���ʿ��� ����� �ɷ��� ���� �ɷ����� �ٲ�ĥ �� �ֽ��ϴ�."
}, strong = {
        @Description(subject = "���� â��", explain = {
                "����� �ɷ��� �ٲ۴ٴ� ����, ������ ������",
                "â���� �� �ֽ��ϴ�. ���� ��Ȱ �� �ɷ� ���浵",
                "������ â���ϱ� �����ϴ�."
        }),
        @Description(subject = "������ ����� �ɷ�", explain = {
                "����Ŀ �� A����̰ų� �� �����ӿ��� ������ ������",
                "�˳��� �ɷ��� ������ �°ݽ��� ���⿡�� Ż���غ�����."
        }),
        @Description(subject = "�� ����", explain = {
                "����ߵ� ������ ���ʿ��� �ɷ��� �°ݽ���",
                "���� �ɷ����� �ٲ��� �� �ֽ��ϴ�."
        }),
        @Description(subject = "��", explain = {
                "��볪 �ڽ��� ���ϴ� �ɷ��� �ɸ��� �Ϸ���",
                "����� ����� �ʿ��մϴ�."
        })
}, weak = {
        @Description(subject = "���� �ɷ�", explain = {
                "���� �ɷ¿��� ī�̷ν��� �� �� �ִ� ���� �����ϴ�.",
                "������ ���������� ���ϼ���."
        }),
        @Description(subject = "��", explain = {
                "��볪 �ڽ��� ���ϴ� �ɷ��� �ɸ��� �Ϸ���",
                "����� ����� �ʿ��մϴ�."
        })
}, stats = @Stats(offense = Level.ZERO, survival = Level.SEVEN, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.EIGHT), difficulty = Difficulty.EASY)

public class Kairos extends AbilityBase implements ActiveHandler {
	
	public Kairos(Participant participant) {
		super(participant);
	}

	private final Cooldown cooldown = new Cooldown(CooldownConfig.getValue());
	private final ActionbarChannel ac = newActionbarChannel();
	private int stack = 0;
	private static final Circle circle = Circle.of(0.5, 30);
	private static final RGB gold = RGB.of(254, 228, 1);
	private Participant target = null;
	private Set<UUID> inv = new HashSet<>();
	private Map<Participant, Integer> getHp = new HashMap<>();
	private static final Random random = new Random();
	private boolean rebirth = false;
	
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
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Kairos.class,
			"Cooldown", 20, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public AbilityRegistration getRandomAbility(Participant target, Rank rank) {
		final Set<AbilityRegistration> usedAbilities = new HashSet<>();
		for (Participant participant : getGame().getParticipants()) {
			if (participant.hasAbility() && participant.attributes().TARGETABLE.getValue()) {
				usedAbilities.add(participant.getAbility().getRegistration());
			}
		}
		
		final int criterion = rank.ordinal();
		final List<AbilityRegistration> registrations = AbilityList.values().stream().filter(
				ability -> !Configuration.Settings.isBlacklisted(ability.getManifest().name()) &&
				!usedAbilities.contains(ability) && (getParticipant().equals(target) || ability.getManifest().rank().ordinal() < criterion)
		).collect(Collectors.toList());
		return registrations.isEmpty() ? null : random.pick(registrations);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clicktype) {
		if (material == Material.IRON_INGOT && clicktype == ClickType.RIGHT_CLICK
				&& !cooldown.isCooldown() && !rebirth) {
			Player player = LocationUtil.getEntityLookingAt(Player.class, getPlayer(), 15, predicate);
			if (player != null) {
				target = getGame().getParticipant(player);
				if (target.hasAbility() && !target.getAbility().isRestricted()) {
					AbilityBase ab = target.getAbility();
					if (ab.getRank().equals(Rank.C) || ab.getRank().equals(Rank.B) || ab.getRank().equals(Rank.A)) {
						try {
							target.setAbility(getRandomAbility(target, ab.getRank()).getAbilityClass());
						} catch (UnsupportedOperationException | ReflectiveOperationException e) {
							e.printStackTrace();
						}
						getPlayer().sendMessage("��e" + player.getName() + "��f�Կ��� ��a��ȸ��f�� �帳�ϴ�.");
						getPlayer().sendMessage("���� ����� �ɷ��� ��e" + target.getAbility().getDisplayName() + "��f�Դϴ�.");
						target.getPlayer().sendMessage("��ſ��� �� ��ȸ�� �־������ϴ�. ���� ����� �ɷ��� ��e" + target.getAbility().getDisplayName() + "��f�Դϴ�.");
						new AbilityTimer(60) {

							private ActionbarChannel actionbarChannel;

							@Override
							protected void run(int count) {
								actionbarChannel.update("��7���� ��f: " + (getCount() / 10.0) + "��");
							}

							@Override
							protected void onStart() {
								inv.add(target.getPlayer().getUniqueId());
								actionbarChannel = target.actionbar().newChannel();
							}

							@Override
							protected void onEnd() {
								inv.remove(target.getPlayer().getUniqueId());
								if (actionbarChannel != null)
									actionbarChannel.unregister();
							}

							@Override
							protected void onSilentEnd() {
								if (actionbarChannel != null)
									actionbarChannel.unregister();
							}
						}.setPeriod(TimeUnit.TICKS, 1).start();
						getHp.put(target, getHp.getOrDefault(target, 0) + 2);
						SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(target.getPlayer(), 1, 1.3f);
						SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer(), 1, 1.5f);
						cooldown.start();
						stack++;
						ac.update("��ȸ�� �� Ƚ��: " + stack);
						return true;
					} else if (ab.getClass().equals(Mix.class)) {
						final Mix mix = (Mix) ab;
						final AbilityBase first = mix.getFirst(), second = mix.getSecond();
						
						if (mix.hasSynergy()) {
							getPlayer().sendMessage("��e" + player.getName() + "��f���� ��c��ȸ��f�� ���� �ʿ䰡 �����ϴ�.");
							SoundLib.ENTITY_ENDERMAN_HURT.playSound(getPlayer(), 1, 0.7f);
							return false;
						}
						final Mix myAbility = (Mix) getParticipant().getAbility();
						if (this.equals(myAbility.getFirst())) {
							final boolean firstStatus = first.getRank().equals(Rank.C) || first.getRank().equals(Rank.B) || first.getRank().equals(Rank.A);
							if (firstStatus) {
								Class<? extends AbilityBase> firstClass = first.getClass();
								if (firstStatus) firstClass = getRandomAbility(target, first.getRank()).getAbilityClass();
								try {
									mix.setAbility(firstClass, mix.getSecond().getClass());
								} catch (ReflectiveOperationException e) {
									e.printStackTrace();
								}
								getPlayer().sendMessage("��e" + player.getName() + "��f�Կ��� ��a��ȸ��f�� �帳�ϴ�.");
								getPlayer().sendMessage("���� ����� �ɷ��� ��e" + mix.getFirst().getDisplayName() + "��f�Դϴ�.");
								target.getPlayer().sendMessage("��ſ��� �� ��ȸ�� �־������ϴ�. ���� ����� �ɷ��� ��e" + mix.getFirst().getDisplayName() + "��f, ��e" + mix.getSecond().getDisplayName() + "��f�Դϴ�.");
								new AbilityTimer(60) {

									private ActionbarChannel actionbarChannel;

									@Override
									protected void run(int count) {
										actionbarChannel.update("��8������f: " + (getCount() / 20.0) + "��");
									}

									@Override
									protected void onStart() {
										inv.add(target.getPlayer().getUniqueId());
										actionbarChannel = target.actionbar().newChannel();
									}

									@Override
									protected void onEnd() {
										inv.remove(target.getPlayer().getUniqueId());
										if (actionbarChannel != null)
											actionbarChannel.unregister();
									}

									@Override
									protected void onSilentEnd() {
										if (actionbarChannel != null)
											actionbarChannel.unregister();
									}
								}.setPeriod(TimeUnit.TICKS, 1).start();
								getHp.put(target, getHp.getOrDefault(target, 0) + 2);
								SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(target.getPlayer(), 1, 1.3f);;
								SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer(), 1, 1.5f);
								cooldown.start();
								stack++;
								ac.update("��b��ȸ�� �� Ƚ����f: " + stack);
								return true;
							} else {
								getPlayer().sendMessage("��e" + player.getName() + "��f���� ��c��ȸ��f�� ���� �ʿ䰡 �����ϴ�.");
								SoundLib.ENTITY_ENDERMAN_HURT.playSound(getPlayer(), 1, 0.7f);
								return false;
							}
						} else if (this.equals(myAbility.getSecond())) {
							final boolean secondStatus = second.getRank().equals(Rank.C) || second.getRank().equals(Rank.B) || second.getRank().equals(Rank.A);
							if (secondStatus) {
								Class<? extends AbilityBase> secondClass = second.getClass();
								if (secondStatus) secondClass = getRandomAbility(target, second.getRank()).getAbilityClass();
								try {
									mix.setAbility(mix.getFirst().getClass(), secondClass);
								} catch (ReflectiveOperationException e) {
									e.printStackTrace();
								}
								getPlayer().sendMessage("��e" + player.getName() + "��f�Կ��� ��a��ȸ��f�� �帳�ϴ�.");
								getPlayer().sendMessage("���� ����� �ɷ��� ��e" + mix.getSecond().getDisplayName() + "��f�Դϴ�.");
								target.getPlayer().sendMessage("��ſ��� �� ��ȸ�� �־������ϴ�. ���� ����� �ɷ��� ��e" + mix.getFirst().getDisplayName() + "��f, ��e" + mix.getSecond().getDisplayName() + "��f�Դϴ�.");
								new AbilityTimer(60) {

									private ActionbarChannel actionbarChannel;

									@Override
									protected void run(int count) {
										actionbarChannel.update("��8������f: " + (getCount() / 20.0) + "��");
									}

									@Override
									protected void onStart() {
										inv.add(target.getPlayer().getUniqueId());
										actionbarChannel = target.actionbar().newChannel();
									}

									@Override
									protected void onEnd() {
										inv.remove(target.getPlayer().getUniqueId());
										if (actionbarChannel != null)
											actionbarChannel.unregister();
									}

									@Override
									protected void onSilentEnd() {
										if (actionbarChannel != null)
											actionbarChannel.unregister();
									}
								}.setPeriod(TimeUnit.TICKS, 1).start();
								getHp.put(target, getHp.getOrDefault(target, 0) + 2);
								SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(target.getPlayer(), 1, 1.3f);;
								SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer(), 1, 1.5f);
								cooldown.start();
								stack++;
								ac.update("��b��ȸ�� �� Ƚ����f: " + stack);
								return true;
							} else {
								getPlayer().sendMessage("��e" + player.getName() + "��f���� ��c��ȸ��f�� ���� �ʿ䰡 �����ϴ�.");
								SoundLib.ENTITY_ENDERMAN_HURT.playSound(getPlayer(), 1, 0.7f);
								return false;
							}
						}
					} else {
							getPlayer().sendMessage("��e" + player.getName() + "��f���� ��c��ȸ��f�� ���� �ʿ䰡 �����ϴ�.");
							SoundLib.ENTITY_ENDERMAN_HURT.playSound(getPlayer(), 1, 0.7f);
							return false;
						}
				} else {
					getPlayer().sendMessage("�ش� �÷��̾�� ���� �� �ִ� ��ȸ�� �����ϴ�.");
					return false;
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
		if (inv.contains(e.getDamager().getUniqueId()) || inv.contains(e.getEntity().getUniqueId())) {
		 	e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}

	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && getPlayer().getHealth() - e.getFinalDamage() <= 0 && !rebirth) {
			this.rebirth = true;
			int sum = 0;
			for (final Entry<Participant, Integer> entry : getHp.entrySet()) {
				final int health = entry.getValue();
				final Player player = entry.getKey().getPlayer();
				Healths.setHealth(player, player.getHealth() - health);
				sum += health;
			}
			Healths.setHealth(getPlayer(), 1 + sum);
			if (1 + sum >= (getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2)) {
				if (getParticipant().getAbility().getClass().equals(Mix.class)) {
					final Mix mix = (Mix) getParticipant().getAbility();
					final AbilityBase first = mix.getFirst(), second = mix.getSecond();
					final boolean firstStatus = first.getClass().equals(Kairos.class), 
							secondStatus = second.getClass().equals(Kairos.class);
					if (firstStatus || secondStatus) {
						Class<? extends AbilityBase> firstClass = first.getClass(), secondClass = second.getClass();
						if (firstStatus) firstClass = getRandomAbility(getParticipant(), first.getRank()).getAbilityClass();
						if (secondStatus) secondClass = getRandomAbility(getParticipant(), second.getRank()).getAbilityClass();
						try {
							mix.setAbility(firstClass, secondClass);
						} catch (ReflectiveOperationException e1) {
							e1.printStackTrace();
						}
					}
					getPlayer().sendMessage("��b������ ��ȸ��f�� ����� ��eī�̷ν���f �ɷ��� �ٲ�����ϴ�.");
					getPlayer().sendMessage("���� �ɷ�: ��e" + mix.getFirst().getDisplayName() + "��f, ��e" + mix.getSecond().getDisplayName());
				} else {
					try {
						getParticipant().setAbility(getRandomAbility(getParticipant(), getParticipant().getAbility().getRank()).getAbilityClass());
					} catch (UnsupportedOperationException | ReflectiveOperationException e1) {
						e1.printStackTrace();
					}
					getPlayer().sendMessage("��b������ ��ȸ��f�� ����� ��eī�̷ν���f �ɷ��� ��e" + getParticipant().getAbility().getDisplayName() + "��f���� �ٲ�����ϴ�.");
				}
			}
			new AbilityTimer(60) {
				@Override
				protected void run(int count) {
					Location center = getPlayer().getLocation().clone().add(0, 2, 0);
					for (Location loc : circle.toLocations(center)) {
						ParticleLib.REDSTONE.spawnParticle(loc, gold);
					}
				}
			}.setPeriod(TimeUnit.TICKS, 1).start();
			SoundLib.ENTITY_EVOKER_PREPARE_SUMMON.playSound(getPlayer().getLocation(), 1, 1);
			new BukkitRunnable() {
				@Override
				public void run() {
					SoundLib.ENTITY_EVOKER_PREPARE_SUMMON.playSound(getPlayer().getLocation(), 1, 1.3f);
				}	
			}.runTaskLater(AbilityWar.getPlugin(), 10L);
			new BukkitRunnable() {
				@Override
				public void run() {
					SoundLib.ENTITY_EVOKER_PREPARE_SUMMON.playSound(getPlayer().getLocation(), 1, 2);
				}	
			}.runTaskLater(AbilityWar.getPlugin(), 30L);
			stack = 0;
			ac.unregister();
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	public void onEntityShootBow(EntityShootBowEvent e) {
		if (inv.contains(e.getEntity().getUniqueId())) {
			e.setCancelled(true);
		}
	}
}