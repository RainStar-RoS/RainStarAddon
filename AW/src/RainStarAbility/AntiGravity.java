package RainStarAbility;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;

@AbilityManifest(name = "���߷�", rank = Rank.C, species = Species.OTHERS, explain = {
		"�ڽ��� �߻��ϴ� ��� �߻�ü�� 1ƽ �� �����մϴ�.",
		"ö�� ��Ŭ�� ��, ������ ��� �߻�ü�� ���� �����մϴ�.",
		"ö�� ��Ŭ�� ��, �������� �ɸ��� �ð��� ���� �����մϴ�.",
		"�� �ɷ��� ���� ���� �� ��5���߷� ���� ŰƮ��f�� �����մϴ�.",
		"��5���߷� ���� ŰƮ ��7: ��fȭ�� 1��Ʈ, ��ô ��2����f/��4�����f 1 ���� ���� 2��"
		})

@Tips(tip = {
        "�߻�ü�� ����� �� �ִٴ� ���� �� �츮�ž� �մϴ�. �̸��׸� ���� ���ָ�",
        "Ȱ���Ͽ� ���߿� ����ΰ� ���ϴ� ���� �ڷ���Ʈ�� �����ϰ�,",
        "�����̳� ȭ�� ���� ����ξ� ����� �ѹ��� ������ �����մϴ�.",
        "�ٸ� ȭ���� ����� ���ư��� ���ϱ� ������ �⺻ ȭ�캸�� ����� ��ƽ��ϴ�."
}, strong = {
        @Description(subject = "Ʈ��", explain = {
                "�ɷ��� �𸣴� ��븦 �̸� �غ��� �� ȭ�� �� ������",
                "����� Ʈ���� �̿��Ͽ� ������ ������ �� �ֽ��ϴ�."
        })
}, weak = {
        @Description(subject = "���Ÿ���", explain = {
                "ȭ���� �ָ� ���ư��� ����ٴ� �� ������ ���Ÿ� ���� ����մϴ�.",
                "�ǵ����̸� ��븦 ���� �Ѿƿ��� ������ �����ϼ���."
        })
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.ZERO, mobility = Level.ZERO, utility = Level.TWO), difficulty = Difficulty.HARD)

public class AntiGravity extends AbilityBase implements ActiveHandler {
	
	public AntiGravity(Participant participant) {
		super(participant);
	}
	
	ActionbarChannel ac = newActionbarChannel();
	
	private final Map<Projectile, Vector> velocityMap = new HashMap<>();
	private static final Vector zerov = new Vector(0, 0, 0);
	private boolean arrows = true;
	private int timer = 1;
	
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR && arrows == true) {
			
			ItemStack poison = new ItemStack(Material.SPLASH_POTION, 2);
			PotionMeta pmeta = (PotionMeta) poison.getItemMeta();
			ItemStack instdmg = new ItemStack(Material.SPLASH_POTION, 2);
			PotionMeta imeta = (PotionMeta) instdmg.getItemMeta();
			ItemStack arrow = new ItemStack(Material.ARROW, 64);
			ItemMeta ameta = arrow.getItemMeta();
			
			pmeta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 200, 0), true);
			pmeta.setColor(PotionEffectType.POISON.getColor());
			pmeta.setDisplayName("��5���߷� ���� ŰƮ ��7- ��2�� ����");
			imeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 0), true);
			imeta.setColor(PotionEffectType.HARM.getColor());
			imeta.setDisplayName("��5���߷� ���� ŰƮ ��7- ��4���� ����");
			ameta.setDisplayName("��5���߷� ���� ŰƮ ��7- ��fȭ��");
			ameta.addEnchant(Enchantment.MENDING, 1, true);
			
			poison.setItemMeta(pmeta);
			instdmg.setItemMeta(imeta);
			arrow.setItemMeta(ameta);
			
			getPlayer().getInventory().addItem(poison);
			getPlayer().getInventory().addItem(instdmg);
			getPlayer().getInventory().addItem(arrow);
			arrows = false;
			
			ac.update("��b���� �ð� ��f: " + timer + "ƽ ��");
		}
		
		if (update == Update.ABILITY_DESTROY || update == Update.RESTRICTION_SET) {
			for (Entry<Projectile, Vector> entry : velocityMap.entrySet()) {
				velocityMap.forEach(Projectile::setVelocity);
				entry.getKey().setGravity(true);
			}
			velocityMap.clear();
		}
	}
	
	@SubscribeEvent
	 public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if (getPlayer().equals(e.getEntity().getShooter())) {
			
				new AbilityTimer(timer) {
					
					@Override
					protected void run(int count) {
					}
					
					@Override
					protected void onEnd() {
						velocityMap.put(e.getEntity(), e.getEntity().getVelocity());
						e.getEntity().setGravity(false);
						e.getEntity().setVelocity(zerov);
					}

					@Override
					protected void onSilentEnd() {
						velocityMap.put(e.getEntity(), e.getEntity().getVelocity());
						e.getEntity().setGravity(false);
						e.getEntity().setVelocity(zerov);
					}
					
				}.setPeriod(TimeUnit.TICKS, 1).start();
		}
	}
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(AbilityBase.ClickType.LEFT_CLICK)) {
	    	
			for (Entry<Projectile, Vector> entry : velocityMap.entrySet()) {
					velocityMap.forEach(Projectile::setVelocity);
					entry.getKey().setGravity(true);
			}
			velocityMap.clear();
	    }
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(AbilityBase.ClickType.RIGHT_CLICK)) {
	    	if (timer == 1) {
	    		timer = 3;
	    	} else if (timer == 3) {
	    		timer = 5;
	    	} else if (timer == 5) {
	    		timer = 1;
	    	}
	    	ac.update("��b���� �ð� ��f: " + timer + "ƽ ��");
	    }
	    return false;
	}
}