package RainStarEffect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectConstructor;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;

@EffectManifest(name = "����", displayName = "��c����", method = ApplicationMethod.UNIQUE_LONGEST, type = {
}, description = {
		"������ ����� ���� Ÿ���� �� �߰� ���ظ� �� �� �ֽ��ϴ�."
})
public class Agro extends AbstractGame.Effect implements Listener {

	public static final EffectRegistration<Agro> registration = EffectRegistry.registerEffect(Agro.class);

	public static void apply(Participant participant, TimeUnit timeUnit, int duration, Player applyPlayer, int increasedDamage) {
		registration.apply(participant, timeUnit, duration, "with-player", applyPlayer, increasedDamage);
	}

	private final Participant participant;
	private final Player applyPlayer;
	private final int increasedDamage;

	@EffectConstructor(name = "with-player")
	public Agro(Participant participant, TimeUnit timeUnit, int duration, Player applyPlayer, int increasedDamage) {
		participant.getGame().super(registration, participant, timeUnit.toTicks(duration));
		this.participant = participant;
		this.applyPlayer = applyPlayer;
		this.increasedDamage = increasedDamage;
		setPeriod(TimeUnit.TICKS, 1);
	}

	@Override
	protected void onStart() {
		Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
		super.onStart();
	}

	@EventHandler
	private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity().equals(applyPlayer) && e.getDamager().equals(participant.getPlayer())) {
			e.setDamage(e.getDamage() + increasedDamage);
		}
	}
	
	@Override
	protected void onEnd() {
		HandlerList.unregisterAll(this);
		super.onEnd();
	}

	@Override
	protected void onSilentEnd() {
		HandlerList.unregisterAll(this);
		super.onSilentEnd();
	}
	
}