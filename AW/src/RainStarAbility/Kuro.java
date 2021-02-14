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
		"��7���� Ÿ�� �� F ��8- ��5���� ���ܡ�f: �ٶ󺸴� �������� ������ �����մϴ�.",
		" �����ϸ� ������ ������ ���ܽ��� �ֺ� ��ƼƼ���� ����� ���ظ� ������",
		" ��ð� ������ �������� ���������ϴ�. $[SWORD_COOLDOWN]",
		"��7�нú� ��8- ��c���� �����f: ġ������ ���ظ� �Ծ��� ��, ü���� �ִ� ü���� ���ݱ���",
		" ��� ȸ���ϰ� ������ �����մϴ�."
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
