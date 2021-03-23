package RainStarAbility;

import org.bukkit.attribute.Attribute;
import org.bukkit.event.player.PlayerMoveEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.annotations.Beta;

@Beta

@AbilityManifest(name = "��Ÿ R", rank = Rank.A, species = Species.OTHERS, explain = {
		"�׽�Ʈ�� ��Ÿ �ɷ��Դϴ�.",
		"���� ����� �˹� ������ �˹���� �ʴ� �÷��̾�� �ο� ��,",
		"����� �� ���̶� �����̸� �˹� ���װ� Ǯ���� �˴ϴ�.",
		"���� ������ �ʱ�ȭ��ŵ�ϴ�."
		})

public class BetaR extends AbilityBase {

	public BetaR(Participant participant) {
		super(participant);
	}
	
	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		e.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
	}
}
