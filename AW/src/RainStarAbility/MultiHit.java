package RainStarAbility;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "�ٴ���Ʈ", rank = Rank.A, species = Species.HUMAN, explain = {
		"���� ���� ���ظ� ���� �� 40%�� ���ط� 3�� �����մϴ�.",
		"���� ���ظ� ���� ������ $[InvConfig]ƽ�� ���� ���°� �˴ϴ�."
		})

@Tips(tip = {
        "������� �Ͻ������� ����������, �� 3���� �����ϱ⿡ ����������",
        "1.2���� ���ظ� ���� �� �ִ� �ɷ��Դϴ�. ���� ��ĭ ������ ���ظ�",
        "���� ������ �� �־� �ٴ���Ʈ �ɷ¿��� ���մϴ�."
}, strong = {
        @Description(subject = "������ ���� ���", explain = {
        		"���� ������� ���� �� Ÿ���ϱ� ������,",
        		"������ ���� ��󿡰� ������� ����� ���� �� �ֽ��ϴ�."
        }),
        @Description(subject = "�ٴ���Ʈ�� ���ϴ� ���", explain = {
        		"�Ʒ��� ���� ������� ���Ӱ����� �ϴ� ���ظ�",
        		"0.5���� �������� �������� ���� �� �ֽ��ϴ�."
        })
}, weak = {
        @Description(subject = "������ ���� ���", explain = {
        		"���� ������� ���� �� Ÿ���ϴ� ���� ������",
        		"������ ���� ��󿡰Դ� ������� ������ ��",
        		"���� ���ɼ��� �ֽ��ϴ�."
        })
}, stats = @Stats(offense = Level.FOUR, survival = Level.FOUR, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.VERY_EASY)

public class MultiHit extends AbilityBase {

	public MultiHit(Participant participant) {
		super(participant);
	}
	
	private LivingEntity target;
	private double dmg = 0;
	
	private final int invincibility = InvConfig.getValue();
	
	public static final SettingObject<Integer> InvConfig = abilitySettings.new SettingObject<Integer>(MultiHit.class,
			"Inv Time", 10, "# ���� �ð�", "# ������ ƽ�Դϴ�. 20ƽ = 1��") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	private final AbilityTimer attacking = new AbilityTimer(2) {

		@Override
		protected void run(int arg0) {
			target.setNoDamageTicks(0);
			target.damage(dmg, getPlayer());
		}
		
	}.setPeriod(TimeUnit.TICKS, 3).register();
	
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
			dmg = (2 * (e.getDamage() / 5));
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