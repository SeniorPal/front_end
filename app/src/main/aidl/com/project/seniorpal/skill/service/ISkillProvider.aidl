// ISkillProvider.aidl
package com.project.seniorpal.skill.service;

// Declare any non-default types here with import statements

import com.project.seniorpal.skill.service.util.SkillDataWrapper;
import com.project.seniorpal.skill.Skill;

import java.util.List;
import java.util.Map;

interface ISkillProvider {

    List<SkillDataWrapper> getAllSkills();

    Map<String, String> activeSkill(in SkillDataWrapper skillData, in Skill.ActivatorType activatorType);

}