package com.project.seniorpal.skill;

import android.content.Context;

import java.util.Map;

/**
 * A skill requires but only requires for context should be an ContextSkill. Otherwise, it should not be an ContextSkill.
 */
public abstract class ContextSkill extends Skill {
    protected Context context;

    public ContextSkill(String id, String desc, Map<String, String> argsDesc, Context context) {
        super(id, desc, argsDesc);
        this.context = context;
    }

    public void setContext(Context newContext) {
        this.context = newContext;
    }

}
