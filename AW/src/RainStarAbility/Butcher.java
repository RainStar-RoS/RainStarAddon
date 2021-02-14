package RainStarAbility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "������", rank = Rank.A, species = Species.HUMAN, explain = {
		"��7�нú� ��8- ��c�����巯��Ʈ��f: ����� ü���� �������� �� ���ϰ�,",
		" �������� �� ���ϰ� �����մϴ�. ����� ü���� ���� �̻��� ��",
		" ��a�����f ��ƼŬ��, �ƴ϶�� ��c������f ��ƼŬ�� ���ϴ�.",
		"��7ų ��8- ��cī�Ϲ߸����f: ����� �׿��� ���, ���� ���� ü�¸�ŭ ü���� ȸ���մϴ�."
		})

public class Butcher extends AbilityBase {
	
	public Butcher(Participant participant) {
		super(participant);
	}
	
	private static final RGB MajorityHealth = RGB.of(134, 229, 127);
	private static final RGB MinorityHealth = RGB.of(183, 0, 0);
	
	@SubscribeEvent
	public void onEntityDamageByEntity (EntityDamageByEntityEvent e) {
		
		if (e.getDamager() instanceof Arrow) {
		    Arrow arrow = (Arrow) e.getDamager();
		    if (getPlayer().equals(arrow.getShooter()) && e.getEntity() instanceof LivingEntity) {
		    	final LivingEntity target = (LivingEntity) e.getEntity();
				e.setDamage(Math.min(12 ,Math.max(e.getDamage() + (2 / Math.max(0.2, target.getHealth() / target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())) - 4, 0.75)));				
				if (target.getHealth() >= target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2) {
					
					ParticleLib.REDSTONE.spawnParticle(target.getLocation().add(0, 2.2, 0), MajorityHealth);
				} else {
					ParticleLib.REDSTONE.spawnParticle(target.getLocation().add(0, 2.2, 0), MinorityHealth);
		    }
		}}
		
		if (e.getDamager(). equals(getPlayer()) && e.getEntity() instanceof LivingEntity) {
			final LivingEntity targets = (LivingEntity) e.getEntity();
			e.setDamage(Math.min(12 ,Math.max(e.getDamage() + (2 / Math.max(0.2, targets.getHealth() / targets.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())) - 4, 0.75)));
			
			if (targets.getHealth() >= targets.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2) {
				ParticleLib.REDSTONE.spawnParticle(targets.getLocation().add(0, 2.2, 0), MajorityHealth);
			} else {
				ParticleLib.REDSTONE.spawnParticle(targets.getLocation().add(0, 2.2, 0), MinorityHealth);
			}
		}

	}
	
	@SubscribeEvent
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (getPlayer().equals(e.getEntity().getKiller())) { 			
			new AbilityTimer(10) {				
				private final Location startLocation = e.getEntity().getLocation().clone();
				
				@Override
				protected void run(int count) {
					ParticleLib.DAMAGE_INDICATOR.spawnParticle(startLocation.clone().add(Line.vectorAt(startLocation, getPlayer().getLocation(), 30, 30 - count)), 0, 0, 0, 1, 0);
					ParticleLib.HEART.spawnParticle(startLocation.clone().add(Line.vectorAt(startLocation, getPlayer().getLocation(), 30, 30 - count)), 0, 0, 0, 1, 0);					
				}
				
				@Override
				protected void onEnd() {
					final EntityRegainHealthEvent event = new EntityRegainHealthEvent(getPlayer(), getPlayer().getHealth(), RegainReason.CUSTOM);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled()) {
						getPlayer().setHealth(Math.min(getPlayer().getHealth() * 2, getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
					}
					SoundLib.ENTITY_ZOMBIE_VILLAGER_CURE.playSound(getPlayer().getLocation(), 1, 0.5f);	
				}
				
			}.setPeriod(TimeUnit.TICKS, 2).start();
		}
	}
};