package com.bindothorpe.champions.config.skill;

import com.bindothorpe.champions.config.CustomConfig;

public class SkillConfig extends CustomConfig {

    public SkillConfig() {
        super("skill_config");
    }

    @Override
    public void setup() {
        super.setup();

        // Add default values here
        // Assassin
        // Passive
        getFile().addDefault("skills.assassin.passive.move_speed_mod", 0.2);
        // BackStab damage mod
        getFile().addDefault("skills.assassin.back_stab.max_level", 1);
        getFile().addDefault("skills.assassin.back_stab.level_up_cost", 2);
        getFile().addDefault("skills.assassin.back_stab.damage_mod", 4.0);
        // Evade
        getFile().addDefault("skills.assassin.evade.max_level", 1);
        getFile().addDefault("skills.assassin.evade.level_up_cost", 2);
        getFile().addDefault("skills.assassin.evade.base_cooldown", 10.0);
        getFile().addDefault("skills.assassin.evade.cooldown_reduction_per_level", 1.0);
        getFile().addDefault("skills.assassin.evade.cooldown_on_success", 1.0);
        getFile().addDefault("skills.assassin.evade.active_duration", 1.0);
        // Leap
        getFile().addDefault("skills.assassin.leap.max_level", 3);
        getFile().addDefault("skills.assassin.leap.level_up_cost", 1);
        getFile().addDefault("skills.assassin.leap.base_cooldown", 5.0);
        getFile().addDefault("skills.assassin.leap.cooldown_reduction_per_level", 1.0);
        getFile().addDefault("skills.assassin.leap.base_leap_strength", 1.0);
        getFile().addDefault("skills.assassin.leap.leap_strength_increase_per_level", 0.2);
        getFile().addDefault("skills.assassin.leap.base_wall_kick_strength", 0.7);
        getFile().addDefault("skills.assassin.leap.wall_kick_strength_increase_per_level", 0.2);
        // Smoke Arrow
        getFile().addDefault("skills.assassin.smoke_arrow.max_level", 5);
        getFile().addDefault("skills.assassin.smoke_arrow.level_up_cost", 1);
        getFile().addDefault("skills.assassin.smoke_arrow.base_cooldown", 20.0);
        getFile().addDefault("skills.assassin.smoke_arrow.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.assassin.smoke_arrow.base_blind_duration", 2.0);
        getFile().addDefault("skills.assassin.smoke_arrow.blind_duration_increase_per_level", 0.2);

        // Brute
        // Explosive Bomb
        getFile().addDefault("skills.brute.explosive_bomb.max_level", 3);
        getFile().addDefault("skills.brute.explosive_bomb.level_up_cost", 1);
        getFile().addDefault("skills.brute.explosive_bomb.base_cooldown", 5.0);
        getFile().addDefault("skills.brute.explosive_bomb.cooldown_reduction_per_level", 0.5);
        getFile().addDefault("skills.brute.explosive_bomb.base_damage", 2.0);
        getFile().addDefault("skills.brute.explosive_bomb.damage_increase_per_level", 1.0);
        // Flesh Hook
        getFile().addDefault("skills.brute.flesh_hook.max_level", 5);
        getFile().addDefault("skills.brute.flesh_hook.level_up_cost", 1);
        getFile().addDefault("skills.brute.flesh_hook.base_cooldown", 14.0);
        getFile().addDefault("skills.brute.flesh_hook.cooldown_reduction_per_level", 1.0);
        getFile().addDefault("skills.brute.flesh_hook.base_max_charge", 45);
        getFile().addDefault("skills.brute.flesh_hook.max_charge_reduction_per_level", 5);
        getFile().addDefault("skills.brute.flesh_hook.base_max_charge_duration", 5.0);
        getFile().addDefault("skills.brute.flesh_hook.max_charge_duration_increase_per_level", 0.0);
        getFile().addDefault("skills.brute.flesh_hook.base_damage", 2.0);
        getFile().addDefault("skills.brute.flesh_hook.damage_increase_per_level", 1.0);
        getFile().addDefault("skills.brute.flesh_hook.base_launch_strength", 1.5);
        getFile().addDefault("skills.brute.flesh_hook.launch_strength_increase_per_level", 0.3);
        getFile().addDefault("skills.brute.flesh_hook.base_pull_strength", 1.5);
        getFile().addDefault("skills.brute.flesh_hook.pull_strength_increase_per_level", 0.3);
        // Grand Entrance
        getFile().addDefault("skills.brute.grand_entrance.max_level", 3);
        getFile().addDefault("skills.brute.grand_entrance.level_up_cost", 1);
        getFile().addDefault("skills.brute.grand_entrance.base_cooldown", 10.0);
        getFile().addDefault("skills.brute.grand_entrance.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.brute.grand_entrance.base_damage", 1.0);
        getFile().addDefault("skills.brute.grand_entrance.damage_increase_per_level", 0.5);
        getFile().addDefault("skills.brute.grand_entrance.base_launch_strength", 1.5);
        getFile().addDefault("skills.brute.grand_entrance.launch_strength_increase_per_level", 0.3);
        getFile().addDefault("skills.brute.grand_entrance.base_launch_down_strength", 4.0);
        getFile().addDefault("skills.brute.grand_entrance.launch_down_strength_increase_per_level", 0.5);
        getFile().addDefault("skills.brute.grand_entrance.base_knock_up_radius", 3.0);
        getFile().addDefault("skills.brute.grand_entrance.knock_up_radius_increase_per_level", 0.5);
        getFile().addDefault("skills.brute.grand_entrance.base_knock_up_strength", 1.0);
        getFile().addDefault("skills.brute.grand_entrance.knock_up_strength_increase_per_level", 0.5);
        // Head Butt
        getFile().addDefault("skills.brute.head_butt.max_level", 3);
        getFile().addDefault("skills.brute.head_butt.level_up_cost", 1);
        getFile().addDefault("skills.brute.head_butt.base_cooldown", 10.0);
        getFile().addDefault("skills.brute.head_butt.cooldown_reduction_per_level", 2.5);
        getFile().addDefault("skills.brute.head_butt.collision_radius", 0.5);
        getFile().addDefault("skills.brute.head_butt.base_damage", 1.0);
        getFile().addDefault("skills.brute.head_butt.damage_increase_per_level", 1.0);
        getFile().addDefault("skills.brute.head_butt.base_wall_impact_damage", 2.0);
        getFile().addDefault("skills.brute.head_butt.wall_impact_damage_increase_per_level", 3.0);
        getFile().addDefault("skills.brute.head_butt.base_launch_strength", 1.5);
        getFile().addDefault("skills.brute.head_butt.launch_strength_increase_per_level", 0.3);
        getFile().addDefault("skills.brute.head_butt.base_impact_launch_strength", 1.5);
        getFile().addDefault("skills.brute.head_butt.impact_launch_strength_increase_per_level", 0.5);
        // Trample
        getFile().addDefault("skills.brute.trample.max_level", 3);
        getFile().addDefault("skills.brute.trample.level_up_cost", 1);
        getFile().addDefault("skills.brute.trample.base_cooldown", 12.0);
        getFile().addDefault("skills.brute.trample.cooldown_reduction_per_level", 4.0);
        getFile().addDefault("skills.brute.trample.base_trample_count", 8);
        getFile().addDefault("skills.brute.trample.trample_count_increase_per_level", 2);
        getFile().addDefault("skills.brute.trample.base_trample_radius", 3.0);
        getFile().addDefault("skills.brute.trample.trample_radius_increase_per_level", 0.5);
        getFile().addDefault("skills.brute.trample.trample_delay_in_milliseconds", 500);
        getFile().addDefault("skills.brute.head_butt.base_damage", 0.5);
        getFile().addDefault("skills.brute.head_butt.damage_increase_per_level", 0.5);

        // Knight
        // Bulls Charge
        getFile().addDefault("skills.knight.bulls_charge.max_level", 4);
        getFile().addDefault("skills.knight.bulls_charge.level_up_cost", 1);
        getFile().addDefault("skills.knight.bulls_charge.base_cooldown", 10.0);
        getFile().addDefault("skills.knight.bulls_charge.cooldown_reduction_per_level", 1.0);
        getFile().addDefault("skills.knight.bulls_charge.base_damage", 2.0);
        getFile().addDefault("skills.knight.bulls_charge.damage_increase_per_level", 1.0);
        getFile().addDefault("skills.knight.bulls_charge.base_active_duration", 3.0);
        getFile().addDefault("skills.knight.bulls_charge.active_duration_increase_per_level", 1.0);
        getFile().addDefault("skills.knight.bulls_charge.move_speed_effect", 2);
        getFile().addDefault("skills.knight.bulls_charge.slow_effect", 1);
        getFile().addDefault("skills.knight.bulls_charge.base_slow_effect_duration", 2.0);
        getFile().addDefault("skills.knight.bulls_charge.slow_effect_duration_increase_per_level", 0.5);
        // Heavy Swing
        getFile().addDefault("skills.knight.heavy_swing.max_level", 3);
        getFile().addDefault("skills.knight.heavy_swing.level_up_cost", 1);
        getFile().addDefault("skills.knight.heavy_swing.base_cooldown", 5.0);
        getFile().addDefault("skills.knight.heavy_swing.cooldown_reduction_per_level", 1.0);
        getFile().addDefault("skills.knight.heavy_swing.base_max_charge", 40);
        getFile().addDefault("skills.knight.heavy_swing.max_charge_reduction_per_level", 5);
        getFile().addDefault("skills.knight.heavy_swing.base_max_charge_duration", 5.0);
        getFile().addDefault("skills.knight.heavy_swing.max_charge_duration_increase_per_level", 0.0);
        getFile().addDefault("skills.knight.heavy_swing.base_damage", 2.0);
        getFile().addDefault("skills.knight.heavy_swing.damage_increase_per_level", 1.0);
        getFile().addDefault("skills.knight.heavy_swing.base_min_cone_angle", 45.0);
        getFile().addDefault("skills.knight.heavy_swing.min_cone_angle_increase_per_level", 0.0);
        getFile().addDefault("skills.knight.heavy_swing.base_max_cone_angle", 90.0);
        getFile().addDefault("skills.knight.heavy_swing.max_cone_angle_increase_per_level", 0.0);
        getFile().addDefault("skills.knight.heavy_swing.base_min_range", 2.0);
        getFile().addDefault("skills.knight.heavy_swing.min_range_increase_per_level", 1.0);
        getFile().addDefault("skills.knight.heavy_swing.base_max_range", 5.0);
        getFile().addDefault("skills.knight.heavy_swing.max_range_increase_per_level", 1.0);
        // Riposte
        getFile().addDefault("skills.knight.riposte.max_level", 5);
        getFile().addDefault("skills.knight.riposte.level_up_cost", 1);
        getFile().addDefault("skills.knight.riposte.base_cooldown", 14.0);
        getFile().addDefault("skills.knight.riposte.cooldown_reduction_per_level", 1.0);
        getFile().addDefault("skills.knight.riposte.base_damage", 2.0);
        getFile().addDefault("skills.knight.riposte.damage_increase_per_level", 0.5);
        getFile().addDefault("skills.knight.riposte.block_window_duration", 1.0);
        getFile().addDefault("skills.knight.riposte.buff_duration", 1.0);

        // Mage
        // Passive
        getFile().addDefault("skills.mage.passive.cooldown_reduction", 0.1);
        // Blizzard
        getFile().addDefault("skills.mage.blizzard.max_level", 3);
        getFile().addDefault("skills.mage.blizzard.level_up_cost", 1);
        getFile().addDefault("skills.mage.blizzard.base_cooldown", 10.0);
        getFile().addDefault("skills.mage.blizzard.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.mage.blizzard.base_max_charge_duration", 2.5);
        getFile().addDefault("skills.mage.blizzard.max_charge_duration_increase_per_level", 0.5);
        getFile().addDefault("skills.mage.blizzard.base_launch_strength", 2.0);
        getFile().addDefault("skills.mage.blizzard.launch_strength_increase_per_level", 0.5);
        getFile().addDefault("skills.mage.blizzard.base_impact_launch_strength_modifier", 0.05);
        getFile().addDefault("skills.mage.blizzard.impact_launch_strength_modifier_increase_per_level", 0.01);
        // Inferno
        getFile().addDefault("skills.mage.inferno.max_level", 3);
        getFile().addDefault("skills.mage.inferno.level_up_cost", 1);
        getFile().addDefault("skills.mage.inferno.base_cooldown", 10.0);
        getFile().addDefault("skills.mage.inferno.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.mage.inferno.base_max_charge_duration", 3.0);
        getFile().addDefault("skills.mage.inferno.max_charge_duration_increase_per_level", 0.5);
        getFile().addDefault("skills.mage.inferno.base_flame_damage", 1.0);
        getFile().addDefault("skills.mage.inferno.flame_damage_increase_per_level", 0.5);
        getFile().addDefault("skills.mage.inferno.base_launch_strength", 1.6);
        getFile().addDefault("skills.mage.inferno.launch_strength_increase_per_level", 0.2);
        // Explosion
        getFile().addDefault("skills.mage.explosion.max_level", 3);
        getFile().addDefault("skills.mage.explosion.level_up_cost", 1);
        getFile().addDefault("skills.mage.explosion.base_cooldown", 10.0);
        getFile().addDefault("skills.mage.explosion.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.mage.explosion.base_damage", 7.0);
        getFile().addDefault("skills.mage.explosion.damage_increase_per_level", 0.5);
        getFile().addDefault("skills.mage.explosion.base_travel_speed", 30.0);
        getFile().addDefault("skills.mage.explosion.travel_speed_increase_per_level", 10.0);
        getFile().addDefault("skills.mage.explosion.base_collision_radius", 2.5);
        getFile().addDefault("skills.mage.explosion.collision_radius_increase_per_level", 0.3);
        // Ice Prison
        getFile().addDefault("skills.mage.ice_prison.max_level", 3);
        getFile().addDefault("skills.mage.ice_prison.level_up_cost", 1);
        getFile().addDefault("skills.mage.ice_prison.base_cooldown", 13.0);
        getFile().addDefault("skills.mage.ice_prison.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.mage.ice_prison.base_duration", 3.0);
        getFile().addDefault("skills.mage.ice_prison.duration_increase_per_level", 1.0);
        getFile().addDefault("skills.mage.ice_prison.base_launch_strength", 1.5);
        getFile().addDefault("skills.mage.ice_prison.launch_strength_increase_per_level", 0.2);
        getFile().addDefault("skills.mage.ice_prison.radius", 5);
        // Immolate
        getFile().addDefault("skills.mage.immolate.max_level", 3);
        getFile().addDefault("skills.mage.immolate.level_up_cost", 1);
        getFile().addDefault("skills.mage.immolate.base_cooldown", 15.0);
        getFile().addDefault("skills.mage.immolate.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.mage.immolate.base_active_duration", 4.0);
        getFile().addDefault("skills.mage.immolate.active_duration_increase_per_level", 1.0);
        getFile().addDefault("skills.mage.immolate.damage_done_mod", 1.0);
        getFile().addDefault("skills.mage.immolate.damage_received_mod", 1.0);
        getFile().addDefault("skills.mage.immolate.move_speed_mod", 0.2);
        getFile().addDefault("skills.mage.immolate.base_flame_damage", 1.0);
        getFile().addDefault("skills.mage.immolate.flame_damage_increase_per_level", 0.5);
        // Void
        getFile().addDefault("skills.mage.void.max_level", 3);
        getFile().addDefault("skills.mage.void.level_up_cost", 1);
        getFile().addDefault("skills.mage.void.base_cooldown", 15.0);
        getFile().addDefault("skills.mage.void.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.mage.void.base_active_duration", 4.0);
        getFile().addDefault("skills.mage.void.active_duration_increase_per_level", 1.0);
        getFile().addDefault("skills.mage.void.base_duration_reduction_on_hit", 1.0);
        getFile().addDefault("skills.mage.void.duration_reduction_on_hit_reduction_per_level", 0.25);
        getFile().addDefault("skills.mage.void.damage_received_mod", -1.0);
        getFile().addDefault("skills.mage.void.slow_mod", 1);
        // Heavy Swing
        getFile().addDefault("skills.mage.static_lazer.max_level", 5);
        getFile().addDefault("skills.mage.static_lazer.level_up_cost", 1);
        getFile().addDefault("skills.mage.static_lazer.base_cooldown", 5.0);
        getFile().addDefault("skills.mage.static_lazer.cooldown_reduction_per_level", 1.0);
        getFile().addDefault("skills.mage.static_lazer.base_max_charge", 40);
        getFile().addDefault("skills.mage.static_lazer.max_charge_reduction_per_level", 5);
        getFile().addDefault("skills.mage.static_lazer.base_max_charge_duration", 3.0);
        getFile().addDefault("skills.mage.static_lazer.max_charge_duration_increase_per_level", 0.0);
        getFile().addDefault("skills.mage.static_lazer.base_damage", 6.0);
        getFile().addDefault("skills.mage.static_lazer.damage_increase_per_level", 2.0);
        getFile().addDefault("skills.mage.static_lazer.base_distance", 30.0);
        getFile().addDefault("skills.mage.static_lazer.distance_increase_per_level", 10.0);
        getFile().addDefault("skills.mage.static_lazer.detection_radius", 0.3);
        getFile().addDefault("skills.mage.static_lazer.detection_density_per_block", 2.0);

        // Ranger
        // Bouncing Arrow
        getFile().addDefault("skills.ranger.bouncing_arrow.max_level", 3);
        getFile().addDefault("skills.ranger.bouncing_arrow.level_up_cost", 1);
        getFile().addDefault("skills.ranger.bouncing_arrow.base_bounce_count", 1);
        getFile().addDefault("skills.ranger.bouncing_arrow.bounce_count_increase_per_level", 1);
        getFile().addDefault("skills.ranger.bouncing_arrow.base_bounce_distance", 5.0);
        getFile().addDefault("skills.ranger.bouncing_arrow.bounce_distance_increase_per_level", 3.0);
        // Hunters Heart
        getFile().addDefault("skills.ranger.hunters_heart.max_level", 3);
        getFile().addDefault("skills.ranger.hunters_heart.level_up_cost", 1);
        getFile().addDefault("skills.ranger.hunters_heart.heal_amount", 1.0);
        getFile().addDefault("skills.ranger.hunters_heart.base_duration_before_heal", 12.0);
        getFile().addDefault("skills.ranger.hunters_heart.duration_before_heal_decrease_per_level", 2.0);
        getFile().addDefault("skills.ranger.hunters_heart.base_interval_between_heal", 2.0);
        getFile().addDefault("skills.ranger.hunters_heart.interval_between_heal_decrease_per_level", 0.5);
        // Kiting Arrow
        getFile().addDefault("skills.ranger.kiting_arrow.max_level", 3);
        getFile().addDefault("skills.ranger.kiting_arrow.level_up_cost", 1);
        getFile().addDefault("skills.ranger.kiting_arrow.base_speed_duration", 0.5);
        getFile().addDefault("skills.ranger.kiting_arrow.speed_duration_increase_per_level", 0.5);
        getFile().addDefault("skills.ranger.kiting_arrow.base_move_speed_mod", 0.05);
        getFile().addDefault("skills.ranger.kiting_arrow.move_speed_mod_increase_per_level", 0.05);
        // Magnetic Pull
        getFile().addDefault("skills.ranger.magnetic_pull.max_level", 3);
        getFile().addDefault("skills.ranger.magnetic_pull.level_up_cost", 1);
        getFile().addDefault("skills.ranger.magnetic_pull.base_cooldown", 10.0);
        getFile().addDefault("skills.ranger.magnetic_pull.cooldown_reduction_per_level", 2.5);
        getFile().addDefault("skills.ranger.magnetic_pull.arrow_speed_mult", 2);
        // Silk Arrow
        getFile().addDefault("skills.ranger.silk_arrow.max_level", 1);
        getFile().addDefault("skills.ranger.silk_arrow.level_up_cost", 2);
        getFile().addDefault("skills.ranger.silk_arrow.base_cooldown", 10.0);
        getFile().addDefault("skills.ranger.silk_arrow.cooldown_reduction_per_level", 0.0);
        getFile().addDefault("skills.ranger.silk_arrow.duration", 10.0);
        // Sonar Arrow
        getFile().addDefault("skills.ranger.sonar_arrow.max_level", 1);
        getFile().addDefault("skills.ranger.sonar_arrow.level_up_cost", 2);
        getFile().addDefault("skills.ranger.sonar_arrow.base_cooldown", 10.0);
        getFile().addDefault("skills.ranger.sonar_arrow.cooldown_reduction_per_level", 0.0);
        getFile().addDefault("skills.ranger.sonar_arrow.detection_radius", 10.0);
        getFile().addDefault("skills.ranger.sonar_arrow.bounce_strength_mult", 0.8);
        // Wolf's Pounce
        getFile().addDefault("skills.ranger.wolfs_pounce.max_level", 5);
        getFile().addDefault("skills.ranger.wolfs_pounce.level_up_cost", 1);
        getFile().addDefault("skills.ranger.wolfs_pounce.base_cooldown", 12.0);
        getFile().addDefault("skills.ranger.wolfs_pounce.cooldown_reduction_per_level", 2.0);
        getFile().addDefault("skills.ranger.wolfs_pounce.base_max_charge", 40);
        getFile().addDefault("skills.ranger.wolfs_pounce.max_charge_reduction_per_level", 5);
        getFile().addDefault("skills.ranger.wolfs_pounce.base_max_charge_duration", 5.0);
        getFile().addDefault("skills.ranger.wolfs_pounce.max_charge_duration_increase_per_level", 0.0);
        getFile().addDefault("skills.ranger.wolfs_pounce.base_damage", 3.0);
        getFile().addDefault("skills.ranger.wolfs_pounce.damage_increase_per_level", 1.0);
        getFile().addDefault("skills.ranger.wolfs_pounce.base_launch_strength", 0.4);
        getFile().addDefault("skills.ranger.wolfs_pounce.launch_strength_increase_per_level", 0.05);



        // Make sure defaults are copied to the file
        getFile().options().copyDefaults(true);
        saveFile();
    }
}