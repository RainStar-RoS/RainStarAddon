package RainStarSynergy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.list.mix.synergy.Synergy;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.minecraft.nms.IHologram;
import daybreak.abilitywar.utils.base.minecraft.nms.NMS;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;

@AbilityManifest(name = "Ÿ¥", rank = Rank.S, species = Species.HUMAN, explain = {
		"��7���̾Ƹ�� Ÿ�� ��8- ��b�ºμ���f: ������ ��a���ڡ�f�� �����մϴ�. $[COOLDOWN]",
		" ��e50%��f Ȯ���� �ڽ� Ȥ�� ��밡 ���� ü���� 2/3�� �Ұ� �˴ϴ�.",
		"��7�нú� ��8- ��c���廩���f: �ڽ��� �й��� ��a���ڡ�f���� ��e50%��f�� Ȯ����",
		" ����� �ٲ� ��밡 ��� �й��ϰ� �˴ϴ�.",
		"��7�нú� ��8- ��3��� �������f: �� �� �̻� ������ ������ ��󿡰� �ٽ� ������ �� ��",
		" ���廩�⸦ ������ �ڽ��� �¸� ���θ� �� �� �ֽ��ϴ�."})

public class Sharper extends Synergy {

	public Sharper(Participant participant) {
		super(participant);
	}
	
	@Override
	protected void onUpdate(Update update) {
		if (update == Update.RESTRICTION_CLEAR) {
			passive.start();
		}
	}
	
	public static final SettingObject<Integer> COOLDOWN = synergySettings.new SettingObject<Integer>(Sharper.class,
			"cooldown", 10, "# ��Ÿ��") {
		@Override
		public boolean condition(Integer value) {
			return value >= 0;
		}

		@Override
		public String toString() {
			return Formatter.formatCooldown(getValue());
		}
	};	
	
	private final Cooldown cool = new Cooldown(COOLDOWN.getValue());
	private Player target;
	private Map<Player, Boolean> winornot = new HashMap<>();
	private Set<Player> gambled = new HashSet<>();
	private Map<Player, FateLook> fateMap = new HashMap<>();
	
	private final Predicate<Entity> predicate = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
			if (entity.equals(getPlayer())) return false;
			if (entity instanceof Player) {
				if (!getGame().isParticipating(entity.getUniqueId())
						|| (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
						|| !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
					return false;
				}
				if (getGame() instanceof Teamable) {
					final Teamable teamGame = (Teamable) getGame();
					final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
					return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
				}
			}
			return true;
		}

