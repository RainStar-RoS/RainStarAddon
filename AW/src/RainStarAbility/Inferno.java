package RainStarAbility;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(name = "���丣��", rank = Rank.S, species = Species.DEMIGOD, explain = {
		"��c�� �Ӽ���f�� ���� ���˻�, ���丣��.",
		"��7�нú� ��8- ��c��ȭ�� ���Ρ�f: ȭ�� �迭 ���ظ� ���� ������ ��c�ۡ�6����e�ϴ� ��6�ҡ�c�ɡ�f�� �ϳ��� ȹ���մϴ�.",
		" ���� �ֵθ� ������ �Ҳ� �ټ����� �Ҹ��� ȭ�� �˱⸦ ���ս��ϴ�.",
		" �Ҳ��� ",
		"��7�� ���� ��8- ��c��ȭ������f: ",
		"��7�� ��Ŭ�� ��8- ��cȭ��������f: ���� 10�ʰ� �߰� ��ȭ��ŵ�ϴ�.",
		" "
		})

public class Inferno extends AbilityBase {

	public Inferno(Participant participant) {
		super(participant);
	}
	
}
