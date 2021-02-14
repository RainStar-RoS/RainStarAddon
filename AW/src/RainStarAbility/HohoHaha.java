package RainStarAbility;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import RainStarEffect.DimensionDistortion;
import RainStarEffect.ElectricShock;
import RainStarEffect.Irreparable;
import RainStarEffect.SnowflakeMark;
import cokes86.addon.effects.GodsPressure;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.list.Zombie.Infection;
import daybreak.abilitywar.ability.AbilityBase.AbilityTimer;
import daybreak.abilitywar.ability.AbilityBase.RestrictionBehavior;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Bleed;
import daybreak.abilitywar.game.manager.effect.EvilSpirit;
import daybreak.abilitywar.game.manager.effect.Hemophilia;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Points;
import daybreak.abilitywar.utils.library.ParticleLib;

@Beta

@AbilityManifest(name = "��Ÿ��", rank = Rank.A, species = Species.OTHERS, explain = {
		"�׽�Ʈ�� ��Ÿ �ɷ��Դϴ�.",
		"���� ����� �˹� ������ �˹���� �ʴ� �÷��̾�� �ο� ��,",
		"����� �� ���̶� �����̸� �˹� ���װ� Ǯ���� �˴ϴ�."
		})

public class HohoHaha extends AbilityBase {

	public HohoHaha(Participant participant) {
		super(participant);
	}
	
	@SubscribeEvent
	public void onPlayerMove(PlayerMoveEvent e) {
		e.getPlayer().getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0);
	}
}
