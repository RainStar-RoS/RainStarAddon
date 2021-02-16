package RainStarAbility;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(
		name = "��Ρ����ȡ�", rank = Rank.S, species = Species.OTHERS, explain = {
		"������ ���� �������� ���ϴ� ��8��ҡ�f �Ӽ��� �˻��.",
		"��7�нú� ��8- ��7�̸� ��8����7��� ���֡�8����f: ��ҿ��� �ڶ�, ����� ���� ����ϱ⿡",
		" ���� ���� �ִ� ��ġ�� ��ο�� ��ο���� ���� ��ų�� ����������.",
		"��7�� ��Ŭ�� ��8- ��7���� ��2����a������ �˳���2����f: �ð��� �帧�� �Ųٷ� Ÿ�� ������ �˳���",
		" 3�ʰ� �˳��� �ð��� �Ž������� Ÿ���ߴ� ������ ������ ���ظ� �ٽ� ������",
		" ������ �ִ�. �ɷ� ��� �� ���� �־�� �� ������ �ð����� �ǵ��ƿ��� 6�ʰ�",
		" ���� ���� �̿��� ���ظ� ���� �� ����. $[REVERSE_COOLDOWN]",
		"��7�� ��� F ��8- ��7��� ��3����b�ð� ���ܡ�3����f: �ð��� ƴ���� ������ �����ִ� �����,",
		" �켱 ���� ���� ��������� ���� 10�ʸ� ����Ѵ�. ��� ���Ŀ� �ٶ󺸴� ��������",
		" �Ŵ��� ������ ���� ���� ���� ���� ������ �� �� ���̶� ���ſ�",
		" �� ��ġ���� �����ߴ� �ڵ��� �ð��� ��Ʋ�� ū ���ظ� ���� �� �ִ�. $[TIMECUTTER_COOLDOWN]",
		"��7�нú� ��8- ��4����c���� ���֡�4����f: ���� ���ֹ��� ������ ����ϱ⿡,",
		" ȸ���� ���� �� ���� ���� �Ǿ����� ȸ���� �Ϸ� �� ������ ���",
		" ȸ������ 10%��ŭ �߰� ���ݷ��� �ִ� 10���� ȹ���� �� �ִ�."
		})

public class KuroEye extends AbilityBase {

	public KuroEye(Participant participant) {
		super(participant);
	}
	
}
