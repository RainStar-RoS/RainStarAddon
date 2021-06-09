package RainStarSynergy;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;

@AbilityManifest(
		name = "���� ������ ��", rank = Rank.A, species = Species.OTHERS, explain = {
		"��7�нú� ��8- ��e���ϴá�f: �̵��� ������ �ð��� ������ ���� �ٲߴϴ�.",
		" ���� �Ǳ� ������ ��� �ɷ��� ����� �� �����ϴ�.",
		"��7ǥ�� ��8- ��e��������f: ���� ������ �õ��� ������ ��󿡰� ǥ���� �ο��մϴ�.",
		" ǥ���� �ο��� ������ ����� ���� ����������"
		})

public class StarryNight extends Synergy {
	
	public StarryNight(Participant participant) {
		super(participant);
	}

}
