package RainStarAbility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityBase.AbilityTimer;
import daybreak.abilitywar.ability.AbilityBase.Update;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.ParticleLib;

@AbilityManifest(name = "���丣��", rank = Rank.S, species = Species.DEMIGOD, explain = {
		"��c�� �Ӽ���f�� ���� ���˻�, ���丣��.",
		"��7�нú� ��8- ��c��ȭ�� ���Ρ�f: ȭ���� ���� ���� Ÿ���� ������ ��c�ۿ��ϴ� �Ҳɡ�f��",
		" ����� ��ȭ �ʸ�ŭ ȹ���մϴ�. ��c�Ҳɡ�f�� �� 10������ �� ���� ������ �� �ֽ��ϴ�.",
		" ��� ȭ�� ���ش� ��c�Ҳɡ�f���� ��ü�Ǿ� �ڰ� ��ȭ�� ����˴ϴ�.",
		" �� �ʸ��� ��c�Ҳɡ�f �ϳ��� �Һ�Ǹ�, ��4�Ҳɡ�f�� �ڽ��� ȭ�� ���ذ� 10% ����մϴ�.",
		"��7�� ���� ��8- ��c��ȭ������f: �ڽ��� Ÿ���� ����� 1�ʰ� �߰� ��ȭ��ŵ�ϴ�.",
		" ����� �̹� 2�� �̻� ��ȭ �����̸� ��� ��󿡰� �߰� ���ظ� �����ϴ�.",
		" ��7�߰� ���ط���f: ��e(����� ���� ȭ�� ���ӽð� * 0.2) + (��c�Ҳɡ�e * 0.1)",
		"��7�� ��Ŭ�� ��8- ��cȭ��������f: ���� $[RANGE]ĭ �̳��� ��� �÷��̾",
		" $[DURATION]�ʰ� �߰� ��ȭ��Ű��, ���� ������ ��� ��󿡰� ��cȭ���f �����̻��� �̴ϴ�.",
		" ��ȭ ������ �ƴϴ� ��󿡰Դ� ȿ���� �����ϴ�. $[COOLDOWN]",
		"��7�����̻� ��8- ��cȭ���f: ��� ȭ�� �迭 ���ظ� 2��� �Խ��ϴ�.",
		" ȭ���� ���� �� ������ ���� ȭ�� ���ӽð��� ����� ���ظ� �Խ��ϴ�."
		})

public class Inferno extends AbilityBase {

	public Inferno(Participant participant) {
		super(participant);
	}
	
	private int burningflame = 0;
	private final ActionbarChannel ac = newActionbarChannel();
	
	protected void onUpdate(Update update) {
	    if (update == Update.RESTRICTION_CLEAR) {
	    	passive.start();
	    } 
	}
	
    private final AbilityTimer passive = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
    		if (getPlayer().getFireTicks() >= 0) {
    			if (burningflame <= 0) {
    				getPlayer().setFireTicks(0);
    			}
    		}
    		if (getPlayer().getFireTicks() % 20 == 0) {
    			if (getPlayer().getFireTicks() != burningflame * 20) {
    				getPlayer().setFireTicks(burningflame * 20);
    			}
    		}
    		if (count % 20 == 0) {
        		flameSet(-1);
    		}
    	}
    	
    }.setPeriod(TimeUnit.TICKS, 1).register();
    
	public void flameSet(int value) {
		if (value <= 0) {
			burningflame = Math.max(0, burningflame + value);
		} else {
			burningflame = Math.min(10, burningflame + value);
		}
		ac.update("��c�� ��e" + burningflame);
	}
	
    @SubscribeEvent
    public void onEntityDamage(EntityDamageEvent e) {
    	if (e.getEntity().equals(getPlayer())) {
    		if (e.getCause() == DamageCause.FIRE || e.getCause() == DamageCause.FIRE_TICK ||
					e.getCause() == DamageCause.LAVA || e.getCause() == DamageCause.HOT_FLOOR) {
    			if (burningflame >= 1) {
    				e.setDamage(e.getDamage() + (e.getDamage() * (burningflame * 0.1)));
    			} else {
    				e.setCancelled(true);
    			}
    		}
    	}
    }
    
    @SubscribeEvent
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
    	onEntityDamage(e);
    }
    
    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
    	onEntityDamage(e);
    	if (e.getDamager().equals(getPlayer())) {
    		if (e.getEntity().getFireTicks() >= 40) {
    			e.setDamage(e.getDamage() + (e.getEntity().getFireTicks() * 0.01) + (burningflame * 0.1));
    		} else {
    			e.getEntity().setFireTicks(e.getEntity().getFireTicks() + 20);
    		}
    		if (e.getEntity().getFireTicks() >= 0) {
    			flameSet((int) (e.getEntity().getFireTicks() * 0.05));
    		}
    		Bukkit.broadcastMessage("��c������7: ��f" + e.getFinalDamage());
    	}
    }
    
}
