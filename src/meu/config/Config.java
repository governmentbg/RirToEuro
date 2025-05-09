/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package meu.config;

/**
 *
 * @author user
 */
public enum Config {
    BASE_PATH_LOG("./log/"),
    URL_LOCAL_ITOP("jdbc:mariadb://127.0.0.1:3306/itop"),
    URL_LOCAL_DAEUREPORTS("jdbc:mariadb://127.0.0.1:3306/daeu_reports"),
    URL_TEST_ITOP("jdbc:mariadb://XXX.XX.XXX.XX:3306/itop"),
    URL_TEST_DAEUREPORTS("jdbc:mariadb://XXX.XX.XXX.XX:3306/daeu_reports"),
    URL_PROD_ITOP("jdbc:mariadb://XXX.XX.XXX.XX:3306/itop"),
    URL_PROD_DAEUREPORTS("jdbc:mariadb://XXX.XX.XXX.XX:3306/daeu_reports"),
    DRIVER_MARIADB("org.mariadb.jdbc.Driver"),
    UNM_LOCAL(""),  // Username Local DB
    PSD_LOCAL(""),  // Password Local DB
    UNM_TEST(""),  // Username Test DB
    PSD_TEST(""),  // Password Test DB
    UNM_PROD(""),  // Username Prod DB
    PSD_PROD(""),  // Password Prod DB
    ;

    private Config(final boolean defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
        this.value = this.defaultValue;
        this.type = TYPE_BOOL;
    }

    private Config(final String defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.type = TYPE_STRING;
    }

    private Config(final int defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
        this.value = this.defaultValue;
        this.type = TYPE_INTEGER;
    }

    private Config(final short defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
        this.value = this.defaultValue;
        this.type = TYPE_SHORT;
    }

    private Config(final long defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
        this.value = this.defaultValue;
        this.type = TYPE_LONG;
    }

    private Config(final double defaultValue) {
        this.defaultValue = String.valueOf(defaultValue);
        this.value = this.defaultValue;
        this.type = TYPE_DOUBLE;
    }

    private Config(final Object defaultInstance) {
        this.defaultValue = defaultInstance.getClass().getCanonicalName();
        this.value = this.defaultValue;
        this.objInstance = defaultInstance;
        this.type = TYPE_CLASS;
    }

    private final String defaultValue;
    private String value;
    private final int type;
    private boolean setFromConfig;
    private Object objInstance;

    private static final int TYPE_STRING = 0;
    private static final int TYPE_INTEGER = 1;
    private static final int TYPE_LONG = 2;
    private static final int TYPE_BOOL = 3;
    private static final int TYPE_CLASS = 4;
    private static final int TYPE_DOUBLE = 5;
    private static final int TYPE_SHORT = 6;

    private void set(final String value) throws Exception {
        switch (type) {
            case TYPE_STRING:
                break;
            case TYPE_INTEGER:
                try {
                Integer.parseInt(value);
            } catch (Exception e) {
                throw new Exception("Not an integer '" + value + "'");
            }
            break;
            case TYPE_SHORT:
                try {
                Short.parseShort(value);
            } catch (Exception e) {
                throw new Exception("Not an short '" + value + "'");
            }
            break;
            case TYPE_LONG:
                try {
                Long.parseLong(value);
            } catch (Exception e) {
                throw new Exception("Not an long '" + value + "'");
            }
            break;
            case TYPE_BOOL:
                if (!value.equalsIgnoreCase("true")
                        && !value.equalsIgnoreCase("false")) {
                    throw new Exception("Not a boolean '" + value + "'");
                }
                break;
            case TYPE_CLASS:
                objInstance = Config.class.getClassLoader()
                        .loadClass(value).newInstance();
                break;
            case TYPE_DOUBLE:
                try {
                Double.parseDouble(value);
            } catch (Exception e) {
                throw new Exception("Not an double '" + value + "'");
            }
            break;
            default:
                throw new Exception("Unexpected type " + type);
        }
        this.value = value;
        this.setFromConfig = true;
    }

    public String get() {
        return value;
    }

    public boolean getBoolean() {
        return Boolean.parseBoolean(get());
    }

    public <T> T getInstance() {
        return (T) objInstance;
    }

    public int getInt() {
        return Integer.parseInt(get());
    }

    public short getShort() {
        return Short.parseShort(get());
    }

    public long getLong() {
        return Long.parseLong(get());
    }

    public float getFloat() {
        return Float.parseFloat(get());
    }

    public Double getDouble() {
        return Double.parseDouble(get());
    }

}
