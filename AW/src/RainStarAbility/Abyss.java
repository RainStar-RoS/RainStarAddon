package RainStarAbility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityFactory.AbilityRegistration;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.Configuration;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.collect.SetUnion;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.google.common.collect.ImmutableMap;

@AbilityManifest(
		name = "���", rank = Rank.L, species = Species.OTHERS, explain = {
		"�������� ���� �� ����� ���� �ִ� �ɷ� �� �ϳ��� �����ɴϴ�.",
		"������ �ɷ��� �ɿ��� �ɷ��� �Ǿ� ",
		"ö���� ��� FŰ�� ������ �ɿ� �ӿ��� �ɷ� �ϳ��� �ִ� 10������ �����ɴϴ�.",
		"ö�� ��Ŭ�� ��, ���� ��� �ɿ��� �ɷ��� ����մϴ�. $[COOLDOWN]",
		"�ɿ��� �ɷ��� ����ü���� $[COUNT]�� �������� ������ ������ϴ�.",
		"�ɿ��� �ɷ� �ϳ��� �޴� ���ط��� 1.5�� �����մϴ�.",
		"��� ��, ���� ���� ����� ������ �ִ� �ɷ��� ������ �� �ɷ��� �˴ϴ�.",
		"��8=============== ��7���� �ɷ� ��8===============",
		"$(EXPLAIN)"
		})

public class Abyss extends AbilityBase implements ActiveHandler, TargetHandler {

	public Abyss(Participant participant) {
		super(participant);
	}
	
	private List<AbilityBase> abilities = new ArrayList<>();
    Random random = new Random();
	
	private static final ImmutableMap<Rank, String> rankcolor = ImmutableMap.<Rank, String>builder()
			.put(Rank.C, "��e")
			.put(Rank.B, "��b")
			.put(Rank.A, "��a")
			.put(Rank.S, "��d")
			.put(Rank.L, "��6")
			.put(Rank.SPECIAL, "��c")
			.build();
	
	public AbilityRegistration getRandomAbility() {
		
		Set<AbilityRegistration> myAbilities = new HashSet<>();
		
        for (AbilityBase ab : abilities) {
        	myAbilities.add(ab.getRegistration());
        }
		
		final List<AbilityRegistration> registrations = AbilityList.values().stream().filter(
				ability -> !Configuration.Settings.isBlacklisted(ability.getManifest().name()) && !myAbilities.contains(ability)
				&& !ability.getManifest().name().equals("���")
		).collect(Collectors.toList());
		return registrations.isEmpty() ? null : random.pick(registrations);
	}
	
	private final AbilityTimer cool = new AbilityTimer(10) {
		
		@Override
		public void run(int count) {
		}
		
    }.setPeriod(TimeUnit.TICKS, 1).register();

    @SubscribeEvent(onlyRelevant = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
    	if (e.getOffHandItem().getType().equals(Material.IRON_INGOT)) {
    		if (!cool.isRunning()) {
				try {
					abilities.add(AbilityBase.create(getRandomAbility(), getParticipant()));
				} catch (ReflectiveOperationException e1) {
					e1.printStackTrace();
				}
    		}
    		e.setCancelled(true);
    	}
    }
	
	@SuppressWarnings("unused")
	private final Object EXPLAIN = new Object() {
		@Override
		public String toString() {
			if (abilities != null) {
				if (abilities.size() == 0) {
					return "�ɷ��� �����ϴ�.".toString();
				} else {
					final StringJoiner joiner = new StringJoiner("��7, ");
					for (AbilityBase ab : abilities) {
						joiner.add(rankcolor.get(ab.getRank()) + ab.getName());
					}
					return joiner.toString();
				}	
			} else {
				return "�ɷ��� �����ϴ�.".toString();
			}
		}
	};

	@Override
	public boolean ActiveSkill(Material material, ClickType clickType) {
		if (!abilities.isEmpty()) {
			for (AbilityBase ability : abilities) {
				if (ability instanceof ActiveHandler) {
					ActiveHandler active = (ActiveHandler) ability;
					
					((ActiveHandler) ability).ActiveSkill(material, clickType);	
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void TargetSkill(Material material, LivingEntity entity) {
		if (!abilities.isEmpty()) {
			for (AbilityBase ability : abilities) {
				if (ability instanceof TargetHandler) {
					((TargetHandler) ability).TargetSkill(material, entity);
				}	
			}
		}
	}

	@Override
	public Set<GameTimer> getTimers() {
		if (!abilities.isEmpty()) {
			Set<GameTimer> timers = super.getTimers();
			for (AbilityBase ability : abilities) {
				if (ability != null) {
					timers = SetUnion.union(timers, ability.getTimers());
				}
			}
			return timers;
		}
		return super.getTimers();
	}

	@Override
	public Set<GameTimer> getRunningTimers() {
		if (!abilities.isEmpty()) {
			Set<GameTimer> timers = super.getRunningTimers();
			for (AbilityBase ability : abilities) {
				if (ability != null) {
					timers = SetUnion.union(timers, ability.getRunningTimers());
				}
			}
			return timers;
		}
		return super.getRunningTimers();
	}

	@Override
	public boolean usesMaterial(Material material) {
		if (!abilities.isEmpty()) {
			for (AbilityBase ability : abilities) {
				if (ability != null) {
					return ability.usesMaterial(material);
				}
			}
		}
		return super.usesMaterial(material);
	}

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			if (abilities != null) {
				if (!abilities.isEmpty()) {
					for (AbilityBase ability : abilities) {
						ability.setRestricted(false);		
					}
				}	
			}
		} else if (update == Update.RESTRICTION_SET) {
			if (!abilities.isEmpty()) {
				for (AbilityBase ability : abilities) {
					ability.setRestricted(true);		
				}
			}
		} else if (update == Update.ABILITY_DESTROY) {
			if (!abilities.isEmpty()) {
				abilities.clear();
			}
		}
	}
	
}
