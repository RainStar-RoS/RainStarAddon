package RainStarSynergy;

import org.bukkit.event.player.PlayerChatEvent;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;

@AbilityManifest(
		name = "������", rank = Rank.L, species = Species.OTHERS, explain = {
		"��7ö�� ��Ŭ�� ��8- ��c��Ģ ������f: 8������ ���õ� ��Ģ �� �ϳ��� ���� �����մϴ�.",
		" ���� �Ŀ��� �ٽô� �ٲ� �� �����ϴ�.",
		"��7�нú� ��8- ��c������f: ������ ��Ģ�� ��� �÷��̾�� ��� ������ 1�� �����ϸ�,",
		" 3������ �ѱ� �� ��� ó���˴ϴ�."
		})

public class TrueSilverSword {

	
	@SubscribeEvent
	public void onPlayerChat(PlayerChatEvent e) {
		if (e.getMessage().get)
	}
	
	
}
