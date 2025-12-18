package com.bindothorpe.champions.domain.skill.interfaces;

import net.kyori.adventure.text.Component;

import java.util.List;

public interface ISkillDescription {

    List<Component> getDescription(int skillLevel);

}
