package RainStarAbility;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import RainStarEffect.Confusion;
import daybreak.abilitywar.AbilityWar;
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
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.list.mix.Mix;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.effect.Bleed;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.Wreck;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.base.Predicate;
import daybreak.google.common.collect.ImmutableSet;

@AbilityManifest(name = "대시", rank = Rank.L, species = Species.HUMAN, explain = {
		"§7패시브 §8- §b스태미나§f: 스태미나를 회복하여 총 5초에 1 게이지가 찹니다.",
		" 스태미나는 전투 도중에는 더 적게 차오르고, 더 많이 소모합니다.",
		" 화면을 전환하지도 않고 움직이지도 않고 있다면 더 빨리 차오릅니다.",
		"§7검 들고 F키 §8- §3대시§f: 바라보는 방향의 수평으로 짧게 대시합니다. §c소모 §7: §b2",
		" 대시로 이동한 거리에 대시 잔상이 남아 닿은 적에게 피해를 입힙니다.",
		" 대시 잔상에 누군가가 맞을 때마다 스태미나를 회복합니다. §d회복 §7: §b0.2",
		"§7공격 후 대시 §8- §e광속§f: 근접 공격 후 0.15초 내에 대시할 경우 대상에게 2초간",
		" §c출혈§f 및 무작위 방향으로 튕겨나가는 §6혼란§f 효과를 부여합니다. §d회복 §7: §b0.5",
		"§8[§7HIDDEN§8] §b속도 경쟁§f: 과연 누가 더 빠를려나?"},
		summarize = {
		"§7검을 들고 F키§f를 누를 시 스태미나를 2 소모해 바라보는 방향으로 §b대시§f합니다.",
		"§b대시§f로 지나간 자리에 잔상이 남아 마법 피해를 입힙니다.",
		"타격 후 §b대시§f할 경우 스태미나 0.5를 회복하고 대상은 §6혼란§f / §c출혈§f 상태가 됩니다."
		})

@Tips(tip = {
        "기동의 다양성보다는 단순 속도를 살린 기동성 캐릭터입니다.",
        "대시하는 그 순간의 무적 및 타게팅 불능은, 충분한 공간만 지원된다면",
        "누구보다 빠르게 위기 상황에서 탈출할 수 있습니다. 또한 광속 효과를",
        "이용하여 다른 플레이어의 기동력을 억제시키고, 자신은 신속 버프로",
        "대시하지 않을 때도 더 빠르게 이동할 수 있습니다.",
        "다만 상하의 방향으로는 이동하지 못해, 오직 전방으로만 대시할 수 있어",
        "지형지물로 우위를 점하기는 어렵습니다."
}, strong = {
        @Description(subject = "기절 외 기동력 억제 능력", explain = {
                "카오스 등 대상을 끌어당기는 기동력 억제 능력은",
                "대시의 타게팅 불능에 의해 간단히 파훼될 수 있습니다."
        }),
        @Description(subject = "기동력이 낮은 상대", explain = {
                "대상의 기동력이 낮을수록 광속의 혼란과 출혈,",
                "그리고 대시의 압도적인 속도로 우위를 점하기 쉽습니다."
        })
}, weak = {
        @Description(subject = "기절", explain = {
                "기절은 대시로도 파훼할 수 없습니다. 속도가 생명인",
                "대시에게는 치명적입니다."
        }),
        @Description(subject = "좁은 공간", explain = {
                "대시는 단순히 재빠른 속도로 질주하는 개념이기에,",
                "블록을 통과할 수 없어 벽에 가로막힐 수 있습니다."
        })
}, stats = @Stats(offense = Level.THREE, survival = Level.SIX, crowdControl = Level.FOUR, mobility = Level.NINE, utility = Level.TWO), difficulty = Difficulty.EASY)

public class Dash extends AbilityBase {

	public Dash(Participant participant) {
		super(participant);
	}
	
	private static final Set<Material> swords;
	private double stamina = 0;
	private BossBar bossBar = null;
	private ActionbarChannel ac = newActionbarChannel();
	
	private int movepoint = 0;
	
