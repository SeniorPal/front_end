package com.project.seniorpal.skill;

import android.content.Context;
import com.project.seniorpal.skill.accessibility.AccessibilityOperator;

import java.util.Map;

/**
 * A skill requires but only requires for context should be an ContextSkill. Otherwise, it should not be an ContextSkill.
 */
public abstract class ContextSkill extends Skill {
    protected Context operator;

    public ContextSkill(String id, String desc, Map<String, String> argsDesc, Context operator) {
        super(id, desc, argsDesc);
        this.operator = operator;
    }

    public void setContext(Context newOperator) {
        this.operator = newOperator;
    }

}
