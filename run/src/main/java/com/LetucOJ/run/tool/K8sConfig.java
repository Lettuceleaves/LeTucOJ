package com.LetucOJ.run.tool;

import io.kubernetes.client.openapi.ApiClient;

public interface K8sConfig {
    ApiClient k8sConfigManager();
}
