package com.project.seniorpal.skill;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A skill is a model of a "skill" in normal language. It provides an id for identifying and descriptions for AI activators to read.
 * The arg and result are both String to String maps while a key is the name of an item and the value is the item's value.
 */
public abstract class Skill {

    public enum ActivatorType implements Parcelable {
        NON_AI, GPT;

        public static final Creator<ActivatorType> CREATOR = new Creator<ActivatorType>() {
            @Override
            public ActivatorType createFromParcel(Parcel in) {
                return ActivatorType.values()[in.readInt()];
            }

            @Override
            public ActivatorType[] newArray(int size) {
                return new ActivatorType[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }
    }

    /**
     * For identifying a Skill. Must be unique. It should be in the same format of a package.
     * It can be the same as the skill's package but not suggested. For example, this class's id can be "xyz.magicalstone.touchcontrol.skill.Skill".
     * It is suggested to be made up of the project's top level package and a path which describes the skill and the skill's name.
     * For example, my top level package is "xyz.magicalstone.touchcontrol", the path can be "base", so the id can be "xyz.magicalstone.touchcontrol.base.Skill".
     * Be sure that the abstract class Skill shouldn't have an id and these examples are just to show what an id should be.
     */
    public final String id;

    /**
     * For an AI to read and "understand". It should be written in natural language and default should be in the Queen's English.
     */
    public final String desc;

    /**
     * For an AI to read and "understand".
     * A key is for an item in args and its value for its description should be written in natural language and default should be in the Queen's English.
     */
    public final Map<String, String> argsDesc;


    public Skill(String id, String desc, Map<String, String> argsDesc) {
        this.id = id;
        this.desc = desc;
        this.argsDesc = Collections.unmodifiableMap(new HashMap<>(argsDesc));
    }

    /**
     * Active the skill.
     * @param args Args for the skill. Should follow the description of argsDesc.
     *             Its keys should be the same as the argsDesc and their value should follow its desc.
     * @param activatorType Determine to preprocess args or not. If it isn't activated by AI, preprocessing is not needed.
     * @return One of the common keys of a return value is "succeed" for if the activity activated succeed or not.
     */
    public Map<String, String> active(Map<String, String> args, ActivatorType activatorType) {
        //TODO If the activator isn't NON_AI, check and fix args if necessary.
        Map<String, String> res = active(args);
        if (res == null) {
            res = new HashMap<>();
            res.put("succeed", Boolean.toString(true));
        } else {
            res.putIfAbsent("succeed", Boolean.toString(true));
        }
        return res;
    }

    /**
     * The impl of activating a skill.
     */
    protected abstract Map<String, String> active(Map<String, String> optimizedArgs);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill)) return false;
        Skill skill = (Skill) o;
        return Objects.equals(id, skill.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
