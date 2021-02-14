package RainStarAbility;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityBase.AbilityTimer;
import daybreak.abilitywar.ability.AbilityBase.ClickType;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;

@AbilityManifest(name = "Ʈ���̽�", rank = Rank.A, species = Species.HUMAN, explain = {
		"��7ö�� Ÿ�� ��8- ��2Ư�� �������f: ö���� ��� �ٸ� �÷��̾ Ÿ���Ͽ�",
		" Ư�� �����⸦ ��󿡰� ������ŵ�ϴ�. Ư�� ������� �� �ϳ��� �� ��󿡰�",
		" ���� �����ϰ�, �ٸ� �÷��̾ ��Ÿ�� �� �⺻ ������� ������ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��a���á�f: Ư�� �����⸦ ������ ����� �þ߸� �� �� �ֽ��ϴ�.",
		" �ٽ� ö�� ��Ŭ���� �ϸ� ���� ��忡�� ���� �� �ֽ��ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��2��������f: Ư�� �������� EMP�� ���Ľ��� �ֺ� 10ĭ ��",
		" ��� �÷��̾ ���� ���� ���·� ����ϴ�. $[COOLDOWN_CONFIG]",
		" ���� �ڽ��� Ư�� �����Ⱑ �ִ� ��ġ�� �ڷ���Ʈ�մϴ�.",
		"��7�нú� ��8- ��a���������f: �����⸦ ������ ���� ü���� �� �� �ֽ��ϴ�.",
		" ���� ����� �ɷ��� ����� ������ ������ �� �ֽ��ϴ�."
		})

public class Trace extends AbilityBase implements ActiveHandler {
	
	public Trace(Participant participant) {
		super(participant);
	}
	
	protected void onUpdate(Update update) {
	    if (update == AbilityBase.Update.RESTRICTION_CLEAR) {
	    	if (target != null) {
				passive.start();
	    	}
	    }
	}
	
	private Player target;
	private boolean monitoring = false;
	
	private final AbilityTimer passive = new AbilityTimer() {
		
    	@Override
		public void run(int count) {
    		
    	}
    	
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player) {
			target = (Player) e.getEntity();
		}
	}
	
	@SubscribeEvent()
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getPlayer().equals(getPlayer()) && monitoring) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (e.getPlayer().equals(getPlayer()) && monitoring) {
			e.setCancelled(true);
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
		if (e.getPlayer().equals(getPlayer()) && monitoring) {
			e.setCancelled(true);
		}
	}
	
	
	public boolean ActiveSkill(Material material, ClickType clicktype) {
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK)) {
	    	if (target != null) {
	    		if (monitoring) {
			    	NMS.setCamera(getPlayer(), getPlayer());
			    	monitoring = false;	
	    		} else {
			    	NMS.setCamera(getPlayer(), target);
			    	monitoring = true;	
	    		}
	    	}
	    }
	    return false;
	}

}
