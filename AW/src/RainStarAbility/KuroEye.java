package RainStarAbility;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(
		name = "��Ρ����ȡ�", rank = Rank.S, species = Species.OTHERS, explain = {
		"��7�нú� ��8- ��7�̸� ��8����� ���֡���f: ��ҿ��� �ڶ�, ����� ���� ����ϱ⿡",
		" ���� ���� �ִ� ��ġ�� ��ο�� ��ο���� ���� ��ų�� ����������.",
		"��7���� Ÿ�� �� F ��8- ��7��� ��3���ð� ���ܡ���f: �ð��� ƴ���� ������ �����ִ� �����,",
		" �켱 ���� ���� ��������� ���� 10�ʸ� ����Ѵ�. ��� ���Ŀ� �ٶ󺸴� ��������",
		" �Ŵ��� ������ ���� ���� ���� ���� ������ �� �� ���̶� ���ſ�",
		" �� ��ġ���� �����ߴ� �ڵ��� �ð��� ��Ʋ�� ū ���ظ� ���� �� �ִ�. $[SWORD_COOLDOWN]",
		"��7�нú� ��8- ��c������ ���֡���f: ���� ���ֹ��� ������ ����ϱ⿡,",
		" ȸ���� ���� �� ���� ���� �Ǿ����� ȸ���� �Ϸ� �� ������ ���",
		" ȸ������ 10%��ŭ �߰� ���ݷ��� �ִ� 10���� ȹ���� �� �ִ�."
		})

public class KuroEye extends AbilityBase {

	public KuroEye(Participant participant) {
		super(participant);
	}
	
}
