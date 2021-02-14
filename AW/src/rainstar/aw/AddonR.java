package rainstar.aw;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import RainStarAbility.Alice;
import RainStarAbility.AntiGravity;
import RainStarAbility.Butcher;
import RainStarAbility.Chronos;
import RainStarAbility.Crystal;
import RainStarAbility.Dash;
import RainStarAbility.DefileGak;
import RainStarAbility.Detection;
import RainStarAbility.Echo;
import RainStarAbility.Empty;
import RainStarAbility.Executioner;
import RainStarAbility.Flex;
import RainStarAbility.GlassCannon;
import RainStarAbility.GuardianAngel;
import RainStarAbility.HohoHaha;
import RainStarAbility.Indecision;
import RainStarAbility.Kairos;
import RainStarAbility.LightningCounter;
import RainStarAbility.LingeringArrow;
import RainStarAbility.LittleDevil;
import RainStarAbility.Mira;
import RainStarAbility.Moros;
import RainStarAbility.MultiHit;
import RainStarAbility.NineTailFox;
import RainStarAbility.NineTailFoxC;
import RainStarAbility.NineTailFoxCP;
import RainStarAbility.PrecisionAiming;
import RainStarAbility.RainStar;
import RainStarAbility.Revenger;
import RainStarAbility.Soda;
import RainStarAbility.Stella;
import RainStarAbility.Teabagging;
import RainStarAbility.Tesla;
import RainStarAbility.TimeStop;
import RainStarAbility.Yuki;
import RainStarSynergy.ASAP;
import RainStarSynergy.Accelerator;
import RainStarSynergy.AkashicRecords;
import RainStarSynergy.Chance;
import RainStarSynergy.Demisoda;
import RainStarSynergy.GravityZero;
import RainStarSynergy.HawkEye;
import RainStarSynergy.HealthCopy;
import RainStarSynergy.HomingPenetrationArrow;
import RainStarSynergy.Infallibility;
import RainStarSynergy.Joker;
import RainStarSynergy.LaplaceDemon;
import RainStarSynergy.MagicShow;
import RainStarSynergy.MomentaryTrip;
import RainStarSynergy.NineTailFoxSynergy;
import RainStarSynergy.PatronSaint;
import RainStarSynergy.SocialDistancing;
import RainStarSynergy.TeslaPlasma;
import RainStarSynergy.TimeBomb;
import RainStarSynergy.TimeTravel;
import RainStarSynergy.Wadadadadada;
import RainStarSynergy.YukiSnow;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.ability.list.Clown;
import daybreak.abilitywar.ability.list.Demigod;
import daybreak.abilitywar.ability.list.DiceGod;
import daybreak.abilitywar.ability.list.EnergyBlocker;
import daybreak.abilitywar.ability.list.Magician;
import daybreak.abilitywar.ability.list.PenetrationArrow;
import daybreak.abilitywar.ability.list.Ruber;
import daybreak.abilitywar.ability.list.Sniper;
import daybreak.abilitywar.ability.list.Terrorist;
import daybreak.abilitywar.ability.list.TimeRewind;
import daybreak.abilitywar.ability.list.VictoryBySword;
import daybreak.abilitywar.ability.list.Void;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.list.mix.AbstractMix;
import daybreak.abilitywar.game.list.mix.synergy.SynergyFactory;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.reflect.ReflectionUtil;

public class AddonR extends Addon implements Listener {
	
