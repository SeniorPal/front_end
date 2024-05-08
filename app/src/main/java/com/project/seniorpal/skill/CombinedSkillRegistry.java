package com.project.seniorpal.skill;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CombinedSkillRegistry extends SkillRegistry {

    private final SkillRegistry[] combinedRegistries;

    public CombinedSkillRegistry(SkillRegistry... combinedRegistries) {
        super(null, null);
        this.combinedRegistries = combinedRegistries.clone();
    }

    @Override
    public Skill getSkillById(String id) {
        for (SkillRegistry registry : combinedRegistries) {
            Skill skillFound = registry.getSkillById(id);
            if (skillFound != null) {
                return skillFound;
            }
        }
        return null;
    }

    @Override
    public void registerSkill(Skill skill) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Skill> getAllSkillsIdToSkill() {
        Map<String, Skill> allSkills = new HashMap<>();
        for (SkillRegistry one : combinedRegistries) {
            allSkills.putAll(one.getAllSkillsIdToSkill());
        }
        return allSkills;
    }
}
