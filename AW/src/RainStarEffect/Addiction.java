package RainStarEffect;

import org.bukkit.Color;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.game.AbstractGame.Effect;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.collect.ImmutableMap;

@EffectManifest(name = "���� �ߵ�", displayName = "��5���� �ߵ�", method = ApplicationMethod.UNIQUE_LONGEST, type = {
}, description = {
		"���� �迭�� ���� ȿ������ ����� 1 �������ϴ�."
})
public class Addiction extends Effect {

	public static final EffectRegistration<Addiction> registration = EffectRegistry.registerEffect(Addiction.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
		registration.apply(participant, timeUnit, duration);
	}

	private final Participant participant;
	private final RGB violet = RGB.of(107, 5, 169);
	
	private static final ImmutableMap<PotionEffectType, Pair<String, Color>> POTION_TYPES_BAD 
	= ImmutableMap.<PotionEffectType, Pair<String, Color>>builder()
			.put(PotionEffectType.POISON, Pair.of("��2��", PotionEffectType.POISON.getColor()))
			.put(PotionEffectType.WEAKNESS, Pair.of("��7������", PotionEffectType.WEAKNESS.getColor()))
			.put(PotionEffectType.SLOW, Pair.of("��8����", PotionEffectType.SLOW.getColor()))
			.put(PotionEffectType.HARM, Pair.of("��4����", PotionEffectType.HARM.getColor()))
			// �� �Ʒ��� ���� ȿ����
			.put(PotionEffectType.WITHER, Pair.of("��0�õ�", Color.fromRGB(1, 1, 1)))
			.put(PotionEffectType.BLINDNESS, Pair.of("��7�Ǹ�", Color.fromRGB(140, 140, 140)))
			.put(PotionEffectType.CONFUSION, Pair.of("��5�ֹ�", Color.fromRGB(171, 130, 18)))
			.put(PotionEffectType.GLOWING, Pair.of("��f�߱�", Color.fromRGB(254, 254, 254)))
			.put(PotionEffectType.HUNGER, Pair.of("��2���", Color.fromRGB(134, 229, 127)))
			.put(PotionEffectType.LEVITATION, Pair.of("��5���� �ξ�", Color.fromRGB(171, 18, 151)))
			.put(PotionEffectType.SLOW_DIGGING, Pair.of("��8ä�� �Ƿ�", Color.fromRGB(93, 93, 93)))
			.put(PotionEffectType.UNLUCK, Pair.of("��a�ҿ�", Color.fromRGB(206, 242, 121)))
			.build();

	public Addiction(Participant participant, TimeUnit timeUnit, int duration) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration));
		this.participant = participant;
		setPeriod(TimeUnit.TICKS, 1);
	}
	
	@Override
	protected void onStart() {
    	for (PotionEffect pe : participant.getPlayer().getActivePotionEffects()) {
    		if (POTION_TYPES_BAD.containsKey(pe.getType())) {
				participant.getPlayer().removePotionEffect(pe.getType());
				PotionEffect newpe = new PotionEffect(pe.getType(), pe.getDuration(), pe.getAmplifier() + 1, false, true);
				participant.getPlayer().addPotionEffect(newpe);
    		}
    	}
		SoundLib.ITEM_BOTTLE_FILL_DRAGONBREATH.playSound(participant.getPlayer().getLocation(), 1, 1);
		super.onStart();
	}
	
	@Override
	protected void run(int count) {
		ParticleLib.SPELL_MOB.spawnParticle(participant.getPlayer().getLocation().clone().add(0, 0.5, 0), violet);
		super.run(count);
	}

	@Override
	protected void onEnd() {
		onSilentEnd();
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
    	for (PotionEffect pe : participant.getPlayer().getActivePotionEffects()) {
    		if (POTION_TYPES_BAD.containsKey(pe.getType())) {
				participant.getPlayer().removePotionEffect(pe.getType());
				PotionEffect newpe = new PotionEffect(pe.getType(), pe.getDuration(), pe.getAmplifier() - 1, false, true);
				participant.getPlayer().addPotionEffect(newpe);
    		}
    	}
		super.onSilentEnd();
	}
	
}
