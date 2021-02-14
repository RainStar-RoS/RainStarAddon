package RainStarAbility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

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
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "�����δ�", rank = Rank.A, species = Species.HUMAN, explain = {
		"�� / �ٰŸ� ���� �� �ϳ��� ��c����f, �ϳ��� ��d����f�� �մϴ�.",
		"��c����f�� ��쿡�� ��c2.5�� �߰� �������f��, ��d����f�� ��쿡�� ��d1ĭ ȸ����f���ݴϴ�.",
		"�� ȿ���� ���� ���ؼ��� �ߵ��մϴ�. ���� ����ü�� $[CountConfig]�� Ÿ���� ������",
		"������ ���� Ÿ���� �ڹٲ�ϴ�.",
		"ö�� ��Ŭ�� ��, ������ Ÿ�� Ƚ���� �ʱ�ȭ�մϴ�. $[CooldownConfig]"})

@Tips(tip = {
        "�ٰŸ� / ���Ÿ� ������ �Ϻ��ϰ� �����س� �� �ִ� �ϼ��� �����Դϴ�.",
        "�ٸ� �䱸�ϴ� �������� �������� �ʴ´ٸ�, �ϼ��� ������ �ǰ���?"
}, strong = {
        @Description(subject = "�Ƿ�", explain = {
        		"�� �ɷ��� ����� �Ƿ¿� ���� �߰������ 2.5�� ����ũ ����",
        		"����ؼ� ���� �� �ֽ��ϴ�!"
        })
}, weak = {
        @Description(subject = "�Ƿ�", explain = {
                "�� �ɷ��� ����� �Ƿ¿� ���� ������ �Ź� ü�� 1ĭ��",
                "����ؼ� ȸ���� �� �� �ֽ��ϴ�!"
        })
}, stats = @Stats(offense = Level.SIX, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.VERY_HARD)

public class Indecision extends AbilityBase implements ActiveHandler {
	
	private boolean sword = true;

	private static final Circle circle = Circle.of(1, 10);
	
	private int stack = 0;

	public Indecision(Participant participant) {
		super(participant);
	}

	private final ActionbarChannel ac = newActionbarChannel();
	private final ActionbarChannel stackac = newActionbarChannel();
	private final Cooldown reset = new Cooldown(CooldownConfig.getValue());
	private final int count = CountConfig.getValue();
	
	public String getState() {
		if (sword)
			return "��b���Ÿ� ��7: ��dHeal ��8| ��a�ٰŸ� ��7: ��cDeal";
		else
			return "��b���Ÿ� ��7: ��cDeal ��8| ��a�ٰŸ� ��7: ��dHeal";
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			ac.update(getState());
			stackac.update("��7Ÿ�� Ƚ�� : ��f" + stack + "��7 ȸ");
		}
	}
	
	public static final SettingObject<Integer> CooldownConfig = abilitySettings.new SettingObject<Integer>(Indecision.class,
			"Cooldown", 60, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};	
	
	public static final SettingObject<Integer> CountConfig = abilitySettings.new SettingObject<Integer>(Indecision.class,
			"Count", 3, "# Ÿ�� �䱸 Ƚ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};	
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(AbilityBase.ClickType.RIGHT_CLICK)
	    		&& !reset.isCooldown()) {
	    	stack = 0;
			stackac.update("��7Ÿ�� Ƚ�� : ��f" + stack + "��7 ȸ");
			reset.start();
	    	return true;
	    }
	    return false;
	}   		
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {

		if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getDamager();
			if (arrow.getShooter().equals(getPlayer()) && e.getEntity() instanceof LivingEntity) {
				final LivingEntity target = (LivingEntity) e.getEntity();

				if (sword == false) {
					e.setDamage(e.getDamage() + 2.5);

					new AbilityTimer(5) {
						@Override
						protected void run(int count) {
							Location center = target.getLocation().clone().add(0, 1 - count * 0.2, 0);
							for (Location loc : circle.toLocations(center).floor(center.getY())) {
								ParticleLib.DAMAGE_INDICATOR.spawnParticle(loc, 0, 0, 0, 1, 0);
							}
						}
					}.setPeriod(TimeUnit.TICKS, 1).start();
					SoundLib.GUITAR.playInstrument(getPlayer(), new Note(1, Tone.A, false));
					SoundLib.GUITAR.playInstrument(getPlayer(), new Note(1, Tone.A, false));
					SoundLib.GUITAR.playInstrument(getPlayer(), new Note(1, Tone.A, false));
				} else {
   					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), 2, RegainReason.CUSTOM);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						target.setHealth(Math.min(target.getHealth() + 2, target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
					}
					e.setCancelled(true);
					(arrow).remove();

					new AbilityTimer(5) {
						@Override
						protected void run(int count) {
							Location center = target.getLocation().clone().add(0, 1 - count * 0.2, 0);
							for (Location loc : circle.toLocations(center).floor(center.getY())) {
								ParticleLib.HEART.spawnParticle(loc);
							}
						}
					}.setPeriod(TimeUnit.TICKS, 1).start();
					SoundLib.CHIME.playInstrument(getPlayer(), new Note(1, Tone.G, false));
					SoundLib.CHIME.playInstrument(getPlayer(), new Note(1, Tone.G, false));
					SoundLib.CHIME.playInstrument(getPlayer(), new Note(1, Tone.G, false));
				}
				stack++;
				stackac.update("��7Ÿ�� Ƚ�� : ��f" + stack + "��7 ȸ");
				if (stack >= count) {
					sword = !sword;
					ac.update(getState());
					stack = 0;
					stackac.update("��7Ÿ�� Ƚ�� : ��f" + stack + "��7 ȸ");
				}
			}
		}

		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof LivingEntity) {
			final LivingEntity target = (LivingEntity) e.getEntity();

			if (sword == true) {
				e.setDamage(e.getDamage() + 2.5);

				new AbilityTimer(5) {
					@Override
					protected void run(int count) {
						Location center = target.getLocation().clone().add(0, 1 - count * 0.2, 0);
						for (Location loc : circle.toLocations(center).floor(center.getY())) {
							ParticleLib.DAMAGE_INDICATOR.spawnParticle(loc, 0, 0, 0, 1, 0);
						}
					}
				}.setPeriod(TimeUnit.TICKS, 1).start();
				SoundLib.GUITAR.playInstrument(getPlayer(), new Note(1, Tone.A, false));
				SoundLib.GUITAR.playInstrument(getPlayer(), new Note(1, Tone.A, false));
				SoundLib.GUITAR.playInstrument(getPlayer(), new Note(1, Tone.A, false));
			} else {
				final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), 2, RegainReason.CUSTOM);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					target.setHealth(Math.min(target.getHealth() + 2, target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
				}
				
				e.setCancelled(true);

				new AbilityTimer(5) {
					@Override
					protected void run(int count) {
						Location center = target.getLocation().clone().add(0, 1 - count * 0.2, 0);
						for (Location loc : circle.toLocations(center).floor(center.getY())) {
							ParticleLib.HEART.spawnParticle(loc);
						}
					}
				}.setPeriod(TimeUnit.TICKS, 1).start();
				SoundLib.CHIME.playInstrument(getPlayer(), new Note(1, Tone.G, false));
				SoundLib.CHIME.playInstrument(getPlayer(), new Note(1, Tone.G, false));
				SoundLib.CHIME.playInstrument(getPlayer(), new Note(1, Tone.G, false));
			}
			stack++;
			stackac.update("��7Ÿ�� Ƚ�� : ��f" + stack + "��7 ȸ");
			if (stack >= count) {
				sword = !sword;
				ac.update(getState());
				stack = 0;
				stackac.update("��7Ÿ�� Ƚ�� : ��f" + stack + "��7 ȸ");
			}
		}
	}
}