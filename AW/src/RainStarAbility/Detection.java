package RainStarAbility;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Materials;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;

@AbilityManifest(
		name = "����",
		rank = Rank.A, 
		species = Species.HUMAN, 
		explain = {
		"������ ����� ��Ŭ�� �� ����� ���� $[DurationConfig]�ʰ� �����մϴ�. $[CooldownConfig]",
		"���� ���Ĵ��� ����� ���� ������ ���� ȸ���մϴ�.",
		"���� �� �������� ���� ���ظ� �޾��� �� 1�� �߰� ���ظ� �Խ��ϴ�."
		})

@Tips(tip = {
        "���� ������ ���������μ� ��󿡰� 2~3Ÿ ������ �������� ���� �� �ֽ��ϴ�.",
        "���� ����Ŀ ���� �� ���� ������ ������ ������ ������ ���� �� �ֽ��ϴ�.",
        "�ٸ� ��󿡰Լ� ���� ���� Ȱ ���� ������ ���ظ� �Դ´ٸ� ���� �Ǵ�,",
        "����� �ɷ��� �� �ľ��ϰ� ����Ͻô� ���� ��õ�մϴ�."
}, strong = {
        @Description(subject = "������", explain = {
                "�������̾߸��� �� �ɷ��� �ִ� Ȱ�� ������ �����Դϴ�.",
                "����� ���� ������ �����Ͽ� �������� ���� �� �ֽ��ϴ�."
        }),
        @Description(subject = "���� ����", explain = {
                "���� �����ϼ��� ���� ���� ����� ����ġ�� ���ϰ� ����",
                "������ ���ϱ� �� �����մϴ�."
        }),
        @Description(subject = "���� ���� ����������� ���� ���", explain = {
                "����Ŀ�� �ؽ� �� �� ���� ���� �Ǹ� ������ �����Ͽ�,",
                "����� �ɷ� ���ظ� �ּ�ȭ��ŵ�ϴ�."
        })
}, weak = {
        @Description(subject = "���Ÿ���", explain = {
                "����� Ȱ �ɷ��� �����ϰ� �ְų� ���Ÿ������� �����Ѵٸ�",
                "������ ��󿡰Լ� �Ź� 2�� �߰� ���ظ� �����Ƿ� �����ؾ� �մϴ�."
        })
}, stats = @Stats(offense = Level.ZERO, survival = Level.SEVEN, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.TWO), difficulty = Difficulty.EASY)

@Materials(materials = {
		Material.WOOD_SWORD,
		Material.STONE_SWORD,
		Material.IRON_SWORD,
		Material.GOLD_SWORD,
		Material.DIAMOND_SWORD
	})

public class Detection extends AbilityBase implements TargetHandler { 

	public Detection(Participant participant) {
		super(participant);
	}

	private final Cooldown skill = new Cooldown(CooldownConfig.getValue());
	private final int duration = DurationConfig.getValue();

	private Set<UUID> noatk = new HashSet<>();
	private boolean dur = false;
	private boolean config = ChangeConfig.getValue();
	private int range = RangeConfig.getValue();
	
	@Override
	public boolean usesMaterial(Material material) {
		return (material == MaterialX.WOODEN_SWORD.getMaterial()
				|| material == MaterialX.STONE_SWORD.getMaterial()
				|| material == MaterialX.IRON_SWORD.getMaterial()
				|| material == MaterialX.GOLDEN_SWORD.getMaterial()
				|| material == MaterialX.DIAMOND_SWORD.getMaterial()
				|| material == MaterialX.NETHERITE_SWORD.getMaterial());
	}
	
	public static final SettingObject<Integer> DurationConfig = 
			abilitySettings.new SettingObject<Integer>(Detection.class,
			"Duration", 3, "# �ɷ� ���ӽð�") {

		@Override
		public boolean condition(Integer value) {
			return value >= 1;
		}

	};
	
