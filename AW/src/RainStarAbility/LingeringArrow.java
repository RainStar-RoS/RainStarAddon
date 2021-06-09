package RainStarAbility;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.config.enums.CooldownDecrease;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.collect.Pair;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.google.common.collect.ImmutableMap;

@AbilityManifest(name = "�ܷ� ȭ��", rank = Rank.B, species = Species.HUMAN, explain = {
		"Ȱ�� �÷��̾ ������ �� ����� ��ġ�� ��������",
		"���� ���븦 �ܷ���ŵ�ϴ�. $[CooldownConfig]",
		"���� ���� ȿ���� �̸� �� �� ������, ö�� ��Ŭ�� ��",
		"�ش� ȿ���� �ѱ� �� �ֽ��ϴ�. $[ActiveConfig]"
		})

@Tips(tip = {
        "���� ��ġ�� 5~20�� ������ ������ ���� ȿ���� �Ŵ�",
        "�ܷ� ������ 10�ʰ� ������Ű�� �ɷ��Դϴ�.",
        "������ ���� ȿ����, ���� ȿ����, �߸��� ȿ���� ����",
        "���� �� ������ ����� ������ �⵵�ϼ���."
}, strong = {
        @Description(subject = "�Ǵܷ�", explain = {
        		"������ �� ���� ���� ȿ���� ���� ���� �Ǵܷ�����",
        		"�����Լ� �Ÿ��� ���� ��, ���� ������ ���� ��",
        		"Ȥ�� ���� ������ �� ��ܳ����� �մϴ�."
        }),
        @Description(subject = "���", explain = {
        		"��������� ���� ȿ���� ȭ���� �ɸ��� ���� �����ϴ�.",
        		"����� ������ �⵵�ϼ���."
        })
}, weak = {
        @Description(subject = "�ҿ�", explain = {
        		"������ ���� ȿ���� ȭ���� �ɾ������ ��ȿ���� ������?",
        		"�ҿ��� ���⸦ �⵵�ϼ���."
        })
}, stats = @Stats(offense = Level.ZERO, survival = Level.ZERO, crowdControl = Level.TWO, mobility = Level.ZERO, utility = Level.ZERO), difficulty = Difficulty.EASY)

public class LingeringArrow extends AbilityBase implements ActiveHandler {
	
	public LingeringArrow(Participant participant) {
		super (participant);
	}
	
	private final Cooldown arrowC = new Cooldown(CooldownConfig.getValue(), CooldownDecrease._50);
	private final Cooldown activeC = new Cooldown(ActiveConfig.getValue(), CooldownDecrease._50);
	private ActionbarChannel actionbar = newActionbarChannel();
	private Random random = new Random();
	
