package com.project.seniorpal.skill;

import java.util.*;

/**
 * The registry for all Skills. Each application should register Skills it provides.
 * Skill from other applications will be synced after calling refreshServiceSkills. To register skills from other application is not allowed.
 */
public class SkillRegistry {

    public static final SkillRegistry localRegistry = new SkillRegistry();

    private final Map<String, Skill> idToSkill;

    private final Map<String, Set<Skill>> descToSkill;

    public Skill getSkillById(String id) {
        return idToSkill.get(id);
    }

    public Skill findMostLikelySkillByDesc(String desc, Map<String, String> args) {
        Set<Skill> skillsWithDesc = descToSkill.get(desc);
        if (skillsWithDesc == null || skillsWithDesc.isEmpty()) {
            return null;
        }
        Set<String> argsKeys = args.keySet();
        for (Skill skill : skillsWithDesc) {
            if (skill.argsDesc.keySet().equals(argsKeys)) {
                return skill;
            }
        }
        return null;
    }

    public void registerSkill(Skill skill) {
        {
            idToSkill.put(skill.id, skill);
            Set<Skill> skillsInSameDesc = descToSkill.computeIfAbsent(skill.desc, k -> new HashSet<>());
            skillsInSameDesc.add(skill);
        }
    }

    public Map<String, Skill> getAllSkillsIdToSkill() {
        return Collections.unmodifiableMap(idToSkill);
    }

    public SkillRegistry() {
        idToSkill = new HashMap<>();
        descToSkill = new HashMap<>();
    }

    protected SkillRegistry(Map<String, Skill> idToSkill, Map<String, Set<Skill>> descToSkill) {
        this.idToSkill = idToSkill;
        this.descToSkill = descToSkill;
    }
}
