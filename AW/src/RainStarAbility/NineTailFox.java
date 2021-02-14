package RainStarAbility;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.Josa;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.collect.ImmutableMap;

@AbilityManifest(name = "����ȣ", rank = Rank.A, species = Species.ANIMAL, explain = {
		"��7�нú� ��8- ��d�����f: �� $[StackTimer]�ʸ��� ���� 1���� ȹ���մϴ�.",
		" �������� ���� ���� ������� �����Ծ��� �� �߰��� 1�� �� ȹ���մϴ�.",
		" ������ �����ϰ� ���� ��, 5�� ���ĺ��� �ִ� ������� 0.5�� ����մϴ�.",
		" ������ 2���� �����Ͽ� �ִ� 9������ ���� �����մϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��b��ϡ�f: �������� ��Ŀ� ���� ����� ��Ÿ���ϴ�.",
		"��7ö�� ��Ŭ�� ��8- ��d�а���f: ������ 9���� �� ����� �� �ֽ��ϴ�.",
		" ��� �� �а��Ͽ� ��e����ȣ(�а�)��f �ɷ��� �ǰ� ��� ������ �ҽ��ϴ�.",
		"��8[��7HIDDEN��8] ��3����������f: ��o������ ���� ������ �� �������?"
		})

public class NineTailFox extends AbilityBase implements ActiveHandler {
	
	private static final ImmutableMap<DamageCause, String> damageCauses = ImmutableMap.<DamageCause, String>builder()
							.put(DamageCause.BLOCK_EXPLOSION, "ħ�� ����")
							.put(DamageCause.CONTACT, "������ ����")
							.put(DamageCause.CRAMMING, "��ƼƼ ���̿� ����")
							.put(DamageCause.CUSTOM, "Ŀ����")
							.put(DamageCause.DROWNING, "�ͻ�")
							.put(DamageCause.ENTITY_ATTACK, "�Ϲ� ����")
							.put(DamageCause.ENTITY_EXPLOSION, "����")
							.put(DamageCause.ENTITY_SWEEP_ATTACK, "�۾��� ����")
							.put(DamageCause.FALL, "����")
							.put(DamageCause.FALLING_BLOCK, "�������� ��Ͽ� ����")
							.put(DamageCause.FIRE, "��")
							.put(DamageCause.FIRE_TICK, "ȭ��")
							.put(DamageCause.FLY_INTO_WALL, "���� ����")
							.put(DamageCause.HOT_FLOOR, "���׸����")
							.put(DamageCause.LAVA, "���")
							.put(DamageCause.LIGHTNING, "����")
							.put(DamageCause.MAGIC, "����")
							.put(DamageCause.POISON, "��")
							.put(DamageCause.PROJECTILE, "�߻�ü")
							.put(DamageCause.STARVATION, "���ָ�")
							.put(DamageCause.SUFFOCATION, "�л�")
							.put(DamageCause.THORNS, "���� ��æƮ")
							.put(DamageCause.VOID, "���� ������ ������")
							.put(DamageCause.WITHER, "�õ�")
							.build();

	public NineTailFox(Participant participant) {
		super(participant);
	}
	
	public static final SettingObject<Integer> StackTimer = abilitySettings.new SettingObject<Integer>(NineTailFox.class,
			"Stack Timer", 25, "# ���� �ڵ� ���� �ð�") {

		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

	};
	
