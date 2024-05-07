package com.project.seniorpal.skill.service.collector;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.*;
import com.project.seniorpal.skill.CombinedSkillRegistry;
import com.project.seniorpal.skill.SkillRegistry;
import com.project.seniorpal.skill.service.util.OneTimeConnection;
import com.project.seniorpal.skill.service.util.ServiceMessageType;
import com.project.seniorpal.skill.service.util.SkillDataWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceSkillCollector {

    /**
     * Skills that will be seen and invoked by other apps.
     * Should be all added just after the service created.
     */
    final SkillRegistry importedSkills0 = new SkillRegistry();

    /**
     * Unmodifiable version of importedSkills0.
     */
    public final SkillRegistry importedSkills = new CombinedSkillRegistry(importedSkills0);

    final Context currentContext;

    public ServiceSkillCollector(Context currentContext) {
        this.currentContext = currentContext;
    }

    public Future<Void> refreshImportedSkills() {
        FutureTask<Void> future = new FutureTask<>(() -> {
            importSkills();
            System.out.println("Import finished");
            return null;
        });
        new Thread(future).start();
        return future;
    }

    private void importSkills() {
        List<ResolveInfo> listOfServices = currentContext.getPackageManager().queryIntentServices(new Intent("xyz.magicalstone.seniorpal.SkillProvider"), PackageManager.MATCH_ALL);
        System.out.println(listOfServices);
        if (listOfServices.size() == 0) {
            return;
        }
        Object waiter = new Object();
        final AtomicInteger queryingServiceCounter = new AtomicInteger(listOfServices.size());
        for (ResolveInfo info : listOfServices) {
            String classOfService = info.serviceInfo.packageName;
            ComponentName componentNameOfService = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
            Intent intent = new Intent();
            intent.setComponent(componentNameOfService);
            ServiceConnection[] connection = new ServiceConnection[]{null};
            Messenger receiver = new Messenger(new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Bundle data = msg.getData();
                    SkillDataWrapper[] skillData = (SkillDataWrapper[]) data.getSerializable("skills");
                    synchronized (importedSkills0) {
                        System.out.printf("Received skills %s%n", Arrays.toString(skillData));
                        for (SkillDataWrapper skillDatum : skillData) {
                            System.out.printf("Received skill id = %s desc = %s%n", skillDatum.id, skillDatum.desc);
                            importedSkills0.registerSkill(new ServiceSkillAdapter(skillDatum.id, skillDatum.desc, skillDatum.args, currentContext, componentNameOfService));
                        }
                    }
                    currentContext.unbindService(connection[0]);
                    if (queryingServiceCounter.decrementAndGet() == 0) {
                        synchronized (waiter) {
                            waiter.notify();
                        }
                    }
                }
            });
            Message messageToSend = new Message();
            messageToSend.what = ServiceMessageType.GET_SKILL_LIST.ordinal();
            connection[0] = new OneTimeConnection(receiver, messageToSend);
            currentContext.bindService(intent, connection[0], Context.BIND_AUTO_CREATE);
        }
        synchronized (waiter) {
            try {
                waiter.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
