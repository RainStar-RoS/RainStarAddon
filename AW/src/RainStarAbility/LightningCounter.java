package RainStarAbility;

import javax.annotation.Nullable;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;

@AbilityManifest(
		name = "����Ʈ�� ī����",
		rank = Rank.A, 
		species = Species.HUMAN, 
		explain = {
		"��7�޸��� ��8- ��3����������f: �޸��鼭 10�� �ȿ� �������� ���� ������ ������",
		" 5�� ���ظ� ������ ������ �����, �����մϴ�. �޸��� ���� �� ������ 1 �ҽ��ϴ�.",
		"��7���� ��8- ��b���麸�� ������!��f: ���� �ø��� �̵� �ӵ��� �����ϸ�,",
		" ��ġ�� �ִ� ���ط��� 1�� �����մϴ�. �ִ� 5�ܰ���� ������ �����ϰ�",
		" �� ���� �ø��� ��3����������f�� ���� �ð��� 10�ʷ� ���ŵ˴ϴ�.",
		"��7�ǰ� ��8- ��c���� ���ٰ�?��f: �ִ� ���� �� �÷��̾�� ���ع��� ��",
		" ������ �ʱ�ȭ�ǰ�, ��Ÿ���� �����ϴ�. $[CooldownConfig]",
		"��7�нú� ��8- ��eE=mc����f: ���� �� �������� ���� Ÿ�� �� ������ �� �ܰ� �Ұ�",
		" ���ӿ� ����ϴ� �߰� ���ظ� �����ϴ�."
		},
		summarize = {
		"��7�޸��鼭 �������� ���� ���������f ���ظ� ������ ������ ���� ��, ��b���ӡ�f�մϴ�.",
		"�޸��⸦ ������ ������ ������ 1 �Ұ�, ��b���ӡ�f�� ��ġ��ŭ",
		"���� �������� ���ط��� ���� �����մϴ�.",
		"���� �� ���� Ÿ�� �� ��b���ӡ�f�� �� �ܰ� �Ҹ��Ͽ� �߰� ���ظ� �����ϴ�."
		})

@Tips(tip = {
        "���� �̵��ӵ��� ��� ���� ġ�� ������ �ο�� �ɷ��Դϴ�.",
        "�⺻ �̵��ӵ��� �����ϱ� ������ �ż� ������ ��´ٸ� �� ����",
        "������ �� �ְ�, �ߵ��� �޸��Ⱑ ������ �ʾƾ� �ϱ⿡",
        "���� ���� ���� �� �����ִ� ���� ������� �����մϴ�.",
        "�ٸ� �޸��� ���� ���� �� �޸��Ⱑ ����� ������ �����ϹǷ�",
        "�ɷ��� �� �츮�� �ʹٸ� �����ϼ���."
}, strong = {
        @Description(subject = "�ӵ�", explain = {
        		"���� �ӵ��߸��� �� �ɷ��� �����Դϴ�.",
        		"�ٸ� �÷��̾ �߰��� ����, ��Ȳ�� ��� ����",
        		"�⺻���� �ӵ� ������ ���� �ۿ��մϴ�."
        }),
        @Description(subject = "���", explain = {
        		"�޸��Ⱑ ������ �ʾƾ� �ϱ� ������, ��� ��",
        		"��ֹ��� ���� �������� ���� �����մϴ�."
        })
}, weak = {
        @Description(subject = "��ֹ�", explain = {
        		"�� �� �ڿ����� ��������� ����, �ǹ� �� ��� �����",
        		"�� �ɷ��� ��ֹ��� �� �� �ֽ��ϴ�. �����ϴٸ� ��߸�",
        		"ã�� ���� ��õ�帳�ϴ�."
        })
}, stats = @Stats(offense = Level.TWO, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.SIX, utility = Level.ZERO), difficulty = Difficulty.NORMAL)

public class LightningCounter extends AbilityBase {
	
	public LightningCounter(Participant participant) {
		super(participant);
	}
	
