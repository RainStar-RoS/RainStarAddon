package RainStarAbility.GhostStory;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(name = "��������", rank = Rank.A, species = Species.HUMAN, explain = {
		"��7�нú� ��8- ��c�Ѵ� ���� ����f: $[RestartConfig]�ʰ� ���� �Ĵٺ��� �ʴ� �÷��̾�� ���� �� �� �����ϴ�.",
		"��7���� ���� ��8- ��c�� ��¦�� ����f: �� ���� ���ϴ� ����� ���� �� �Ƿ� ȿ���� �ο��մϴ�.",
		" ���� �Ƿ� ȿ���� �ɸ� �÷��̾�� ��󿡰� �߰� ���ظ� ���� �� �ֽ��ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��c������ �����������f: $[DurationConfig]�ʰ� ���� �ٶ󺸴� ��� ��󿡰�",
		" �Ƿ� ȿ���� �ο��˴ϴ�. $[CooldownConfig]",
		"��2[��a���̵�� �����ڡ�f: ��ejjapdook��2]"
		})

public class AbstractGhostStory extends AbilityBase {
	
	public AbstractGhostStory(Participant participant) {
		super(participant);
	}
	
	

}
