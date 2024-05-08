package com.project.seniorpal.skill.service.util;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.project.seniorpal.skill.Skill;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

public class SkillDataWrapper implements Parcelable {

    public String id;

    public String desc;

    public Map<String, String> args;

    public transient ComponentName providerServiceName;

    public SkillDataWrapper(String id, String desc, Map<String, String> args) {
        this.id = id;
        this.desc = desc;
        this.args = args;
    }

    public SkillDataWrapper(Skill skill) {
        this.id = skill.id;
        this.desc = skill.desc;
        this.args = skill.argsDesc;
    }

    protected SkillDataWrapper(Parcel in) {
        id = in.readString();
        desc = in.readString();
        Bundle bundle = in.readBundle(getClass().getClassLoader());
        args = new HashMap<>(bundle.size());
        for (String key : bundle.keySet()) {
            args.put(key, bundle.getString(key));
        }
    }

    public static final Creator<SkillDataWrapper> CREATOR = new Creator<SkillDataWrapper>() {
        @Override
        public SkillDataWrapper createFromParcel(Parcel in) {
            return new SkillDataWrapper(in);
        }

        @Override
        public SkillDataWrapper[] newArray(int size) {
            return new SkillDataWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(desc);
        Bundle bundle = new Bundle(args.size());
        args.forEach(bundle::putString);
        dest.writeBundle(bundle);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{" + "\"type\": \"function\"," + "\"function\": {" + "\"name\": \"");
        builder.append(id);
        builder.append("\"," + "\"description\": \"");
        builder.append(desc);
        builder.append("\"," + "\"parameters\": ");

        builder.append("{" + "\"type\": \"object\"," + "\"properties\": " + '{');
        for (Map.Entry<String, String> entry : args.entrySet()) {
            builder.append('"' + entry.getKey() + '"' + ": { \"type\": \"string\",");
            builder.append("\"description\": \"" + entry.getValue() + '\"' + "},");
        }
        builder.append("},\"required\": [");
        for (String key : args.keySet()) {
            builder.append('\"' + key + '\"' + ',');
        }
        builder.append("]} }}");
        return builder.toString();
    }
}
