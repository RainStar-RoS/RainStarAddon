package RainStarAbility;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(
		name = "���", rank = Rank.A, species = Species.HUMAN, explain = {
		"��7�нú� ��8- ��8����� �����ڡ�f: �ڽ��� �ִ� ��ġ�� ��ο���� ��ų ���ط��� �����մϴ�.",
		"��7���� Ÿ�� �� F ��8- ��5���� ���ܡ�f: �ٶ󺸴� �������� ������ �����մϴ�. ���� ��",
		" ������ ������ ���ܽ��� �ֺ� ��ƼƼ���� ����� ���ظ� ������",
		" ��ð� ������ �������� ���������ϴ�. $[SWORD_COOLDOWN]",
		"��7ö�� ��Ŭ�� ��8- ��8�ɿ��� �θ���f: �ֺ� $[RANGE]ĭ ���� ��� �÷��̾ $[DURATION]�ʰ�",
		" ���� �ٶ� ä õõ�� �� ������ ħ�Ĵ��մϴ�. $[ACTIVE_COOLDOWN]",
		"��7�нú� ��8- ��c���� �����f: ü���� 25% ������ �� ��Ⱑ 2 ������ ������",
		" ������ ������ ü���� ���ݱ��� ȸ��, ��� �ɷ��� ��ȭ��ŵ�ϴ�."
		})

public class Kuro extends AbilityBase {

	public Kuro(Participant participant) {
		super(participant);
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (getPlayer().equals(e.getDamager())) {
			
		}
	}
	
}
