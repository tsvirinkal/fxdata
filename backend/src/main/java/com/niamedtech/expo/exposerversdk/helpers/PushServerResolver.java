package com.niamedtech.expo.exposerversdk.helpers;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public interface PushServerResolver {
    CompletableFuture<String> postAsync(URL url, String json) throws CompletionException;
}
