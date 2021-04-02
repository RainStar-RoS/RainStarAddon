package RainStarAbility;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "�ϰ��ʻ�", rank = Rank.A, species = Species.HUMAN, explain = {
		"��7�нú� ��8- ��c�� �濡 ���١�f: �� �� ���� ���� Ÿ���� �� ���� ���� ó������",
		" ���� Ÿ���� �� 2.5���� ���ط� �����ϴ�.",
		" ���� �⺻ ���ط����θ� �����ϰ�, ����� Ÿ���ϸ� Ÿ���Ҽ��� ��󿡰� �ִ�",
		" �⺻ ���ط��� ����� �����մϴ�.",
		"��7�нú� ��8- ��c������ų��f: �� �� ���� Ÿ���� �� ���� ���� �� ���� óġ�� ���",
		" ��� ���� Ÿ�� ī������ �ʱ�ȭ�Ǹ�, ü���� ���� ���ط���ŭ ȸ���մϴ�."
		},
		summarize = {
		"�� �� ���� ���� Ÿ���� �� ���� ���� ���� Ÿ�ݽ� 2.5���� ���ظ� �����ϴ�.",
		"�� ȿ���� ���� óġ�� ��� ���� Ÿ�� ī������ ����, ü���� ȸ���մϴ�.",
		"Ÿ���� ���� �ִ� ������ �⺻ ���ط��� ����� �����մϴ�."
		})

public class OneShotOneKill extends AbilityBase {

	public OneShotOneKill(Participant participant) {
		super(participant);
	}
	
	private Map<Player, Integer> attackCounter = new HashMap<>();
	private double lastDamage = 0;
	private Player dead;
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager().equals(getPlayer()) && !e.isCancelled()) {
			if (e.getEntity() instanceof Player) {
				Player player = (Player) e.getEntity();
				if (!attackCounter.containsKey(player)) {
					new AbilityTimer(5) {
						
						@Override
						public void run(int count) {
							SoundLib.ENTITY_PLAYER_ATTACK_STRONG.playSound(getPlayer(), 1, 1.15f);
						}
						
					}.setPeriod(TimeUnit.TICKS, 1).start();
					SoundLib.ENTITY_POLAR_BEAR_WARNING.playSound(getPlayer().getLocation());
					ParticleLib.CRIT.spawnParticle(player.getLocation().add(0, 1, 0), .3f, .3f, .3f, 100, 1);
					e.setDamage(e.getDamage() * 2.5);
					attackCounter.put(player, 1);
					if (player.getHealth() - e.getFinalDamage() <= 0) {
						new AbilityTimer(2) {
							
							@Override
							public void onStart() {
								dead = player;
								lastDamage = e.getFinalDamage();
							}
							
							@Override
							public void onEnd() {
								onSilentEnd();
							}
							
							@Override
							public void onSilentEnd() {
								dead = null;
								lastDamage = 0;
							}
							
						}.setPeriod(TimeUnit.TICKS, 1).start();
					}
				} else {
					e.setDamage(Math.max(e.getDamage() / 3, e.getDamage() - (attackCounter.get(player) * 0.3)));
					attackCounter.put(player, attackCounter.get(player) + 1);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (dead != null) {
			if (e.getEntity().equals(dead)) {
				attackCounter.clear();
				Healths.setHealth(getPlayer(), getPlayer().getHealth() + lastDamage);
				SoundLib.ENTITY_ZOMBIE_VILLAGER_CURE.playSound(getPlayer().getLocation(), 1, 0.75f);
				ParticleLib.ITEM_CRACK.spawnParticle(dead.getEyeLocation(), .3f, .3f, .3f, 100, 0.5, MaterialX.REDSTONE);
			}
		}
	}
	
}
