package play.i18n;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Localized<T> {

    private Map<String, T> values = new HashMap<String, T>();

    public void set(T value) {
        this.values.put(Locale.get(), value);
    }

    public void set(String lang, T value) {
        this.values.put(lang, value);
    }

    public T get() {
        return this.values.get(Locale.get());
    }

    public T get(String lang) {
        return this.values.get(lang);
    }

    @SuppressWarnings("unchecked")
    public Set<T> values() {
        return new HashSet(values.values());
    }

    public Set<String> lang() {
        return values.keySet();
    }

    @Override
    public String toString() {
        T value = get();
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