	private Location startLocation;
	private Participant target;
	private boolean onetime = true;
	private static final Vector zerov = new Vector(0, 0, 0);
	private int timer = (int) Math.ceil(Wreck.isEnabled(GameManager.getGame()) ? Wreck.calculateDecreasedAmount(75) * 5 : 5);
	private PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 200, 2, true, false);
	private PotionEffect normalspeed = new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false);
	private PotionEffect invisible = new PotionEffect(PotionEffectType.INVISIBILITY, 3, 0, true, false);
	private ItemStack[] armors;
	
	static {
		if (MaterialX.NETHERITE_SWORD.isSupported()) {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
		} else {
			swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
		}
	}
	
	protected void onUpdate(Update update) {
	    if (update == Update.RESTRICTION_CLEAR) {
	    	staminaupdater.start();
	    	nomove.start();
	    } 
	}
	
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
	
	private final AbilityTimer attacked = new AbilityTimer(3) {
		
		@Override
		public void run(int count) {
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer inbattle = new AbilityTimer(50) {
		
		@Override
		public void onStart() {
			ac.update("§a전투 중");
		}
		
		@Override
		public void run(int count) {
			ac.update("§a전투 중");
		}
		
		@Override
		public void onEnd() {
			onSilentEnd();
		}
		
		@Override
		public void onSilentEnd() {
			ac.update(null);
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer nomove = new AbilityTimer() {
		
		@Override
		public void run(int count) {
			movepoint++;
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	private final AbilityTimer staminaupdater = new AbilityTimer() {
		
    	@Override
    	public void onStart() {
    		bossBar = Bukkit.createBossBar("스태미나", BarColor.BLUE, BarStyle.SEGMENTED_10);
    		bossBar.setProgress(stamina * 0.1);
    		bossBar.addPlayer(getPlayer());
    		if (ServerVersion.getVersion() >= 10) bossBar.setVisible(true);
    	}
    	
    	@Override
		public void run(int count) {
    		if (timer == 0) {
    			staminaGain(0.5);
    			bossBar.setProgress(stamina * 0.1);
    		} else {
    			staminaGain((double) 1 / (timer * 20));
    			bossBar.setProgress(stamina * 0.1);	
    		}
    	}
    	
		@Override
		public void onEnd() {
			bossBar.removeAll();
		}

		@Override
		public void onSilentEnd() {
			bossBar.removeAll();
		}
		
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
	public void staminaUse(double value) {
		if (!inbattle.isRunning()) {
			stamina = Math.max(0, stamina - value);
		} else {
			stamina = Math.max(0, stamina - (value * 1.25));	
		}
	}
	
	public void staminaGain(double value) {
		if (!inbattle.isRunning()) {
			if (movepoint > 1) {
				stamina = Math.min(10, stamina + (value * 1.25));	
			} else {
				stamina = Math.min(10, stamina + value);
			}
		} else {
			stamina = Math.min(10, stamina + (value * 0.75));
		}
	}
	
	public void staminaTrueGain(double value) {
		stamina = Math.min(10, stamina + value);
	}
	
	private final AbilityTimer dashing = new AbilityTimer(1) {
		
		@Override
		public void onStart() {
	    	getPlayer().addPotionEffect(invisible);
	    	getPlayer().setVelocity(VectorUtil.validateVector(getPlayer().getLocation().getDirection().normalize().multiply(10).setY(0)));
	    	getPlayer().getInventory().setArmorContents(null);
			getParticipant().attributes().TARGETABLE.setValue(false);
			if (target != null) {
		    	if (attacked.isRunning() && !target.hasEffect(Confusion.registration)) {
					if (onetime == true && target.hasAbility()) {
						AbilityBase ab = target.getAbility();
						if (ab.getClass().equals(LightningCounter.class)) {
			    			target.getPlayer().addPotionEffect(speed);
			    			getPlayer().addPotionEffect(speed);
			    			getPlayer().sendMessage("§8[§7HIDDEN§8] §f우연히 고속의 상대를 만나 경쟁을 하여 매우 빨라졌습니다.");
			    			getPlayer().sendMessage("§8[§7HIDDEN§8] §c속도 경쟁§f을 달성하였습니다.");
			    			staminaGain(10);
			    			SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(getPlayer());
			    			onetime = false;
						} else if (ab.getClass().equals(Mix.class)) {
							Mix mix = (Mix) ab;
							if (mix.hasAbility() && !mix.hasSynergy()) {
								if (mix.getFirst().getClass().equals(LightningCounter.class)
										|| mix.getSecond().getClass().equals(LightningCounter.class)) {
						    			target.getPlayer().addPotionEffect(speed);
						    			getPlayer().addPotionEffect(speed);
						    			getPlayer().sendMessage("§8[§7HIDDEN§8] §f우연히 고속의 상대를 만나 경쟁을 하여 매우 빨라졌습니다.");
						    			getPlayer().sendMessage("§8[§7HIDDEN§8] §c속도 경쟁§f을 달성하였습니다.");
						    			staminaGain(10);
						    			SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(getPlayer());
						    			onetime = false;
									} else {
										Bleed.apply(getGame(), target.getPlayer(), TimeUnit.SECONDS, 2);
							    		Confusion.apply(target, TimeUnit.SECONDS, 2, 20);
							    		staminaTrueGain(0.5);
									}
							} else {
								Bleed.apply(getGame(), target.getPlayer(), TimeUnit.SECONDS, 2);
								Confusion.apply(target, TimeUnit.SECONDS, 2, 20);
								staminaTrueGain(0.5);
							}
						} else {
				    		Bleed.apply(getGame(), target.getPlayer(), TimeUnit.SECONDS, 2);
				    		Confusion.apply(target, TimeUnit.SECONDS, 2, 20);
				    		staminaTrueGain(0.5);
			    		}
					} else if (onetime == false || !target.hasAbility()) {
			   			Bleed.apply(getGame(), target.getPlayer(), TimeUnit.SECONDS, 2);
			   			Confusion.apply(target, TimeUnit.SECONDS, 2, 20);
			   			staminaTrueGain(0.5);
					}
		   		}	
			}
	   	}
	 
	   	@Override
		public void run(int count) {
			SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer().getLocation(), 1, 1.5f);
	   	}
	   	
	   	@Override
	   	public void onEnd() {
	   		onSilentEnd();
	   	}
	    	
	   	@Override
	    public void onSilentEnd() {
			getPlayer().setVelocity(zerov);
			getPlayer().getInventory().setArmorContents(armors);
	   		SoundLib.ENTITY_FIREWORK_ROCKET_BLAST.playSound(getPlayer().getLocation());
			getParticipant().attributes().TARGETABLE.setValue(true);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					new AfterImage().start();
				}
				
			}.runTaskLater(AbilityWar.getPlugin(), 1L);
	   	}
	    	
	}.setPeriod(TimeUnit.TICKS, 1).register();
	
    @SubscribeEvent(onlyRelevant = true)
    public void onPlayerMove(PlayerMoveEvent e) {
    	if (movepoint > 0) {
    		movepoint = 0;
    	}
    }
    
	@SubscribeEvent
	private void onPlayerJoin(final PlayerJoinEvent e) {
		if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId()) && staminaupdater.isRunning()) {
			if (bossBar != null) bossBar.addPlayer(e.getPlayer());
		}
	}

	@SubscribeEvent
	private void onPlayerQuit(final PlayerQuitEvent e) {
		if (getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
			if (bossBar != null) bossBar.removePlayer(e.getPlayer());
		}
	}
	
    @SubscribeEvent(onlyRelevant = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
    	if (swords.contains(e.getOffHandItem().getType()) && e.getPlayer().equals(getPlayer())) {
    		if (!dashing.isRunning()) {
    			if (inbattle.isRunning() ? stamina >= (2 * 1.25) : stamina >= 2) {
        	    	startLocation = getPlayer().getLocation();
        	    	armors = getPlayer().getInventory().getArmorContents();
        	    	staminaUse(2);
                	dashing.start();
        		} else {
        			getPlayer().sendMessage("§f[§c!§f] §c스태미나가 부족합니다.");
        		}	
    		}
    		e.setCancelled(true);
    	}
    }
    
    @SubscribeEvent(onlyRelevant = true)
    public void onEntityDamage(EntityDamageEvent e) {
    	if (dashing.isRunning() && e.getEntity().equals(getPlayer())) {
    		e.setCancelled(true);
    	}
    }
    
    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
    	onEntityDamage(e);
    	if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof Player) {
			target = getGame().getParticipant((Player) e.getEntity());
    		if (attacked.isRunning()) attacked.setCount(3);
    		else attacked.start();
    		if (inbattle.isRunning()) inbattle.setCount(50);
    		else inbattle.start();
    	}
    	if (e.getEntity().equals(getPlayer()) && e.getDamager() instanceof Player) {
    		if (inbattle.isRunning()) inbattle.setCount(50);
    		else inbattle.start();
    	}
    }
    
    @SubscribeEvent
    public void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
    	onEntityDamage(e);
    }
    
    public class AfterImage extends AbilityTimer {
    	
    	private Set<Damageable> damagedcheck = new HashSet<>();
    	private Location saveloc1;
    	private Location saveloc2;
    	
    	private AfterImage() {
    		super(TaskType.REVERSE, 60);
    		setPeriod(TimeUnit.TICKS, 1);
    	}
    	
    	@Override
    	protected void onStart() {
	   		for (Location loc : Line.between(startLocation, getPlayer().getLocation(), (int) Math.min(300, (25 * Math.sqrt(startLocation.distance(getPlayer().getLocation()))))).toLocations(startLocation)) {
	   			ParticleLib.END_ROD.spawnParticle(loc.add(0, 1, 0), 0, 0, 0, 1, 0);
	   			saveloc1 = startLocation;
	   			saveloc2 = getPlayer().getLocation();
	   		}
    	}
    	
    	@Override
    	protected void run(int count) {
    		for (Damageable p : LocationUtil.rayTraceEntities(Damageable.class, saveloc1, saveloc2, 0.75, predicate)) {
    			if (!p.equals(getPlayer()) && !damagedcheck.contains(p)) {
    				if (p instanceof Player) {
            			if (getGame().getParticipant((Player) p).hasEffect(Confusion.registration)) {
            				if (count < 50) {
            					getPlayer().addPotionEffect(normalspeed);
                				Damages.damageMagic(p, getPlayer(), true, 1.5f);
                				damagedcheck.add(p);
                				staminaGain(0.2);
            				}
            			} else {
            				Damages.damageMagic(p, getPlayer(), true, 1.5f);
            				damagedcheck.add(p);
            				staminaGain(0.2);
            			}
    				} else {
        				Damages.damageMagic(p, getPlayer(), true, 1.5f);
        				damagedcheck.add(p);
        				staminaGain(0.2);
    				}
    			}
    		}
    	}
    	
    	@Override
    	protected void onEnd() {
    		onSilentEnd();
    	}
    	
    	@Override
    	protected void onSilentEnd() {
    		damagedcheck.clear();
    	}
    	
    }
    
}
