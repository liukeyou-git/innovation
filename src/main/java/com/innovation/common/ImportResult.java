// src/main/java/com/innovation/common/ImportResult.java
package com.innovation.common;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult<T> {
    // 有效数据列表
    private List<T> validData = new ArrayList<>();
    // 错误信息列表（格式："行号: 错误描述"）
    private List<String> errorMessages = new ArrayList<>();
    // 是否存在错误
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }
}