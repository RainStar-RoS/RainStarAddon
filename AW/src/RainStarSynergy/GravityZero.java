package RainStarSynergy;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "���߷�", rank = Rank.A, species = Species.OTHERS, explain = {
		"�ڽ��� �߻��ϴ� ��� �߻�ü�� 5�ʰ� �߷��� ������ �����ϰ� ���ư��ϴ�.",
		"ö�� ��Ŭ�� ��, $[DURATION]�ʰ� �ڽ��� ���� �߷��� ������ ���� �ʽ��ϴ�.",
		"�ɷ� ���� ���� �ٽ� ��Ŭ�� �� ��� ���� �� �ֽ��ϴ�. $[COOLDOWN]",
		"���� ȭ��� ���� ��ü�� 3�ʰ� �߷��� ������ ���ϴ�.",
		"���� ������� �����մϴ�."
})

public class GravityZero extends Synergy implements ActiveHandler {
	
	public GravityZero(Participant participant) {
		super(participant);
	}
	
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.ABILITY_DESTROY || update == Update.RESTRICTION_SET) {
			getPlayer().setGravity(true);
		}
	}
	
	private final Cooldown gravityc = new Cooldown(COOLDOWN.getValue());
	
	LivingEntity target;
	
	public static final SettingObject<Integer> COOLDOWN = synergySettings.new SettingObject<Integer>(GravityZero.class,
			"cooldown", 30, "# ���߷� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> DURATION = synergySettings.new SettingObject<Integer>(GravityZero.class,
			"duration", 10, "# ���ӽð�") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (material == Material.IRON_INGOT && clickType.equals(ClickType.RIGHT_CLICK) && !gravityc.isCooldown()) {
			if (nogravity.isRunning()) {
				nogravity.stop(false);
			} else {
				nogravity.start();
				return true;
			}
		}
		return false;
	}		
	
	private PotionEffect levitation = new PotionEffect(PotionEffectType.LEVITATION, 60, 3, true, false);
	
	@SubscribeEvent
	 public void onProjectileLaunch(ProjectileLaunchEvent e) {
		if (getPlayer().equals(e.getEntity().getShooter())) {
			
				new AbilityTimer(100) {
					
					@Override
					protected void run(int count) {
					}
					
					@Override
					protected void onStart() {
						e.getEntity().setGravity(false);
					}
					
					@Override
					protected void onEnd() {
						e.getEntity().setGravity(true);
					}

					@Override
					protected void onSilentEnd() {
						e.getEntity().setGravity(true);
					}
					
				}.setPeriod(TimeUnit.TICKS, 1).start();
		}
	}
	
	@SubscribeEvent
	private void onEntityDamage(EntityDamageEvent e) {
		if (!e.isCancelled() && getPlayer().equals(e.getEntity()) && e.getCause().equals(DamageCause.FALL)) {
			e.setCancelled(true);
			getPlayer().sendMessage("��a���� ������� ���� �ʽ��ϴ�.");
			SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer());
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Arrow) {
			Arrow ar = (Arrow) e.getDamager();
			if (getPlayer().equals(ar.getShooter()) && !e.getEntity().equals(getPlayer()) && e.getEntity() instanceof LivingEntity) {
				target = (LivingEntity) e.getEntity();
				target.addPotionEffect(levitation);
			}
		}
	}
	
	private final Duration nogravity = (Duration) new Duration(DURATION.getValue() * 20, gravityc) {
		
		@Override
		protected void onDurationProcess(int ticks) {
			getPlayer().setGravity(false);
		}
		
		@Override
		protected void onDurationStart() {
			getPlayer().sendMessage("��b���߷¡�f�� ����˴ϴ�. ����� ���� ��5�߷¡�f�� ������ �����մϴ�.");
		}
		
		@Override
		protected void onDurationEnd() {
			getPlayer().setGravity(true);
			getPlayer().sendMessage("�ٽ� ��5�߷¡�f�� ������ �޽��ϴ�.");
		}
		
		@Override
		protected void onDurationSilentEnd() {
			getPlayer().setGravity(true);
			getPlayer().sendMessage("�ٽ� ��5�߷¡�f�� ������ �޽��ϴ�.");
		}
		
	}.setPeriod(TimeUnit.TICKS, 1);
}