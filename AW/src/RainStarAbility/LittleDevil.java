package RainStarAbility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.Tips.Description;
import daybreak.abilitywar.ability.Tips.Difficulty;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import daybreak.google.common.base.Predicate;

@AbilityManifest(
		name = "���� �Ǹ�",
		rank = Rank.A, 
		species = Species.UNDEAD, 
		explain = {
		"��7ö�� ��Ŭ�� ��8- ��6Ʈ�� ���� Ʈ��!��f: �ֺ� $[RANGE_CONFIG]ĭ ���� ��� �÷��̾���",
		" �κ��丮�� ���� ���� �Ҹ��� �������� �ִٸ� �� �� �޾ư��ϴ�.",
		" ���� �Ҹ��� �������� �� �ϳ��� ���ų� �̹� Ʈ���� ������ ��󿡰� �峭�� ��",
		" ����� �ֹ� 1~9�� �������� �������� �ڼ����ϴ�. $[COOLDOWN_CONFIG]"
		})

@Tips(tip = {
        "�ٸ� �÷��̾��� �Ҹ����� ������ ����� ������ �����ϰ�,",
        "�� �ڽ��� �� �Ҹ����� ����Ͽ� �������̳� �⵿�� ���� ���� ��",
        "�ֽ��ϴ�. ���� ��󿡰� �Ҹ����� ���ٸ� ����� �������� ������",
        "���� ȸ�ǳ� �ɷ� ���� �� ������ â���� �� �ְ�, �������� �������ϴ�."
}, strong = {
        @Description(subject = "���� â��", explain = {
                "����� �Ҹ��� �������� ������ ���� ���� ��",
                "�ڽſ��� �̵��� �Ǵ� ������ �� �� �ֽ��ϴ�."
        }),
        @Description(subject = "�Ҹ��� �������� ���� �÷��̾�", explain = {
                "����� �Ҹ��� �������� ������ ��������",
                "�ɷ��� �� ������ ����ؼ� ������ �� �ֽ��ϴ�."
        })
}, weak = {
        @Description(subject = "Ÿ�̹�", explain = {
                "���ϴ� �������� �����ϱ� ���ؼ��� ����� ����",
                "�� ���� Ÿ�̹��� ����� �մϴ�."
        })
}, stats = @Stats(offense = Level.ZERO, survival = Level.THREE, crowdControl = Level.FOUR, mobility = Level.ZERO, utility = Level.SIX), difficulty = Difficulty.VERY_EASY)

public class LittleDevil extends AbilityBase implements ActiveHandler {
	
	public LittleDevil(Participant participant) {
		super(participant);
	}
	
	public static final SettingObject<Integer> COOLDOWN_CONFIG 
	= abilitySettings.new SettingObject<Integer>(LittleDevil.class,
			"cooldown", 60, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};
	
	public static final SettingObject<Integer> RANGE_CONFIG 
	= abilitySettings.new SettingObject<Integer>(LittleDevil.class,
			"range", 7, "# �ɷ� ����") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}
	};
	
	private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue(), CooldownDecrease._50);
	private Set<Player> playercount = new HashSet<>();
	
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler &&
								((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(
							entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant)
							|| (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK) && !cooldown.isCooldown()) {
			List<Player> players = LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), RANGE_CONFIG.getValue(), predicate);
	    	if (players.size() != 0) {
				for (Player p : players) {
					SoundLib.ENTITY_WITCH_AMBIENT.playSound(p, 1, 1.8f);
					Inventory inventory = p.getPlayer().getInventory();
					List<ItemStack> list = new CopyOnWriteArrayList<>(inventory.getContents());
					for (ItemStack itemStack : list) {
						if (itemStack == null) {
							list.remove(itemStack);
							continue;
						}
						if (!playercount.contains(p)) {
							if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.LINGERING_POTION ||
									itemStack.getType() == Material.SPLASH_POTION || itemStack.getType() == Material.ENDER_PEARL
									|| itemStack.getType() == Material.GOLDEN_APPLE || itemStack.getType() == Material.TOTEM) {
								if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.LINGERING_POTION ||
									itemStack.getType() == Material.SPLASH_POTION) {
									getPlayer().getInventory().addItem(itemStack);
									p.getInventory().removeItem(itemStack);
									SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer());
									playercount.add(p);
									break;
								} else {
									ItemLib.addItem(getPlayer().getInventory(), itemStack.getType(), 1);
									ItemLib.removeItem(p.getInventory(), itemStack.getType(), 1);
									SoundLib.ENTITY_ITEM_PICKUP.playSound(getPlayer());
									playercount.add(p);
									break;
								}
							} else {
								list.remove(itemStack);	
							}	
						} else {
							list.remove(itemStack);
						}
					}

					if (list.size() == 0) {
						Inventory inv = p.getPlayer().getInventory();
						List<ItemStack> slots = new ArrayList<>();
						slots.add(inv.getItem(0));
						slots.add(inv.getItem(1));
						slots.add(inv.getItem(2));
						slots.add(inv.getItem(3));
						slots.add(inv.getItem(4));
						slots.add(inv.getItem(5));
						slots.add(inv.getItem(6));
						slots.add(inv.getItem(7));
						slots.add(inv.getItem(8));
						Collections.shuffle(slots);
						for (int a=0;a<9;a++){ inv.setItem(a, slots.get(a)); }
					}
				}	
				cooldown.start();
				return true;
	    	} else {
	    		getPlayer().sendMessage("�ֺ� ��6" + RANGE_CONFIG.getValue() + "ĭ��f ���� �峭�� ĥ ����� �����ϴ�.");
	    		return false;
	    	}
	    }
		return false;
	}
	
}