		@Override
		public boolean apply(@Nullable Entity arg0) {
			return false;
		}
	};
	
	public void deal(Player player) {
		explosionImmune.start();
		if (player.equals(getPlayer())) {
			Random random = new Random();
			if (random.nextBoolean()) {
				target.setHealth(target.getHealth() / 3);
				target.getWorld().createExplosion(target.getLocation(), 1.0F); 	
				player.sendMessage("��3[��b!��3] ��c���� �����f�� ���Ͽ� ��Ƴ��ҽ��ϴ�.");
			} else {
				player.setHealth(player.getHealth() / 3);
				player.getWorld().createExplosion(player.getLocation(), 1.0F); 	
			}
		} else {
			player.setHealth(player.getHealth() / 3);
			player.getWorld().createExplosion(player.getLocation(), 1.0F); 	
		}
		if (fateMap.containsKey(target)) fateMap.get(target).stop(false);
	}
	
    private final AbilityTimer explosionImmune = new AbilityTimer(2) {
    	
    	@Override
		public void run(int count) {
    		
    	}
    	
    }.setPeriod(TimeUnit.TICKS, 1).register();
	
    private final AbilityTimer passive = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
    		for (Participant participant : getGame().getParticipants()) {
    			if (predicate.test(participant.getPlayer())) {
    				Player player = participant.getPlayer();
    				if (!winornot.containsKey(player)) {
    					Random random = new Random();
    					boolean win = random.nextBoolean();
    					winornot.put(player, win);
    					if (gambled.contains(player)) {
    						new FateLook(player, win).start();
    					}
    				}
    			}
    		}
    	}
    	
    }.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final Duration timer = new Duration(26, cool) {
		
		private int music;
		private List<Player> players = new ArrayList<>();

		@Override
        protected void onDurationStart() {
        	players.add(getPlayer());
        	players.add(target);
        	music = 1;
        }
		
		@Override
		protected void onDurationProcess(int count) {
			switch(music) {
			case 1:
			case 9:
				SoundLib.PIANO.playInstrument(players, Note.natural(1, Note.Tone.B));
				break;
			case 2:
				SoundLib.PIANO.playInstrument(players, Note.sharp(1, Note.Tone.C));
				break;
			case 3:
				SoundLib.PIANO.playInstrument(players, Note.natural(1, Note.Tone.D));
				break;
			case 7:
				SoundLib.PIANO.playInstrument(players, Note.sharp(1, Note.Tone.G));
				break;
			case 8:
			case 15:
				SoundLib.PIANO.playInstrument(players, Note.natural(1, Note.Tone.A));
				break;
			case 13:
			case 21:
				SoundLib.PIANO.playInstrument(players, Note.natural(0, Note.Tone.E));
				break;
			case 17:
			case 19:
			case 23:
			case 25:
				SoundLib.PIANO.playInstrument(players, Note.natural(1, Note.Tone.E));
				break;
			}
			music++;
		}
		
		@Override
		protected void onDurationEnd() {
			onDurationSilentEnd();
		}
		
		@Override
		protected void onDurationSilentEnd() {
			if (winornot.containsKey(target)) {
				if (winornot.get(target)) {
					deal(getPlayer());
				} else {
					deal(target);
				}
				winornot.remove(target);
			} else {
				Random random = new Random();
				if (random.nextBoolean()) {
					deal(getPlayer());
				} else {
					deal(target);
				}
			}
			players.clear();
			gambled.add(target);
		}
		
		
	}.setPeriod(TimeUnit.TICKS, 4);
	
	@SubscribeEvent
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity().equals(getPlayer()) || e.getEntity().equals(target)) {
			if (explosionImmune.isRunning()) {
				if (e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
		onEntityDamage(e);
	}
	
	@SubscribeEvent
	public void onDamage(EntityDamageByEntityEvent e) {
		onEntityDamage(e);
		if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player) {
			if (getPlayer().getInventory().getItemInMainHand() != null) {
				if (getPlayer().getInventory().getItemInMainHand().getType().equals(Material.DIAMOND)) {
					if (!timer.isDuration() && !cool.isCooldown()) {
						if (!e.isCancelled()) {
							target = (Player) e.getEntity();
							timer.start();	
						}
					}
				}
			}
		}
	}
	
	private class FateLook extends AbilityTimer {
		
		private final Player player;
		private final IHologram hologram;
		
		private FateLook(Player player, Boolean win) {
			setPeriod(TimeUnit.TICKS, 4);
			this.player = player;
			this.hologram = NMS.newHologram(player.getWorld(), player.getLocation().getX(),
					player.getLocation().getY() + player.getEyeHeight() + 0.6, player.getLocation().getZ());
			if (win) {
				hologram.setText("��c���� �й�");
			} else {
				hologram.setText("��b���� �¸�");
			}
			hologram.display(getPlayer());
			Sharper.this.fateMap.put(player, this);
		}
		
		@Override
		protected void run(int count) {
			hologram.teleport(player.getWorld(), player.getLocation().getX(), 
					player.getLocation().getY() + player.getEyeHeight() + 0.6, player.getLocation().getZ(), 
					player.getLocation().getYaw(), 0);
		}
		
		@Override
		protected void onEnd() {
			onSilentEnd();
		}
		
		@Override
		protected void onSilentEnd() {
			hologram.unregister();
		}
		
	}
	
}
