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

    public List<String> findNMostLikelySkills(List<Double> embeddings, int n) {
        if (!isEmbeddingsInitialized()) {
            throw new IllegalStateException("Embeddings are not initialized for all skills");
        }
        // Find the n most likely skills according to cosine similarity
        List<String> allSkills = new ArrayList<>(idToSkill.keySet());
        allSkills.sort((o1, o2) -> {
            Skill skill1 = idToSkill.get(o1);
            Skill skill2 = idToSkill.get(o2);
            double similarity1 = cosineSimilarity(embeddings, skill1.getEmbeddings());
            double similarity2 = cosineSimilarity(embeddings, skill2.getEmbeddings());
            return Double.compare(similarity2, similarity1);
        });
        return allSkills.subList(0, Math.min(n, allSkills.size()));
    }

    private boolean isEmbeddingsInitialized() {
        boolean isInitialized = true;
        for (Skill skill : getAllSkillsIdToSkill().values()) {
            if (skill.getEmbeddings() == null) {
                isInitialized = false;
                break;
            }
        }
        return false;
    }

    // Compute Consine Similarity
    private static double cosineSimilarity(List<Double> a, List<Double> b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dotProduct += a.get(i) * b.get(i);
            normA += Math.pow(a.get(i), 2);
            normB += Math.pow(b.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
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
