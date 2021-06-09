package RainStarSynergy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import RainStarEffect.ElectricShock;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer.TaskType;

@AbilityManifest(
		name = "�Ϸ�Ʈ��", rank = Rank.L, species = Species.DEMIGOD, explain = {
		"���� ��� FŰ�� ������ �ٶ󺸴� �������� ������ ������",
		" $[COOLDOWN]",
		"���� ���� ������ ���� ����� $[SHOCK_DURATION]�ʰ� ����, $[STUN_DURATION]�ʰ� �����ϰ� �˴ϴ�."
		})

public class Electric extends Synergy implements ActiveHandler {

	public Electric(Participant participant) {
		super(participant);
	}
	
	public static final SettingObject<Integer> COOLDOWN = 
			synergySettings.new SettingObject<Integer>(Electric.class, "cooldown", 70,
            "# ��Ÿ��") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };
    
	public static final SettingObject<Integer> SHOCK_DURATION = 
			synergySettings.new SettingObject<Integer>(Electric.class, "shock-duration", 14,
            "# ���� ���ӽð�") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
    };
    
	public static final SettingObject<Integer> STUN_DURATION = 
			synergySettings.new SettingObject<Integer>(Electric.class, "stun-duration", 5,
            "# ���� ���ӽð�") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
    };
    
	private final AbilityTimer skill = new AbilityTimer(TaskType.REVERSE, 30) {
		
    	@Override
		public void onStart() {
    	}
		
    	@Override
		public void run(int i) {
			getPlayer().setVelocity(getPlayer().getLocation().getDirection().normalize().setY(0).multiply(2));
			getPlayer().getWorld().strikeLightning(getPlayer().getLocation());
    	}
    	
    	@Override
    	public void onEnd() {
    		onSilentEnd();
    	}
    	
    	@Override
    	public void onSilentEnd() {
    		cool.start();
    	}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final Cooldown cool = new Cooldown(COOLDOWN.getValue());
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
		if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK) 
				&& !cool.isCooldown() && !skill.isRunning()) {
			return skill.start();
		}
		return false;
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (skill.isRunning() && e.getCause().equals(DamageCause.LIGHTNING)) {
			if (e.getEntity().equals(getPlayer())) {
				e.setCancelled(true);
			} else if (e.getEntity() instanceof Player) {
				Player player = (Player) e.getEntity();
				ElectricShock.apply(getGame().getParticipant(player), TimeUnit.TICKS, SHOCK_DURATION.getValue() * 20);
				Stun.apply(getGame().getParticipant(player), TimeUnit.TICKS, STUN_DURATION.getValue() * 20);
			}
		}
	}
	
	
	
}
