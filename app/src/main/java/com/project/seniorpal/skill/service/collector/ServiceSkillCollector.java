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
import com.project.seniorpal.skill.service.ISkillProvider;
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

    public Future<Void> importAllSkills() {
        FutureTask<Void> future = new FutureTask<>(() -> {
            importSkills();
            System.out.println("Import finished");
            return null;
        });
        new Thread(future).start();
        return future;
    }

    private void importSkills() {
        List<ResolveInfo> listOfServices = currentContext.getPackageManager().queryIntentServices(new Intent("com.project.seniorpal.SkillProvider"), PackageManager.MATCH_ALL);
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

            OneTimeConnection connection = new OneTimeConnection();
            connection.setCallback((name, service) -> {
                ISkillProvider provider = ISkillProvider.Stub.asInterface(service);
                try {
                    List<SkillDataWrapper> dataWrappers = provider.getAllSkills();
                    for (SkillDataWrapper dataWrapper : dataWrappers) {
                        importedSkills0.registerSkill(new ServiceSkillAdapter(dataWrapper, currentContext, componentNameOfService));
                    }
                } catch (RemoteException ignored) {
                } finally {
                    currentContext.unbindService(connection);
                    if (queryingServiceCounter.decrementAndGet() == 0) {
                        synchronized (waiter) {
                            waiter.notify();
                        }
                    }
                }
            });

            currentContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
        synchronized (waiter) {
            try {
                waiter.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
