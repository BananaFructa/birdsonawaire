package BananaFructa.bow;

import journeymap.client.data.WorldData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Utils {

    static HashMap<String,Field> fieldCache = new HashMap<>();

    public static <T> T readDeclaredField(Class<?> targetType, Object target, String name) {
        try {
            String key = targetType.getName() + " ~ " + name;
            Field f;
            if (fieldCache.containsKey(key)) {
                f = fieldCache.get(key);
            } else {
                f = targetType.getDeclaredField(name);
                f.setAccessible(true);
                fieldCache.put(key,f);
            }
            return (T) f.get(target);
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public static Method getDeclaredMethod(Class<?> targetClass, String name, Class<?>... parameters) {
        try {
            Method m = targetClass.getDeclaredMethod(name,parameters);
            m.setAccessible(true);
            return m;
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public static Method getDeclaredMethod(Class<?> targetClass, String name) {
        try {
            Method m = targetClass.getDeclaredMethod(name);
            m.setAccessible(true);
            return m;
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public static String getSaveDirectory() throws Exception {
        WorldData data = new WorldData();
        data.load(Object.class);
        boolean singleplayer = Utils.readDeclaredField(WorldData.class,data,"singlePlayer");
        String name = Utils.readDeclaredField(WorldData.class,data,"name");
        int dim = Utils.readDeclaredField(WorldData.class,data,"dimension");

        return "journeymap/data/" + (singleplayer ? "sp" : "mp") + "/" + name + "/DIM" + dim;
    }

    public static String getSaveDirectory(int dim) throws Exception {
        WorldData data = new WorldData();
        data.load(Object.class);
        boolean singleplayer = Utils.readDeclaredField(WorldData.class,data,"singlePlayer");
        String name = Utils.readDeclaredField(WorldData.class,data,"name");

        return "journeymap/data/" + (singleplayer ? "sp" : "mp") + "/" + name + "/DIM" + dim;
    }

}
