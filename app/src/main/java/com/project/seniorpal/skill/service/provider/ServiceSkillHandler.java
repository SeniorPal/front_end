package com.project.seniorpal.skill.service.provider;

import android.os.RemoteException;
import android.util.Log;
import com.project.seniorpal.skill.Skill;
import com.project.seniorpal.skill.service.ISkillProvider;
import com.project.seniorpal.skill.service.util.SkillDataWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceSkillHandler extends ISkillProvider.Stub {

    private final ServiceSkillProvider skillProvider;

    public ServiceSkillHandler(ServiceSkillProvider skillProvider) {
        this.skillProvider = skillProvider;
    }

    @Override
    public List<SkillDataWrapper> getAllSkills() throws RemoteException {
        return skillProvider.exportedSkills.getAllSkillsIdToSkill().values().stream().map(SkillDataWrapper::new).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> activeSkill(SkillDataWrapper skillData, Skill.ActivatorType activatorType) throws RemoteException {
        synchronized (skillProvider.lockForSkillRemoteActivating) {
            Skill targetSkill = skillProvider.exportedSkills.getSkillById(skillData.id);
            if (targetSkill == null) {
                HashMap<String, String> result = new HashMap<>();
                result.put("succeed", Boolean.toString(false));
                result.put("reason", "No such Skill: " + skillData.id);
                return result;
            }
            try {
                return targetSkill.active(skillData.args, activatorType);
            } catch (Exception e) {
                Log.i("Skill", e.getMessage());
                return null;
            }
        }
    }
}
