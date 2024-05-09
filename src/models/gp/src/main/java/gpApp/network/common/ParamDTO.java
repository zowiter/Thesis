package gpApp.network.common;

/**
 * network.common.ParamDTO defines a pair of data, namely the parameter and its value. The
 * object is used define an input parameter.
 *
 * @version 1.0
 */
public class ParamDTO {
    /**
     * name of the parameter
     */
    private String param;

    /**
     * value of the parameter
     *
     * @see ParamDTO#param
     */
    private String val;

    /**
     * Constructor method
     *
     * @param parameter a parameter
     * @param value     the value of the parameter
     */
    public ParamDTO(String parameter, String value) {
        this.param = parameter;
        this.val = value;
    }

    /**
     * Sets the name of the parameter
     *
     * @param str name of the parameter
     */
    public void setParam(String str) {
        this.param = str;
    }

    /**
     * Sets the value of the parameter
     *
     * @param val value of the parameter
     */
    public void setVal(String val) {
        this.val = val;
    }

    /**
     * Returns the name of the parameter
     *
     * @return name of the parameter
     */
    public String getParam() {
        return this.param;
    }

    /**
     * Returns the value of the parameter
     *
     * @return value of the parameter
     */
    public String getVal() {
        return this.val;
    }

}