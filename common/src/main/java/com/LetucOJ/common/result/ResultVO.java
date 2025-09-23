package com.LetucOJ.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultVO {
    private int status;
    private Object data;
    private String error;


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
