package RainStarSynergy;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "�ʹٴٴٴٴ�", rank = Rank.A, species = Species.HUMAN, explain = {
		"���� ���� ���ظ� ���� �� 30%�� ���ط� 5�� �����մϴ�.",
		"���� ���ظ� ���� ������ $[InvConfig]ƽ�� ���� ���°� �˴ϴ�."
		})

public class Wadadadadada extends Synergy {

	public Wadadadadada(Participant participant) {
		super(participant);
	}
	
	private LivingEntity target;
	private double dmg = 0;
	
	private final int invincibility = InvConfig.getValue();
	
	public static final SettingObject<Integer> InvConfig = synergySettings.new SettingObject<Integer>(Wadadadadada.class,
			"Inv Time", 14, "# ���� �ð�", "# ������ ƽ�Դϴ�. 20ƽ = 1��") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	private final AbilityTimer attacking = new AbilityTimer(4) {

		@Override
		protected void run(int arg0) {
			target.setNoDamageTicks(0);
			target.damage(dmg, getPlayer());
		}
		
	}.setPeriod(TimeUnit.TICKS, 2).register();
	
	private final AbilityTimer inv = new AbilityTimer(invincibility) {

		@Override
		protected void run(int arg0) {
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) && !e.isCancelled()) {
			if (!inv.isRunning()) {
				inv.start();
			} else {
				SoundLib.ITEM_SHIELD_BLOCK.playSound(getPlayer().getLocation(), 1, 1.2f);
				e.setCancelled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
		if (e.getDamager().equals(getPlayer()) && !attacking.isRunning()) {
			dmg = (3 * (e.getDamage() / 10));
			e.setDamage(dmg);
			target = (LivingEntity) e.getEntity();
			attacking.start();
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}
}