	public static final SettingObject<Integer> RangeConfig = abilitySettings.new SettingObject<Integer>(
			Detection.class,
			"Range", 3, "# ���� ����", "# ����! ���� ���� Change ���Ǳ� ���� �� ����˴ϴ�.") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};	
	
	public static final SettingObject<Boolean> ChangeConfig = abilitySettings.new SettingObject<Boolean>(
			Detection.class,
			"Change", false, "# true�� �����Ͻø� ���� �õ��� �����",
			"�Ϻ� ���� ���� ��� �÷��̾��� ���� ������ �����մϴ�.") {
	};
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (noatk.contains(e.getDamager().getUniqueId()) && e.getEntity() == getPlayer()) {
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer().getLocation(), 1, 1.7f);
			e.setCancelled(true);
		}
		if (dur == true) {
				if (e.getEntity().equals(getPlayer())) {
					e.setDamage(e.getDamage() + 1);
			}
		}
	}
	 
	public static final SettingObject<Integer> CooldownConfig = 
			abilitySettings.new SettingObject<Integer>(Detection.class, "Cooldown", 45,
            "# ��Ÿ��") {

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
    
	public void TargetSkill (Material material, LivingEntity entity) {
		if ((material.equals(Material.WOOD_SWORD) || 
				material.equals(Material.STONE_SWORD) || 
				material.equals(Material.IRON_SWORD) || 
				material.equals(Material.GOLD_SWORD) || 
				material.equals(Material.DIAMOND_SWORD)) 
				&& entity instanceof Player && !skill.isCooldown()) {
			
			Player p = (Player) entity;
			if (config) {
				for (Player player : LocationUtil.getNearbyEntities(Player.class, p.getLocation(), range, range, predicate)) {
					Participant target = getGame().getParticipant(player);
					new AbilityTimer(duration * 20) {

						private ActionbarChannel actionbarChannel;

						@Override
						protected void run(int count) {
							actionbarChannel.update("��e" + getPlayer().getName() + " ��f���� ��c���� ���� �Ҵ� ��7: ��f" + (getFixedCount()) + " ��");
						}

						@Override
						protected void onStart() {
							noatk.add(target.getPlayer().getUniqueId());
							dur = true;
							actionbarChannel = target.actionbar().newChannel();
						}

						@Override
						protected void onEnd() {
							noatk.remove(target.getPlayer().getUniqueId());
							dur = false;
							if (actionbarChannel != null)
								actionbarChannel.unregister();
						}

						@Override
						protected void onSilentEnd() {
							noatk.remove(target.getPlayer().getUniqueId());
							dur = false;
							if (actionbarChannel != null)
								actionbarChannel.unregister();
						}
					}.setPeriod(TimeUnit.TICKS, 1).start();		
					SoundLib.ENTITY_IRON_GOLEM_DEATH.playSound(target.getPlayer(), 10, 0.5f);
					getPlayer().sendMessage("��e" + target.getPlayer().getName() + " ��f���� ������ �����Ͽ����ϴ�.");
				}
				SoundLib.BLOCK_ENCHANTMENT_TABLE_USE.playSound(getPlayer());
				ParticleLib.ENCHANTMENT_TABLE.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 500, 0);
				skill.start();
			} else {
				Participant target = getGame().getParticipant(p);
				new AbilityTimer(duration * 20) {

					private ActionbarChannel actionbarChannel;

					@Override
					protected void run(int count) {
						actionbarChannel.update("��e" + getPlayer().getName() + " ��f���� ��c���� ���� �Ҵ� ��7: ��f" + (getFixedCount()) + " ��");
					}

					@Override
					protected void onStart() {
						noatk.add(target.getPlayer().getUniqueId());
						dur = true;
						actionbarChannel = target.actionbar().newChannel();
					}

					@Override
					protected void onEnd() {
						noatk.remove(target.getPlayer().getUniqueId());
						dur = false;
						if (actionbarChannel != null)
							actionbarChannel.unregister();
					}

					@Override
					protected void onSilentEnd() {
						noatk.remove(target.getPlayer().getUniqueId());
						dur = false;
						if (actionbarChannel != null)
							actionbarChannel.unregister();
					}
				}.setPeriod(TimeUnit.TICKS, 1).start();		
				SoundLib.BLOCK_ENCHANTMENT_TABLE_USE.playSound(getPlayer());
				SoundLib.ENTITY_IRON_GOLEM_DEATH.playSound(target.getPlayer(), 10, 0.5f);
				getPlayer().sendMessage("��e" + target.getPlayer().getName() + " ��f���� ������ �����Ͽ����ϴ�.");
				ParticleLib.ENCHANTMENT_TABLE.spawnParticle(getPlayer().getLocation(), 1, 1, 1, 500, 0);
				
				skill.start();
			}
		}		
	}
}