package RainStarAbility;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.player.PlayerMoveEvent;

import RainStarEffect.Irreparable;
import RainStarEffect.SnowflakeMark;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;

@Beta

@AbilityManifest(name = "�˹���ġ", rank = Rank.A, species = Species.OTHERS, explain = {
		"�˹� ���� ��ġ�� �ɷ�",
		"���� ����� �˹� ������ �˹���� �ʴ� �÷��̾�� �ο� ��,",
		"����� �� ���̶� �����̸� �˹� ���װ� Ǯ���� �˴ϴ�."
		})

public class KnockbackPatch extends AbilityBase implements ActiveHandler {

	public KnockbackPatch(Participant participant) {
		super(participant);
	}
	
	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		e.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
	}
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
		if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK)) {
			SnowflakeMark.apply(getParticipant(), TimeUnit.SECONDS, 200, 1);
			return true;
		}
		return false;
	}
	
	
}