	@Override
	public void onEnable() {
		AbilityFactory.registerAbility(PrecisionAiming.class);
		AbilityList.registerAbility(PrecisionAiming.class);
		AbilityFactory.registerAbility(Butcher.class);
		AbilityList.registerAbility(Butcher.class);
		AbilityFactory.registerAbility(Indecision.class);
		AbilityList.registerAbility(Indecision.class);
		AbilityFactory.registerAbility(GlassCannon.class);
		AbilityList.registerAbility(GlassCannon.class);
		AbilityFactory.registerAbility(Detection.class);
		AbilityList.registerAbility(Detection.class);
		AbilityFactory.registerAbility(Empty.class);
		AbilityList.registerAbility(Empty.class);
		AbilityFactory.registerAbility(LingeringArrow.class);
		AbilityList.registerAbility(LingeringArrow.class);
		AbilityFactory.registerAbility(TimeStop.class);
		AbilityList.registerAbility(TimeStop.class);
		AbilityFactory.registerAbility(AntiGravity.class);
		AbilityList.registerAbility(AntiGravity.class);
		AbilityFactory.registerAbility(LightningCounter.class);
		AbilityList.registerAbility(LightningCounter.class);
		AbilityFactory.registerAbility(Chronos.class);
		AbilityList.registerAbility(Chronos.class);
		AbilityFactory.registerAbility(Crystal.class);
		AbilityList.registerAbility(Crystal.class);
		AbilityFactory.registerAbility(MultiHit.class);
		AbilityList.registerAbility(MultiHit.class);
		AbilityFactory.registerAbility(NineTailFox.class);
		AbilityList.registerAbility(NineTailFox.class);
		AbilityFactory.registerAbility(NineTailFoxC.class);
		AbilityFactory.registerAbility(NineTailFoxCP.class);
		AbilityFactory.registerAbility(Echo.class);
		AbilityList.registerAbility(Echo.class);
		AbilityFactory.registerAbility(Flex.class);
		AbilityList.registerAbility(Flex.class);
		AbilityFactory.registerAbility(Dash.class);
		AbilityList.registerAbility(Dash.class);
		AbilityFactory.registerAbility(Revenger.class);
		AbilityList.registerAbility(Revenger.class);
		AbilityFactory.registerAbility(Kairos.class);
		AbilityList.registerAbility(Kairos.class);
		AbilityFactory.registerAbility(LittleDevil.class);
		AbilityList.registerAbility(LittleDevil.class);
		AbilityFactory.registerAbility(Mira.class);
		AbilityList.registerAbility(Mira.class);
		AbilityFactory.registerAbility(GuardianAngel.class);
		AbilityList.registerAbility(GuardianAngel.class);
		AbilityFactory.registerAbility(Stella.class);
		AbilityList.registerAbility(Stella.class);
		AbilityFactory.registerAbility(Moros.class);
		AbilityList.registerAbility(Moros.class);
		AbilityFactory.registerAbility(Yuki.class);
		AbilityList.registerAbility(Yuki.class);
		AbilityFactory.registerAbility(Executioner.class);
		AbilityList.registerAbility(Executioner.class);
		AbilityFactory.registerAbility(Tesla.class);
		AbilityList.registerAbility(Tesla.class);
		AbilityFactory.registerAbility(RainStar.class);
		AbilityList.registerAbility(RainStar.class);
		AbilityFactory.registerAbility(DefileGak.class);
		AbilityList.registerAbility(DefileGak.class);
		AbilityFactory.registerAbility(Soda.class);
		AbilityList.registerAbility(Soda.class);
		AbilityFactory.registerAbility(Alice.class);
		AbilityList.registerAbility(Alice.class);
		AbilityFactory.registerAbility(Teabagging.class);
		AbilityList.registerAbility(Teabagging.class);
		
		AbilityFactory.registerAbility(HohoHaha.class);
		AbilityList.registerAbility(HohoHaha.class);
		
		SynergyFactory.registerSynergy(PrecisionAiming.class, Sniper.class, HawkEye.class);
		SynergyFactory.registerSynergy(AntiGravity.class, AntiGravity.class, GravityZero.class);
		SynergyFactory.registerSynergy(TimeRewind.class, TimeStop.class, TimeTravel.class);
		SynergyFactory.registerSynergy(Dash.class, Dash.class, Accelerator.class);
		SynergyFactory.registerSynergy(EnergyBlocker.class, Indecision.class, SocialDistancing.class);
		SynergyFactory.registerSynergy(PrecisionAiming.class, PrecisionAiming.class, Infallibility.class);
		SynergyFactory.registerSynergy(Kairos.class, Kairos.class, Chance.class);
		SynergyFactory.registerSynergy(Magician.class, Mira.class, MagicShow.class);
		SynergyFactory.registerSynergy(GlassCannon.class, VictoryBySword.class, ASAP.class);
		SynergyFactory.registerSynergy(GuardianAngel.class, GuardianAngel.class, PatronSaint.class);
		SynergyFactory.registerSynergy(PrecisionAiming.class, PenetrationArrow.class, HomingPenetrationArrow.class);
		SynergyFactory.registerSynergy(NineTailFox.class, NineTailFox.class, NineTailFoxSynergy.class);
		SynergyFactory.registerSynergy(MultiHit.class, MultiHit.class, Wadadadadada.class);
		SynergyFactory.registerSynergy(DiceGod.class, Moros.class, LaplaceDemon.class);
		SynergyFactory.registerSynergy(Terrorist.class, TimeStop.class, TimeBomb.class);
		SynergyFactory.registerSynergy(Void.class, Mira.class, MomentaryTrip.class);
		SynergyFactory.registerSynergy(Demigod.class, Soda.class, Demisoda.class);
		SynergyFactory.registerSynergy(Yuki.class, Yuki.class, YukiSnow.class);
		SynergyFactory.registerSynergy(Ruber.class, Crystal.class, HealthCopy.class);
		SynergyFactory.registerSynergy(Tesla.class, Tesla.class, TeslaPlasma.class);
		SynergyFactory.registerSynergy(Clown.class, Alice.class, Joker.class);
		
		new BukkitRunnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					SynergyFactory.registerSynergy(Empty.class, (Class<? extends AbilityBase>) ReflectionUtil.ClassUtil.forName("cokes86.addon.ability.list.DataMining"), AkashicRecords.class);
	            } catch (ClassNotFoundException e) {
	                e.printStackTrace();
	            }
	        }   
	    }.runTaskLater(AbilityWar.getPlugin(), 10L);
		
		Bukkit.broadcastMessage("��a���ν�Ÿ �ֵ�¡�e�� ����Ǿ����ϴ�.");
		Bukkit.broadcastMessage("��e�ɷ� ��f31�� ��7/ ��d�ó��� ��f22�� ���� �Ϸ�.");
		
		Bukkit.getPluginManager().registerEvents(this, getPlugin());
		
	}
	
	@EventHandler()
	public void onGameCredit(GameCreditEvent e) {
		e.addCredit("��a���ν�Ÿ �ֵ�¡�f�� ����Ǿ����ϴ�. ��e�ɷ� ��f31�� ���� �Ϸ�.");
		if (e.getGame() instanceof AbstractMix) {
			e.addCredit("��d�ó��� ��f22�� ���� �Ϸ�.");
		}
		e.addCredit("��a���ν�Ÿ �ֵ�� ��f������ : RainStar_ [��9���ڵ� ��f: RainStar��7#0846��f]");
	}

}