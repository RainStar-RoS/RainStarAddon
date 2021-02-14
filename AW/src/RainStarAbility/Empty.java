package RainStarAbility;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.list.mix.AbstractMix;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.TimeUtil;
import daybreak.abilitywar.utils.base.collect.SetUnion;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;
import java.util.StringJoiner;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(name = "[ ]", rank = Rank.S, species = Species.OTHERS, explain = {
		"��a---------------------------------",
		"$(EXPLAIN)",
		"��a---------------------------------",
		"ö���� ����� ��Ŭ���Ͽ� �ɷ��� 30�ʰ� �����մϴ�. $[MIN_COOLDOWN]",
		"���� [  ]���� �ǵ��ƿɴϴ�. �ǵ��ƿ� �� ������ �ɷ���",
		"��Ÿ���� ���, ������ �ð���ŭ [  ]�� ��Ÿ���� ���մϴ�."
})

@Tips(tip = {
        "�ٸ� ����� �ɷ��� ������ �� �־ ������ ���ɼ��� ���� �ɷ��Դϴ�.",
        "����� �ɷ��� �����Ͽ� �ɷ��� �˾����� �� �ִٴ���, ���� �ɷ��� �����Ͽ�",
        "�� �ڴ�� ����� �����ϰ� ��� �� �ɷ¿� �������� �ʴ� �ɷ��Դϴ�.",
        "�ٸ� �ɷ� ��뿡 �־� Ÿ������ �����Ǳ⿡ �ٸ� ��󿡰� �����ϴ� ����",
        "�����ϼž߸� �մϴ�."
}, strong = {
        @Description(subject = "���� �ɷ��� ���", explain = {
                "�ٸ� ����� �ɷ��� �����Ҽ��� �����ϴ� ������ �ɷµ� ���������ϴ�."
        }),
        @Description(subject = "�ɷ� �ľ�", explain = {
                "Ÿ���� �ɷ��� �����Ͽ� ����� �ɷ��� �������� �˾Ƴ� �� �ֽ��ϴ�."
        }),
        @Description(subject = "������", explain = {
                "�ɷ��� ������ �� �ִٴ� ���� �ٽ� ����, ���� �������� ��� �ɷµ���",
                "������ ��� �����ϴٴ� ���Դϴ�. ���� �ɷ����� �ٲٸ� ������ ������",
                "��ĥ �� �ִ� �������̾߸��� �� �ɷ��� �ִ� �����Դϴ�."
        })
}, weak = {
        @Description(subject = "���� �ɷ��� ���", explain = {
                "������ ����� ���� �ɷ��� �����ϰ� �ִٸ�, ������ �ڽ��� �ɷµ�",
                "�������� ���� �˴ϴ�. �ٸ� ����� �ɷ��� �̸� �ľ��صμ���."
        }),
        @Description(subject = "���� ������ ��", explain = {
                "���� ������ ���� ������ ��������� �� �ɷ��� �ִ� ������",
                "�������� ��ġ�� ���� �˴ϴ�. �ǵ��� �÷��̾ ���� ������",
                "�����ϴ� ���� �����ϴ�."
        }),
        @Description(subject = "ª�� ���ӽð�", explain = {
                "������ �ɷ��� ��� ������ �ð��� 20�ʹۿ� ä ���� �ʾ�,",
                "��κ��� ��Ƽ�� �ɷ��� �ⲯ�ؾ� �� �� ����� �� �ֽ��ϴ�.",
                "���� ������ �ɷ��� ��Ÿ���� ���� �����Ƿ�",
                "������ ������ �Ǵܷ��� ���մϴ�."
        })
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.TEN), difficulty = Difficulty.NORMAL)

public class Empty extends AbilityBase implements ActiveHandler, TargetHandler {

