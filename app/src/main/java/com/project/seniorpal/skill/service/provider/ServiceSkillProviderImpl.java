package com.project.seniorpal.skill.service.provider;

import com.project.seniorpal.skill.accessibility.AccessibilityOperator;
import com.project.seniorpal.skill.accessibility.CheckWeatherSkill;
import com.project.seniorpal.skill.accessibility.WeChatCallSkill;
import com.project.seniorpal.skill.common.OpenAppByPackageNameSkill;
import com.project.seniorpal.skill.common.ScheduleMeetingSkill;
import com.project.seniorpal.skill.common.SendSmsSkill;

public class ServiceSkillProviderImpl extends ServiceSkillProvider {

    @Override
    public void onCreate() {
        super.onCreate();
        //Demo code
        AccessibilityOperator operator = AccessibilityOperator.getServiceInstance();
        if (operator != null) {
            exportedSkills.registerSkill(new WeChatCallSkill(operator));
//            exportedSkills.registerSkill(new CheckWeatherSkill(operator));
        }
        exportedSkills.registerSkill(new ScheduleMeetingSkill(this));
        exportedSkills.registerSkill(new SendSmsSkill(this));
    }
}
