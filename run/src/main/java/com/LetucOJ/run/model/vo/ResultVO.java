package com.LetucOJ.run.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ResultVO implements Serializable {
    private byte status; // 0: accept 1: wrong 2:compile error 3: runtime error 4: timeout 5: server error
    private Object data; // The data returned by the service
    private String error; // Error message if any

    public ResultVO(byte status, Object data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public ResultVO() {

    }

    public String toString() {
        return "resultVO{" +
                "status=" + status +
                ", data=" + dataToString(this.data) +
                ", error='" + error + '\'' +
                '}';
    }

    public String getDataAsString() {
        return dataToString(this.data);
    }

    static public String dataToString(Object data) {
        if (data == null) {
            return "null";
        } else if (data instanceof List) {
            StringBuilder result = new StringBuilder("[");
            for (Object o : (List) data) {
                result.append(dataToString(o) + ",");
            }
            result.deleteCharAt(result.length() - 1);
            result.append("]");
            return result.toString();
        } else if (data.getClass().isArray()) {
            return new String((byte[]) data);
        } else {
            return data.toString();
        }
    }
}