	private final Cooldown RunCool = new Cooldown(CooldownConfig.getValue(), CooldownDecrease._25);
	private PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 60, 1, true, false);
	
	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(
			LightningCounter.class, "Cooldown", 10, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	protected void onUpdate(AbilityBase.Update update) {
		if (update == AbilityBase.Update.RESTRICTION_CLEAR) {
			actionbarChannel.update("��3���� ��b0��3�ܰ�");
		    running.start();
		    getPlayer().setWalkSpeed(0.3f);
		    getPlayer().setFlySpeed(0.2f);
		}
		if (update == AbilityBase.Update.RESTRICTION_SET || update == AbilityBase.Update.ABILITY_DESTROY) {
			getPlayer().setSprinting(false);
		    getPlayer().setWalkSpeed(0.2F);
		    getPlayer().setFlySpeed(0.1F);
		} 
	}
	
	private int dmg = 5;
	private LivingEntity pentity = null;
	private final ActionbarChannel actionbarChannel = newActionbarChannel();
	
	private final Attacking attacking = new Attacking();
	
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
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (attacking.stack >= 5) {
			if (e.getDamager() instanceof Player && e.getEntity().equals(getPlayer())) {
			attacking.stop(true);
			RunCool.start();
			}
		}
		if (attacking.stack >= 1) {
			if (e.getDamager().equals(getPlayer())) {
				e.setDamage(e.getDamage() + (attacking.stack * 1.4));
				attacking.stack--;
				attacking.stop(true);
				actionbarChannel.update("��3���� ��b" + attacking.stack + "��3�ܰ�, ��6���� �ð���f: ��f����");
				if (attacking.stack == 0) {
					getPlayer().setWalkSpeed(0.3f);
					getPlayer().setFlySpeed(0.2f);
					dmg = 5;
				} else if (attacking.stack == 1) {
					getPlayer().setWalkSpeed(0.325f);
					getPlayer().setFlySpeed(0.225f);
					dmg = 6;
				} else if (attacking.stack == 2) {
					getPlayer().setWalkSpeed(0.35f);
					getPlayer().setFlySpeed(0.25f);
					dmg = 7;
				} else if (attacking.stack == 3) {
					getPlayer().setWalkSpeed(0.4f);
					getPlayer().setFlySpeed(0.3f);
					dmg = 8;
				} else if (attacking.stack == 4) {
					getPlayer().setWalkSpeed(0.45f);
					getPlayer().setFlySpeed(0.35f);
					dmg = 9;
				} else if (attacking.stack == 5) {
					getPlayer().setWalkSpeed(0.5f);
					getPlayer().setFlySpeed(0.4f);
					dmg = 10;
				}
			}
		}
	}
	
	private final AbilityTimer running = new AbilityTimer() {
		@Override
		protected void run(int count) {
			if (getPlayer().isSprinting() && !RunCool.isRunning()) {
				attacking.start();
			} else if (!getPlayer().isSprinting() && !RunCool.isRunning() && attacking.isRunning() && attacking.stack >= 1) {
				attacking.stack--;
				attacking.stop(true);
				actionbarChannel.update("��3���� ��b" + attacking.stack + "��3�ܰ�, ��6���� �ð���f: ��f����");
				if (attacking.stack == 0) {
					getPlayer().setWalkSpeed(0.3f);
					getPlayer().setFlySpeed(0.2f);
					dmg = 5;
				} else if (attacking.stack == 1) {
					getPlayer().setWalkSpeed(0.325f);
					getPlayer().setFlySpeed(0.225f);
					dmg = 6;
				} else if (attacking.stack == 2) {
					getPlayer().setWalkSpeed(0.35f);
					getPlayer().setFlySpeed(0.25f);
					dmg = 7;
				} else if (attacking.stack == 3) {
					getPlayer().setWalkSpeed(0.4f);
					getPlayer().setFlySpeed(0.3f);
					dmg = 8;
				} else if (attacking.stack == 4) {
					getPlayer().setWalkSpeed(0.45f);
					getPlayer().setFlySpeed(0.35f);
					dmg = 9;
				} else if (attacking.stack == 5) {
					getPlayer().setWalkSpeed(0.5f);
					getPlayer().setFlySpeed(0.4f);
					dmg = 10;
				}
			}
		}
	}.setPeriod(TimeUnit.TICKS, 1);
	
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
						Damages.damageMagic(pentity, getPlayer(), false, dmg);
						pentity.addPotionEffect(slow);
						pentity = null;
						addStack();
					}
				}
			}
 			
		}
		
		private void addStack() {
			if (stack < 5) {
				stack++;
			}
			if (isRunning()) {
				setCount(200);
				actionbarChannel.update("��3���� ��b" + stack + "��3�ܰ�, ��6���� �ð���f: " + getFixedCount() + "��");
			}
			if (stack == 1) {
				getPlayer().setWalkSpeed(0.325f);
				getPlayer().setFlySpeed(0.225f);
				dmg = 6;
				 SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1);
				 ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.3);
			} else if (stack == 2) {
				getPlayer().setWalkSpeed(0.35f);
				getPlayer().setFlySpeed(0.25f);
				dmg = 7;
				 SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1.3f);
				 ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.4);
			} else if (stack == 3) {
				getPlayer().setWalkSpeed(0.4f);
				getPlayer().setFlySpeed(0.3f);
				dmg = 8;
				 SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1.5f);
				 ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.5);
			} else if (stack == 4) {
				getPlayer().setWalkSpeed(0.45f);
				getPlayer().setFlySpeed(0.35f);
				dmg = 9;
				 SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1.7f);
				 ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.6);
			} else if (stack == 5) {
				getPlayer().setWalkSpeed(0.5f);
				getPlayer().setFlySpeed(0.4f);
				dmg = 10;
				 SoundLib.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.playSound(getPlayer(), 1, 1.9f);
				 ParticleLib.FLAME.spawnParticle(getPlayer().getLocation(), 0, 0, 0, 50, 0.7);
			}
		}
		
		@Override
		protected void onEnd() {
			actionbarChannel.update("��6���� �ð� ��f����");
			stack = 0;
			getPlayer().setWalkSpeed(0.3f);
			getPlayer().setFlySpeed(0.2f);
			dmg = 5;
		}
		
		@Override
		protected void onSilentEnd() {
		}	
	}
}