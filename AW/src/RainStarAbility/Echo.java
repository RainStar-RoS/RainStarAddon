package RainStarAbility;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "�޾Ƹ�", rank = Rank.S, species = Species.OTHERS, explain = {
		"��7�нú� ��8- ��cī���͡�f: �ٸ� �÷��̾�� ���� ���ظ� ���� ��",
		" ��󿡰� 0.5�� ���� ���� ���ظ� ���� �� ���ط���ŭ ȸ���ϰ�",
		" ����� �� ���ط��� �ǵ����ݴϴ�. $[COOLDOWN]",
		" �̶� �ݰݱ��� �ɸ� �ð��� �ݺ���� �ݰ� ���ذ� 1.5�迡�� 0.1����� �����˴ϴ�.",
		"��7��Ÿ�� �нú� ��8- ��c���÷�����f: ��Ÿ�� ���� ī���� ������ ������ ��,",
		" ī���� ȿ���� �ߵ����� �ʰ� ��Ÿ���� 5�ʾ� �پ��ϴ�."})

@Tips(tip = {
        "���̴�ġ�� ���� ���ظ� �ǵ����ִ� �ݰ� ���� �ɷ��Դϴ�.",
        "�� �⺻ ���ؿ� ���� ����� �� ���ط��� ���� ��󿡰� ġ������",
        "���ظ� ������, �� �ڽ��� ����� �� ���ظ�ŭ �ٽ� ȸ���մϴ�.",
        "������ �ٰŸ� ���طθ� �ߵ��Ǳ⿡, ���Ÿ� ���س� ���� ���ظ�",
        "�����ؾ� �մϴ�."
}, strong = {
        @Description(subject = "�� ���� ������� ���� ���", explain = {
                "�� �� �游 ���� ������� ����Ŀ ���� �ɷ��̶��",
                "�� �ɷ����� ������ �ݰ��� ��󿡰� ������ ���� ��",
                "�ֽ��ϴ�."
        }),
        @Description(subject = "������ �ɷ�", explain = {
                "���� ������ ���ϴ� �� �ɷ¿��Դ� ����� ��� �ִ�",
                "���� ��� �ִ� ������ �ɷ��� �����ϴ�."
        }),
        @Description(subject = "���� ��æƮ", explain = {
                "���ô� ���� ����� ������ �ݰ� ��æƮ�Դϴ�.",
                "���� ��æƮ�� �����Ѵٸ�, �ݰ� ������� ������ �ϱ⵵",
                "���� ��� �ݰݵǰ���?"
        }),
        @Description(subject = "���� ����", explain = {
                "������� �ݰ��ؾ� �ϴ� �� �ɷ¿��� ����� ���ݿ���",
                "�˹�Ǿ �ָ� ���ư��� �ʴ� ���� ������ �����մϴ�."
        })
}, weak = {
        @Description(subject = "���� ���� �� �ٸ� Ÿ���� ����", explain = {
                "���Ÿ� ����, ���ƿ��� ���� ��. �޾Ƹ��� �ƹ��͵�",
                "�ݰ��� �� �����ϴ�. �ִ��� ���شٴϼ���."
        }),
        @Description(subject = "�˹�", explain = {
                "���� �˹��� ���� �������� ���� �� ��󿡰� �ݰ��Ϸ� ���� ����",
                "�ݰ� �ð��� ���� �� �ֽ��ϴ�..."
        })
}, stats = @Stats(offense = Level.FIVE, survival = Level.FIVE, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.NORMAL)

public class Echo extends AbilityBase {
	
	public Echo(Participant participant) {
		super(participant);
	}
	
	private final ActionbarChannel ac = newActionbarChannel();
	private final Cooldown cool = new Cooldown(COOLDOWN.getValue(), CooldownDecrease._50);
	private int stack = 0;
	private static final RGB color = RGB.of(189, 189, 189);
	
	public static final SettingObject<Integer> COOLDOWN = abilitySettings.new SettingObject<Integer>(Echo.class,
			"cooldown", 30, "# �ݰ� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	private Player target;
	private double dmg;
	private double finaldmg;
	
	private final AbilityTimer counter = new AbilityTimer(10) {
		
		@Override
		public void run(int count) {
			ac.update("��c�ݰ� ������f: ��e" + target.getName() + "��f, " + (getCount() / 20.0) + "��");
		}
		
		@Override
		public void onEnd() {
			onSilentEnd();
		}
		
		@Override
		protected void onSilentEnd() {
			ac.update(null);
		}
		
    }.setPeriod(TimeUnit.TICKS, 1).register();
    
	private final AbilityTimer particle = new AbilityTimer(3) {
		
		@Override
		public void run(int count) {
			ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().clone().add(Line.vectorAt(getPlayer().getLocation(), target.getPlayer().getLocation(), 3, count - 1)), color);
		}
		
		@Override
		public void onEnd() {
			onSilentEnd();
		}
		
		@Override
		public void onSilentEnd() {
			ParticleLib.ITEM_CRACK.spawnParticle(target.getLocation(), 0.5, 1, 0.5, 10, 0, MaterialX.WHITE_STAINED_GLASS_PANE);
			ParticleLib.ITEM_CRACK.spawnParticle(target.getLocation(), 0.5, 1, 0.5, 10, 0, MaterialX.GRAY_STAINED_GLASS_PANE);
			ParticleLib.ITEM_CRACK.spawnParticle(target.getLocation(), 0.5, 1, 0.5, 10, 0, MaterialX.LIGHT_GRAY_STAINED_GLASS_PANE);
			ParticleLib.ITEM_CRACK.spawnParticle(target.getLocation(), 0.5, 1, 0.5, 10, 0, MaterialX.BLACK_STAINED_GLASS_PANE);
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(getPlayer()) && e.getDamager() instanceof Player) {
			dmg = e.getDamage();
			finaldmg = e.getFinalDamage();
			target = (Player) e.getDamager();
			counter.start();
		}
		if (target != null) {
			if (e.getEntity().equals(target.getPlayer()) && e.getDamager().equals(getPlayer()) && counter.isRunning()) {
				if (cool.isRunning()) {
					cool.setCount(Math.max(cool.getCount() - 5, 0));
				} else {
					particle.start();
					getPlayer().setHealth(Math.min(getPlayer().getHealth() + finaldmg, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
					if (counter.getCount() <= 1) {
						e.setDamage(e.getDamage() + (dmg * 0.1));
						getPlayer().sendMessage("��b>>> ��c0.1�� ����� �ݰ�!");
					} else if (counter.getCount() <= 2) {
						e.setDamage(e.getDamage() + (dmg * 0.2));
						getPlayer().sendMessage("��b>>> ��c0.2�� ����� �ݰ�!");
					} else if (counter.getCount() <= 3) {
						e.setDamage(e.getDamage() + (dmg * 0.25));
						getPlayer().sendMessage("��b>>> ��c0.25�� ����� �ݰ�!");
					} else if (counter.getCount() <= 4) {
						e.setDamage(e.getDamage() + (dmg * 0.5));
						getPlayer().sendMessage("��b>>> ��c0.5�� ����� �ݰ�!");
					} else if (counter.getCount() <= 5) {
						e.setDamage(e.getDamage() + (dmg * 0.75));
						getPlayer().sendMessage("��b>>> ��c0.75�� ����� �ݰ�!");
					} else if (counter.getCount() <= 6) {
						e.setDamage(e.getDamage() + (dmg * 1));
						getPlayer().sendMessage("��b>>> ��c1�� ����� �ݰ�!");
					} else if (counter.getCount() <= 7) {
						e.setDamage(e.getDamage() + (dmg * 1.2));
						getPlayer().sendMessage("��b>>> ��c1.2�� ����� �ݰ�!");
					} else if (counter.getCount() <= 8) {
						e.setDamage(e.getDamage() + (dmg * 1.3));
						getPlayer().sendMessage("��b>>> ��c1.3�� ����� �ݰ�!");
					} else if (counter.getCount() <= 9) {
						e.setDamage(e.getDamage() + (dmg * 1.4));
						getPlayer().sendMessage("��b>>> ��c1.4�� ����� �ݰ�!");
					} else {
						e.setDamage(e.getDamage() + (dmg * 1.5));
						getPlayer().sendMessage("��b>>> ��c1.5�� ����� �ݰ�!");
					}
					counter.stop(false);
					cool.start();
				}
				stack++;
				double temp = (double) stack;
				int soundnumber = (int) (temp - (Math.ceil(temp / SOUND_RUNNABLES.size()) - 1) * SOUND_RUNNABLES.size()) - 1;
				SOUND_RUNNABLES.get(soundnumber).run();
			}
		}
	}
	
	private final List<Runnable> SOUND_RUNNABLES = Arrays.asList(
			
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.G));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.G));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.G));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.G));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.G));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.G));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.F));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.G));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.G));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(1, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(1, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(1, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(1, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.F));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.C));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(0, Tone.F));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.C));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.G));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.G));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.C));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.F));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.C));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.G));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.F));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.sharp(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.F));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.F));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.F));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.D));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.A));
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(1, Tone.A));
			},
			() -> {
					SoundLib.PIANO.playInstrument(getPlayer(), Note.natural(0, Tone.D));
					stack = 0;
			}
			
	);
	
}