	private final ActionbarChannel ac = newActionbarChannel();
	private int stack = 2;	
	private int timeget = (StackTimer.getValue() * 20);
	private int timer = (int) (Wreck.isEnabled(GameManager.getGame()) ? Wreck.calculateDecreasedAmount(25) * timeget : timeget);
	private Set<DamageCause> damagetype = new HashSet<>();
	private boolean master = false;
	
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			ac.update("��b���� ����f: " + stack + "��");
			passive.start();
		} else if (update == Update.ABILITY_DESTROY || update == Update.RESTRICTION_SET) {
			ac.unregister();
			stack = 2;
			damagetype.clear();
		}
	}
	
    private final AbilityTimer passive = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
    		if (timer != 0) {
        		if (count % timer == 0 && stack < 9) {
        			stack++;
        			ac.update("��b���� ����f: " + stack + "��");
        		}	
    		} else {
    			if (stack < 9) {
        			stack++;
        			ac.update("��b���� ����f: " + stack + "��");	
    			}
    		}
    	}
    	
    }.setBehavior(RestrictionBehavior.PAUSE_RESUME).setPeriod(TimeUnit.TICKS, 1).register();
	
    @SubscribeEvent
    public void onEntityDamage(EntityDamageEvent e) {
    	if (e.getEntity().equals(getPlayer())) {
			if (!damagetype.contains(e.getCause()) && !e.getCause().equals(DamageCause.SUICIDE) && !e.getCause().equals(DamageCause.DRAGON_BREATH)) {
				damagetype.add(e.getCause());
				getPlayer().sendMessage("[��c!��f] ��c" + damageCauses.get(e.getCause()) + "��f" + KoreanUtil.getJosa(damageCauses.get(e.getCause()), Josa.����) + " ���Ͽ� ������ �����߽��ϴ�.");
				SoundLib.BLOCK_ENCHANTMENT_TABLE_USE.playSound(getPlayer(), 1, 1.4f);
				ParticleLib.ENCHANTMENT_TABLE.spawnParticle(getPlayer().getLocation(), 0.5, 1, 0.5, 200, 1);
				if (damagetype.size() == 24) {
					getPlayer().sendMessage("��8[��7HIDDEN��8] ��� ���� ����� �����Ͽ� ����� �Ϻ��� �а��� �� �ְ� �Ǿ����ϴ�.");
					getPlayer().sendMessage("��8[��7HIDDEN��8] ��3����������f�� �޼��Ͽ����ϴ�.");
	    			SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(getPlayer());
					master = true;
				} else {
		    		if (stack < 9) {
		    			stack++;
		    			ac.update("��b���� ����f: " + stack + "��");
		    		}	
				}
			}
		}
    }
    
    @SubscribeEvent
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
    	onEntityDamage(e);
    }
    
	@SubscribeEvent
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player) {
			e.setDamage(e.getDamage() + ((stack - 4) * 0.5));
		}
		if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getDamager();
			if (getPlayer().equals(arrow.getShooter())) {
				e.setDamage(e.getDamage() + ((stack - 4) * 0.5));
			}
		}
	}
	
	public boolean ActiveSkill(Material material, AbilityBase.ClickType clicktype) {
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.RIGHT_CLICK) && stack == 9) {
	    	if (master) {
	    		SoundLib.ITEM_ARMOR_EQUIP_LEATHER.playSound(getPlayer());
		    	getPlayer().sendMessage("��e���� �а��� �����ϼ̽��ϴ�. ��7/aw check");
		    	AbilityBase ab = getParticipant().getAbility();
		    	if (ab.getClass().equals(Mix.class)) {
		    		final Mix mix = (Mix) ab;
					final AbilityBase first = mix.getFirst(), second = mix.getSecond();
					if (this.equals(first)) {
						try {
							mix.setAbility(NineTailFoxCP.class, second.getClass());
						} catch (ReflectiveOperationException e) {
							e.printStackTrace();
						}
					} else if (this.equals(second)) {
						try {
							mix.setAbility(first.getClass(), NineTailFoxCP.class);
						} catch (ReflectiveOperationException e) {
							e.printStackTrace();
						}
					}
		    	} else {
			    	try {
						getParticipant().setAbility(NineTailFoxCP.class);
					} catch (UnsupportedOperationException | ReflectiveOperationException e) {
						e.printStackTrace();
					}	
		    	}	
	    	} else {
	    		SoundLib.ITEM_ARMOR_EQUIP_LEATHER.playSound(getPlayer());
		    	getPlayer().sendMessage("��e�а��� �����ϼ̽��ϴ�. ��7/aw check");
		    	AbilityBase ab = getParticipant().getAbility();
		    	if (ab.getClass().equals(Mix.class)) {
		    		final Mix mix = (Mix) ab;
					final AbilityBase first = mix.getFirst(), second = mix.getSecond();
					if (this.equals(first)) {
						try {
							mix.setAbility(NineTailFoxC.class, second.getClass());
						} catch (ReflectiveOperationException e) {
							e.printStackTrace();
						}
					} else if (this.equals(second)) {
						try {
							mix.setAbility(first.getClass(), NineTailFoxC.class);
						} catch (ReflectiveOperationException e) {
							e.printStackTrace();
						}
					}
		    	} else {
			    	try {
						getParticipant().setAbility(NineTailFoxC.class);
					} catch (UnsupportedOperationException | ReflectiveOperationException e) {
						e.printStackTrace();
					}	
		    	}
	    	}
	    	return true;
	    }
	    if (material.equals(Material.IRON_INGOT) && clicktype.equals(ClickType.LEFT_CLICK)) {
	    	getPlayer().sendMessage("��c==== ��d���� ��� ��c====");
			final StringJoiner joiner = new StringJoiner("��f, ");
			for (final Entry<DamageCause, String> entry : damageCauses.entrySet()) {
				joiner.add((damagetype.contains(entry.getKey()) ? "��e" : "��7") + entry.getValue());
			}
			getPlayer().sendMessage(joiner.toString());
			getPlayer().sendMessage("��c====================");
	    }
		return false;
	}
	
}
