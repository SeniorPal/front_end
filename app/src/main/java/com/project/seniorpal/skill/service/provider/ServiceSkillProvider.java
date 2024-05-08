package com.project.seniorpal.skill.service.provider;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import androidx.annotation.Nullable;
import com.project.seniorpal.skill.SkillRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Usage:
 * 1. Extend this class.
 * 2. Register the extended class as a service.
 * 3. Register all skills to export after the service started.
 * 4. Call refreshImportedSkills();
 * 5. Other services' and the service's exported skills are all in importedSkills.
 */
public abstract class ServiceSkillProvider extends Service {

    /**
     * Skills that will be seen and invoked by other apps.
     * Should be all added just after the service created.
     */
    public final SkillRegistry exportedSkills = new SkillRegistry();

    final ExecutorService serviceExecutor = Executors.newCachedThreadPool();

    Binder serviceMessenger;

    Object lockForSkillRemoteActivating;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceMessenger = new ServiceSkillHandler(this);
        lockForSkillRemoteActivating = new Object();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceMessenger = null;
        lockForSkillRemoteActivating = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger;
    }


}


