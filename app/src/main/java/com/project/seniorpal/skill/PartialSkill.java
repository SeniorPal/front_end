package com.project.seniorpal.skill;

import java.util.Map;
import java.util.function.BiPredicate;

public class PartialSkill extends Skill {

    private final Skill original;
    private final Map<String, String> overrideArgs;

    public PartialSkill(String id, String desc, Map<String, String> argsDesc, Skill original, Map<String, String> overrideArgs) {
        super(id, desc, removeAllValuesFromMap(argsDesc, (k, v) -> overrideArgs.containsKey(k)));
        this.original = original;
        this.overrideArgs = overrideArgs;
    }

    @Override
    protected Map<String, String> active(Map<String, String> optimizedArgs) {
        optimizedArgs.replaceAll((k, v) -> overrideArgs.get(k));
        return original.active(optimizedArgs, ActivatorType.NON_AI);
    }

    private static <K, V> Map<K, V> removeAllValuesFromMap(Map<K, V> from, BiPredicate<K, V> removeIf) {
        from.entrySet().removeIf(entry -> removeIf.test(entry.getKey(), entry.getValue()));
        return from;
    }
}