	public static final SettingObject<Integer> CooldownConfig 
	= abilitySettings.new SettingObject<Integer>(LingeringArrow.class,
			"Cooldown", 12, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	}, ActiveConfig = abilitySettings.new SettingObject<Integer>(LingeringArrow.class,
			"Active_Cooldown", 30, "# ���� ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};

	private PotionEffectType potionEffect = new ArrayList<>(POTION_TYPES.keySet()).get(random.nextInt(POTION_TYPES.size()));

	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			potionEffect = new ArrayList<>(POTION_TYPES.keySet()).get(random.nextInt(POTION_TYPES.size()));
			actionbar.update("��b���� ȿ����f: ��e" + POTION_TYPES.get(potionEffect).getLeft());
		}
	}

	public boolean ActiveSkill(Material material, ClickType clicktype) {
		if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK) && !activeC.isCooldown()) {
			potionEffect = new ArrayList<>(POTION_TYPES.keySet()).get(random.nextInt(POTION_TYPES.size()));
			actionbar.update("��b���� ȿ����f: ��e" + POTION_TYPES.get(potionEffect).getLeft());
			return activeC.start();
		}
		return false;
	}
	
	private static final ImmutableMap<PotionEffectType, Pair<String, Color>> POTION_TYPES 
	= ImmutableMap.<PotionEffectType, Pair<String, Color>>builder()
			.put(PotionEffectType.REGENERATION, Pair.of("��d���", PotionEffectType.REGENERATION.getColor()))
			.put(PotionEffectType.SPEED, Pair.of("��b�ż�", PotionEffectType.SPEED.getColor()))
			.put(PotionEffectType.FIRE_RESISTANCE, Pair.of("��cȭ�� ����", PotionEffectType.FIRE_RESISTANCE.getColor()))
			.put(PotionEffectType.HEAL, Pair.of("��dġ��", PotionEffectType.HEAL.getColor()))
			.put(PotionEffectType.NIGHT_VISION, Pair.of("��9�߰� ����", PotionEffectType.NIGHT_VISION.getColor()))
			.put(PotionEffectType.INCREASE_DAMAGE, Pair.of("��6��", PotionEffectType.INCREASE_DAMAGE.getColor()))
			.put(PotionEffectType.JUMP, Pair.of("��a���� ��ȭ", PotionEffectType.JUMP.getColor()))
			.put(PotionEffectType.WATER_BREATHING, Pair.of("��3���� ȣ��", PotionEffectType.WATER_BREATHING.getColor()))
			.put(PotionEffectType.INVISIBILITY, Pair.of("��7����ȭ", PotionEffectType.INVISIBILITY.getColor()))
			.put(PotionEffectType.LUCK, Pair.of("��a���", PotionEffectType.LUCK.getColor()))
			.put(PotionEffectType.POISON, Pair.of("��2��", PotionEffectType.POISON.getColor()))
			.put(PotionEffectType.WEAKNESS, Pair.of("��7������", PotionEffectType.WEAKNESS.getColor()))
			.put(PotionEffectType.SLOW, Pair.of("��8����", PotionEffectType.SLOW.getColor()))
			.put(PotionEffectType.HARM, Pair.of("��4����", PotionEffectType.HARM.getColor()))
			// �� �Ʒ��� ���� ȿ����
			.put(PotionEffectType.WITHER, Pair.of("��0�õ�", Color.fromRGB(1, 1, 1)))
			.put(PotionEffectType.ABSORPTION, Pair.of("��e���", Color.fromRGB(254, 246, 18)))
			.put(PotionEffectType.BLINDNESS, Pair.of("��7�Ǹ�", Color.fromRGB(140, 140, 140)))
			.put(PotionEffectType.CONFUSION, Pair.of("��5�ֹ�", Color.fromRGB(171, 130, 18)))
			.put(PotionEffectType.FAST_DIGGING, Pair.of("��e������", Color.fromRGB(254, 254, 143)))
			.put(PotionEffectType.GLOWING, Pair.of("��f�߱�", Color.fromRGB(254, 254, 254)))
			.put(PotionEffectType.HEALTH_BOOST, Pair.of("��c����� ��ȭ", Color.fromRGB(254, 178, 217)))
			.put(PotionEffectType.HUNGER, Pair.of("��2���", Color.fromRGB(134, 229, 127)))
			.put(PotionEffectType.LEVITATION, Pair.of("��5���� �ξ�", Color.fromRGB(171, 18, 151)))
			.put(PotionEffectType.SATURATION, Pair.of("��e������", Color.fromRGB(254, 221, 115)))
			.put(PotionEffectType.SLOW_DIGGING, Pair.of("��8ä�� �Ƿ�", Color.fromRGB(93, 93, 93)))
			.put(PotionEffectType.UNLUCK, Pair.of("��a�ҿ�", Color.fromRGB(206, 242, 121)))
			.put(PotionEffectType.DAMAGE_RESISTANCE, Pair.of("��8����", Color.fromRGB(1, 96, 106)))
			.build();
	
	
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (NMS.isArrow(e.getDamager())) {
			Arrow arrow = (Arrow) e.getDamager();
			if (getPlayer().equals(arrow.getShooter()) && e.getEntity() instanceof Player
					&& !e.getEntity().equals(getPlayer()) && !arrowC.isCooldown()) {
				final Player target = (Player) e.getEntity();
				AreaEffectCloud AEC
					= target.getPlayer().getWorld().spawn(target.getPlayer().getLocation().add(0, 0.2, 0), 
							AreaEffectCloud.class);
				AEC.setDuration(200);
				AEC.addCustomEffect(new PotionEffect((potionEffect), (random.nextInt(15) + 5) * 20, 0), true);
				AEC.setColor(POTION_TYPES.get(potionEffect).getRight());
				AEC.setWaitTime(0);
				getPlayer().sendMessage(POTION_TYPES.get(potionEffect).getLeft() + "��f ���밡 �����Ǿ����ϴ�.");
				arrowC.start();

				potionEffect = new ArrayList<>(POTION_TYPES.keySet()).get(random.nextInt(POTION_TYPES.size()));
				actionbar.update("��b���� ȿ����f: ��e" + POTION_TYPES.get(potionEffect).getLeft());
			}
		}
	}
	
}