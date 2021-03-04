package RainStarAbility;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(name = "���丣��", rank = Rank.S, species = Species.DEMIGOD, explain = {
		"��c�� �Ӽ���f�� ���� ���˻�, ���丣��.",
		"��7�нú� ��8- ��c��ȭ�� ���Ρ�f: �ڽ��� ��ȭ �ð� 1�ʴ� �ۿ��ϴ� �Ҳ��� �ϳ���",
		" �ִ� 10������ ȹ���� �� �ֽ��ϴ�. �Ҳ� �ϳ��� �ڽ��� �޴� �� �迭 ���ط���",
		" 5%�� ����մϴ�. �ۿ��ϴ� �Ҳ��� ���� ��, ���� �ֵθ��� ���濡 ���� ���ս��ϴ�.",
		"��7�� ���� ��8- ��c��ȭ������f: ",
		"��7�� ��Ŭ�� ��8- ��cȭ��������f: ���� 10�ʰ� �߰� ��ȭ��ŵ�ϴ�.",
		" "
		})

public class Inferno extends AbilityBase {

	public Inferno(Participant participant) {
		super(participant);
	}
	
}
