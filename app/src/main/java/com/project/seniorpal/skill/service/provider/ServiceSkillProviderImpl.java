package com.project.seniorpal.skill.service.provider;

import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.WeChatCallSkill;

public class ServiceSkillProviderImpl extends ServiceSkillProvider {

    @Override
    public void onCreate() {
        super.onCreate();
        //Demo code
        AccessibilityOperator operator = AccessibilityOperator.getServiceInstance();
        if (operator != null) {
            exportedSkills.registerSkill(new WeChatCallSkill(operator));
        }
    }
}
