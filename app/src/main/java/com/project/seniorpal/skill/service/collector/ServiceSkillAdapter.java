package com.project.seniorpal.skill.service.collector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import com.project.seniorpal.skill.ContextSkill;
import com.project.seniorpal.skill.service.ISkillProvider;
import com.project.seniorpal.skill.service.provider.ServiceSkillProvider;
import com.project.seniorpal.skill.service.util.OneTimeConnection;
import com.project.seniorpal.skill.service.util.SkillDataWrapper;

import java.util.HashMap;
import java.util.Map;

public class ServiceSkillAdapter extends ContextSkill {

    private final ComponentName providerServiceName;

    ServiceSkillAdapter(String id, String desc, Map<String, String> args, Context baseService, ComponentName providerServiceName) {
        super(id, desc, args, baseService);
        this.providerServiceName = providerServiceName;
    }

    ServiceSkillAdapter(SkillDataWrapper wrapper, Context baseService, ComponentName providerServiceName) {
        super(wrapper.id, wrapper.desc, wrapper.args, baseService);
        this.providerServiceName = providerServiceName;
    }

    @Override
    public Map<String, String> active(Map<String, String> args, ActivatorType activatorType) {
        Intent intent = new Intent();
        intent.setComponent(providerServiceName);
        OneTimeConnection connection = new OneTimeConnection();
        Map<String, String>[] result = new Map[]{null};
        connection.setCallback((name, service) -> {
            ISkillProvider provider = ISkillProvider.Stub.asInterface(service);
            try {
                result[0] = provider.activeSkill(new SkillDataWrapper(this.id, "", args), activatorType);
                if (result[0] == null) {
                    result[0] = new HashMap<>();
                    result[0].put("succeed", Boolean.toString(false));
                    result[0].put("reason", "Misc exception.");
                }
            } catch (RemoteException e) {
                result[0].put("succeed", Boolean.toString(false));
                result[0].put("reason", e.getMessage());
            } finally {
                context.unbindService(connection);
                synchronized (result) {
                    result.notify();
                }
            }
        });
        if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            synchronized (result) {
                try {
                    result.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            context.unbindService(connection);
            result[0] = new HashMap<>();
            result[0].put("succeed", Boolean.toString(false));
            result[0].put("reason", "No such ServiceProvider. Maybe it is disabled or uninstalled.");
        }
        return result[0];
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        throw new IllegalStateException("This method " + ServiceSkillProvider.class + ".active(Map<String, String> optimizedArgs) shouldn't be invoked.");
    }
}

