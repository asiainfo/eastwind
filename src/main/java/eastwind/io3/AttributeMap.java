package eastwind.io3;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AttributeMap {

    private HashMap<String, Object> attributes;

    public Object getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(name);
    }

    public void putAttribute(String name, Object value) {
        checkInit();
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(name);
        }
    }

    public Iterator<Map.Entry<String, Object>> iterator() {
        checkInit();
        return attributes.entrySet().iterator();
    }

    private void checkInit() {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
    }
}
