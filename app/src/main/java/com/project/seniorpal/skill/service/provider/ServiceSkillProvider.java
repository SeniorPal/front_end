package com.project.seniorpal.skill.service.provider;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.project.seniorpal.skill.SkillRegistry;
import com.project.seniorpal.skill.service.util.ServiceMessageType;
import com.project.seniorpal.skill.service.util.SkillDataWrapper;
import org.jetbrains.annotations.NotNull;

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

    final Messenger serviceMessenger = new Messenger(new MessageHandler(Looper.getMainLooper()));

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    public class MessageHandler extends Handler {

        private final ServiceSkillHandler serviceSkillHandler = new ServiceSkillHandler(ServiceSkillProvider.this);

        public MessageHandler(@NonNull @NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (ServiceMessageType.values()[msg.what]) {
                case GET_SKILL_LIST:
                    Message reply = Message.obtain(null, ServiceMessageType.GET_SKILL_LIST.ordinal());
                    Bundle bundle = new Bundle();
                    SkillDataWrapper[] dataOfSkills = exportedSkills.getAllSkillsIdToSkill().values().stream().map(skill -> new SkillDataWrapper(skill.id, skill.desc, skill.argsDesc)).toArray(SkillDataWrapper[]::new);
                    bundle.putSerializable("skills", dataOfSkills);
                    reply.setData(bundle);
                    try {
                        msg.replyTo.send(reply);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case ACTIVE_SKILL:
                    serviceSkillHandler.handleMessage(msg);
                    break;
            }
        }
    }
}


