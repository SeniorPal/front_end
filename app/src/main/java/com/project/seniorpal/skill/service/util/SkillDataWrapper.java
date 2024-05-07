package com.project.seniorpal.skill.service.util;

import android.content.ComponentName;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

public class SkillDataWrapper implements Externalizable {

    public String id;

    public String desc;

    public Map<String, String> args;

    public transient ComponentName providerServiceName;

    public SkillDataWrapper(String id, String desc, Map<String, String> args) {
        this.id = id;
        this.desc = desc;
        this.args = args;
    }

    public SkillDataWrapper() {
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(desc);
        out.writeObject(args);
    }

    @Override
    public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
        id = in.readUTF();
        desc = in.readUTF();
        args = (Map<String, String>) in.readObject();
    }
}
