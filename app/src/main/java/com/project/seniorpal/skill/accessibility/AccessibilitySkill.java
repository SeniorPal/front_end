package com.project.seniorpal.skill.accessibility;


import com.project.seniorpal.skill.Skill;

import java.util.Map;

/**
 * A skill requires for accessibility service should be an AccessibilitySkill. Otherwise, it should not be an AccessibilitySkill.
 */
public abstract class AccessibilitySkill extends Skill {
    protected AccessibilityOperator operator;

    public AccessibilitySkill(String id, String desc, Map<String, String> argsDesc, AccessibilityOperator operator) {
        super(id, desc, argsDesc);
        this.operator = operator;
    }

    void setOperator(AccessibilityOperator newOperator) {
        this.operator = newOperator;
    }
}
