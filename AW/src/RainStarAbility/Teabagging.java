package RainStarAbility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.google.common.base.Predicate;

@AbilityManifest(
		name = "Ƽ���", rank = Rank.C, species = Species.HUMAN, explain = {
		"�������� ���� ���� ���� �� ��ũ���⸦ ��Ÿ�� ��� ���ظ� �ݴϴ�.",
		"���� ��밡 �� �޸���� ���� ���� ���� ���ط��� �� �������ϴ�."
		})

public class Teabagging extends AbilityBase {

	public Teabagging(Participant participant) {
		super(participant);
	}
	
	protected void onUpdate(Update update) {
	    if (update == Update.RESTRICTION_CLEAR) {
	    	passive.start();
	    } 
	}
	
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
	
	private boolean isBehind(Player p, Player target) {
	    Location eye = p.getEyeLocation();
	    Vector toEntity = target.getEyeLocation().toVector().subtract(eye.toVector());
	    double dot = toEntity.normalize().dot(eye.getDirection());
	    return dot < -0.5D;
	}
	
	private Map<Player, Integer> teabaggingcount = new HashMap<>();
	private Set<Player> bagged = new HashSet<>();
	
	private final Predicate<Entity> predicate2 = new Predicate<Entity>() {
		@Override
		public boolean test(Entity entity) {
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
	
    private final AbilityTimer passive = new AbilityTimer() {
    	
    	@Override
		public void run(int count) {
    		for (Participant participant : getGame().getParticipants()) {
    			if (predicate.test(participant.getPlayer())) {
        			Player player = participant.getPlayer();
        			if (getPlayer().equals(LocationUtil.getEntityLookingAt(Player.class, player, 10, predicate2))) {
        				if (getPlayer().isSneaking() && !bagged.contains(player)) {
        					bagged.add(player);
        					if (teabaggingcount.containsKey(player)) {
        						teabaggingcount.put(player, teabaggingcount.get(player) + 1);
        					} else {
        						teabaggingcount.put(player, 1);
        					}
        					if (teabaggingcount.get(player) >= 3) {
        						if (isBehind(getPlayer(), player)) {
            						player.damage(4, getPlayer());	
        						} else {
            						player.damage(2, getPlayer());	
        						}
        						Random random = new Random();
        						if (random.nextInt(10) == 0) {
        							switch(random.nextInt(8)) {
        							case 0: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c���� �� �ù޳�?");
        							break;
        							case 1: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��cez");
        							break;
        							case 2: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c������?");
        							break;
        							case 3: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c��");
        							break;
        							case 4: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c��~");
        							break;
        							case 5: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c���ƾ��? ��¼����~");
        							break;
        							case 6: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��c��������~");
        							break;
        							case 7: player.sendMessage("��e" + getPlayer().getName() + " ��b>��e " + player.getName() + "��7: ��cũũ��滧��~");
        							break;
        							}
        						}
        						teabaggingcount.put(player, 0);
        					}
        				}
        			}
    			}
    		}
    		if (!getPlayer().isSneaking()) {
    			bagged.clear();
    		}
    	}
    
    }.setPeriod(TimeUnit.TICKS, 1).register();
	
}