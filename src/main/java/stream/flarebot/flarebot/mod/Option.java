package stream.flarebot.flarebot.mod;

import java.util.Objects;

public class Option {

    private String key;
    private String name;
    private Object defaultValue;
    private Object value;

    public Option(String key, String name, Object defaultValue) {
        this.key = key;
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getKey() {
        if (Objects.isNull(key)) throw new IllegalStateException("Option key is null!");
        return this.key;
    }

    public String getName() {
        if (Objects.isNull(name)) throw new IllegalStateException("Option name is null!");
        return this.name;
    }

    public String stringDefaultValue() {
        if (Objects.isNull(defaultValue)) throw new IllegalStateException("Option default value is null!");
        return (String) defaultValue;
    }

    public String stringValue() {
        if (Objects.isNull(value)) throw new IllegalStateException("Option value is null!");
        return (String) value;
    }

    public int intDefaultValue() {
        if (Objects.isNull(defaultValue)) throw new IllegalStateException("Option default value is null!");
        return (Integer) defaultValue;
    }

    public int intValue() {
        if (Objects.isNull(value)) throw new IllegalStateException("Option value is null!");
        return (Integer) value;
    }

    public char charDefaultValue() {
        if (Objects.isNull(defaultValue)) throw new IllegalStateException("Option default value is null!");
        return (Character) defaultValue;
    }

    public char charValue() {
        if (Objects.isNull(value)) throw new IllegalStateException("Option value is null!");
        return (Character) value;
    }
}
