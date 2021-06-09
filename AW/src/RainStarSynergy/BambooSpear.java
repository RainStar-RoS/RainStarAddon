package RainStarSynergy;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;

@AbilityManifest(
		name = "��â", rank = Rank.S, species = Species.HUMAN, explain = {
		"��23�ʸ��� 0.5���� ��ȸ��f�� �־�����, �ش� �ð� ���� ���� ���� Ÿ���ϸ�",
		"��󿡰� 4.5���� ���ظ� �����ϴ�. ��â�� ���� �� ���� ��� �����մϴ�.",
		"��â�� �ı��� ��󿡰�, 0.75���� ���ظ��� �����ϴ�."
		})

public class BambooSpear extends Synergy {

	public BambooSpear(Participant participant) {
		super(participant);
	}
	
	
}
