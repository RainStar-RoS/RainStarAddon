package RainStarAbility;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(name = "�����ΰ�", rank = Rank.S, species = Species.GOD, explain = {
		"��7�нú� ��8- ��c������f: �� ���� �ΰ��� ���� ä ������ �����մϴ�.",
		" �ٸ� �ΰ��� �ɷ��� ó������ Ȱ��ȭ�� ���� ü�°� �κ��丮�� ������ �����մϴ�.", 
		"��7�ΰ� ��8- ��aȯ�� �ΰݡ�f: ",
		"��7�ΰ� ��8- ��a���� �ΰݡ�f: ����� �ɷ��� ����� ������ �����ϴ�.",
		" ��� �ɷ��� ȯ���̶� ���⿡ ��� �ɷ��� Ÿ���ÿ��� �����Ǹ�,",
		" "
		})

public class DualPersonality extends AbilityBase {
	
	public DualPersonality(Participant participant) {
		super(participant);
	}

}