	public static final SettingObject<Integer> MIN_COOLDOWN = abilitySettings.new SettingObject<Integer>(Empty.class, "COOLDOWN", 30, "# �ּ� ��Ÿ��") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue()) + " + n";
		}

	};

	private AbilityBase ability;

	private final Object EXPLAIN = new Object() {
		@Override
		public String toString() {
			if (ability == null) {
				return "�ɷ��� ������ �� �ֽ��ϴ�.".toString();
			} else {
				final StringJoiner joiner = new StringJoiner("\n");
				joiner.add("��a������ �ɷ� ��f| ��7[��b" + ability.getName() + "��7] " + ability.getRank().getRankName() + " " + ability.getSpecies().getSpeciesName());
				for (final Iterator<String> iterator = ability.getExplanation(); iterator.hasNext();) {
					joiner.add("��f" + iterator.next());
				}
				return joiner.toString();
			}
		}
	};
	
	@Override
	public String getDisplayName() {
		return "[" + (ability != null ? (ability.getName() + "]") : " ]");
	}

	private final int minCooldown = MIN_COOLDOWN.getValue();
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	private final Cooldown cooldown = new Cooldown(MIN_COOLDOWN.getValue(), "����");

	public Empty(Participant participant) {
		super(participant);
	}

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		return ability instanceof ActiveHandler && ((ActiveHandler) ability).ActiveSkill(material, clickType);
	}

	@Override
	public void TargetSkill(Material material, LivingEntity entity) {
		if (ability != null) {
			if (ability instanceof TargetHandler) {
			((TargetHandler) ability).TargetSkill(material, entity);
			}
		} else {
			if (material == Material.IRON_INGOT && entity instanceof Player) {
				if (!cooldown.isCooldown()) {
					final Player entityPlayer = (Player) entity;
					final Participant target = getGame().getParticipant(entityPlayer);
					if (target.hasAbility() && !target.getAbility().isRestricted()) {
						final AbilityBase targetAbility = target.getAbility();
						if (getGame() instanceof AbstractMix) {
							final Mix targetMix = (Mix) targetAbility;
							if (targetMix.hasAbility()) {
								if (targetMix.hasSynergy()) {
									try {
										this.ability = AbilityBase.create(targetMix.getSynergy().getClass(), getParticipant());
										this.ability.setRestricted(false);
										getPlayer().sendMessage("��b�ɷ��� �����Ͽ����ϴ�. ����� �ɷ��� ��e" + ability.getName() + "��b �Դϴ�.");
										new ReturnTimer();
									} catch (ReflectiveOperationException e) {
										e.printStackTrace();
									}
								} else {
									final Mix myMix = (Mix) getParticipant().getAbility();
									final AbilityBase myFirst = myMix.getFirst(), first = targetMix.getFirst(), second = targetMix.getSecond();

									try {
										final Class<? extends AbilityBase> clazz = (this.equals(myFirst) ? first : second).getClass();
										if (clazz != Empty.class) {
											this.ability = AbilityBase.create(clazz, getParticipant());
											this.ability.setRestricted(false);
											getPlayer().sendMessage("��b�ɷ��� �����Ͽ����ϴ�. ����� �ɷ��� ��e" + ability.getName() + "��b �Դϴ�.");
											new ReturnTimer();
										} else {
											getPlayer().sendMessage("��b������ ������ �� �����ϴ�.");
										}
									} catch (ReflectiveOperationException e) {
										e.printStackTrace();
									}
								}

							}

						} else {
							try {
								final Class<? extends AbilityBase> clazz = targetAbility.getClass();
								if (clazz != Empty.class) {
									this.ability = AbilityBase.create(clazz, getParticipant());
									this.ability.setRestricted(false);
									getPlayer().sendMessage("��b�ɷ��� �����Ͽ����ϴ�. ����� �ɷ��� ��e" + targetAbility.getName() + "��b �Դϴ�.");
									new ReturnTimer();
								} else {
									getPlayer().sendMessage("��b������ ������ �� �����ϴ�.");
								}
							} catch (ReflectiveOperationException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Set<GameTimer> getTimers() {
		return ability != null ? SetUnion.union(super.getTimers(), ability.getTimers()) : super.getTimers();
	}

	@Override
	public Set<GameTimer> getRunningTimers() {
		return ability != null ? SetUnion.union(super.getRunningTimers(), ability.getRunningTimers()) : super.getRunningTimers();
	}

	@Override
	public boolean usesMaterial(Material material) {
		if (ability != null) {
			return ability.usesMaterial(material);
		}
		return super.usesMaterial(material);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (ability != null) {
				ability.setRestricted(false);
			}
		} else if (update == Update.RESTRICTION_SET) {
			if (ability != null) {
				ability.setRestricted(true);
			}
		} else if (update == Update.ABILITY_DESTROY) {
			if (ability != null) {
				ability.destroy();
				ability = null;
			}
		}
	}

	private class ReturnTimer extends AbilityTimer {

		private ReturnTimer() {
			super(TaskType.REVERSE, 30);
			this.start();
		}

		@Override
		protected void run(int arg0) {
			actionbarChannel.update("[  ]���� ��ȯ���� ��3" + TimeUtil.parseTimeAsString(getFixedCount()) + "��f ���ҽ��ϴ�.");
		}

		@Override
		protected void onEnd() {
			try {
				int cooltimeSum = 0;
				if (Empty.this.ability != null) {
					for (GameTimer timer : Empty.this.ability.getRunningTimers()) {
						if (timer instanceof Cooldown.CooldownTimer) {
							cooltimeSum += timer.getCount();
						}
					}
					Empty.this.ability.destroy();
				}
				Empty.this.ability = null;
				getParticipant().getPlayer().sendMessage("��b����� �ɷ��� ��f[  ]��b���� �ǵ��ƿԽ��ϴ�.");
				actionbarChannel.update(null);
				cooldown.setCooldown(minCooldown + (cooltimeSum / 2));
				cooldown.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}