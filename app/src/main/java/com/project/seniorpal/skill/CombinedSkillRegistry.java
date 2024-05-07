package com.project.seniorpal.skill;

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
    public Skill findMostLikelySkillByDesc(String desc, Map<String, String> args) {
        for (SkillRegistry registry : combinedRegistries) {
            Skill skillFound = registry.findMostLikelySkillByDesc(desc, args);
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
}
