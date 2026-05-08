#include <jni.h>
#include <string>
#include <curl/curl.h>
#include <android/log.h>

#define LOG_TAG "NativeHttp"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

size_t WriteCallback(void* contents, size_t size, size_t nmemb, std::string* s) {
    size_t newLength = size * nmemb;
    LOGD("WriteCallback: received %zu bytes", newLength);
    try {
        s->append((char*)contents, newLength);
    } catch (std::bad_alloc& e) {
        LOGE("WriteCallback: memory allocation failed");
        return 0;
    }
    return newLength;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_network_http_NativeHttpManager_nativeGetRequest(
        JNIEnv* env,
        jobject /* this */,
        jstring url) {
    LOGD("nativeGetRequest called");
    
    const char* url_str = env->GetStringUTFChars(url, nullptr);
    if (!url_str) {
        LOGE("nativeGetRequest: Invalid URL (null)");
        return env->NewStringUTF("{\"status\": \"error\", \"message\": \"Invalid URL\"}");
    }
    
    LOGD("nativeGetRequest: URL = %s", url_str);

    CURL* curl = curl_easy_init();
    std::string response;

    if (curl) {
        LOGD("nativeGetRequest: curl initialized successfully");
        
        curl_easy_setopt(curl, CURLOPT_URL, url_str);
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 10L);
        
        LOGD("nativeGetRequest: curl options set, performing request...");

        CURLcode res = curl_easy_perform(curl);
        curl_easy_cleanup(curl);

        if (res != CURLE_OK) {
            LOGE("nativeGetRequest: curl_easy_perform failed, code=%d, msg=%s", 
                 res, curl_easy_strerror(res));
            std::string error = "{\"status\": \"error\", \"message\": \"";
            error += curl_easy_strerror(res);
            error += "\"}";
            env->ReleaseStringUTFChars(url, url_str);
            return env->NewStringUTF(error.c_str());
        }
        
        LOGD("nativeGetRequest: request completed successfully");
    } else {
        LOGE("nativeGetRequest: Failed to initialize curl");
        response = "{\"status\": \"error\", \"message\": \"Failed to initialize curl\"}";
    }

    env->ReleaseStringUTFChars(url, url_str);
    
    if (response.empty()) {
        LOGE("nativeGetRequest: Empty response received");
        return env->NewStringUTF("{\"status\": \"error\", \"message\": \"Empty response\"}");
    }
    
    LOGD("nativeGetRequest: response length = %zu", response.length());
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_demo_network_http_NativeHttpManager_nativePostRequest(
        JNIEnv* env,
        jobject /* this */,
        jstring url,
        jstring body) {
    LOGD("nativePostRequest called");
    
    const char* url_str = env->GetStringUTFChars(url, nullptr);
    const char* body_str = env->GetStringUTFChars(body, nullptr);

    if (!url_str) {
        LOGE("nativePostRequest: Invalid URL (null)");
        return env->NewStringUTF("{\"status\": \"error\", \"message\": \"Invalid URL\"}");
    }
    if (!body_str) {
        LOGE("nativePostRequest: Invalid request body (null)");
        env->ReleaseStringUTFChars(url, url_str);
        return env->NewStringUTF("{\"status\": \"error\", \"message\": \"Invalid request body\"}");
    }
    
    LOGD("nativePostRequest: URL = %s", url_str);
    LOGD("nativePostRequest: Body length = %zu", strlen(body_str));

    CURL* curl = curl_easy_init();
    std::string response;

    if (curl) {
        LOGD("nativePostRequest: curl initialized successfully");
        
        curl_easy_setopt(curl, CURLOPT_URL, url_str);
        curl_easy_setopt(curl, CURLOPT_POST, 1L);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, body_str);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl, CURLOPT_SSL_VERIFYHOST, 0L);
        curl_easy_setopt(curl, CURLOPT_TIMEOUT, 10L);

        struct curl_slist* headers = nullptr;
        headers = curl_slist_append(headers, "Content-Type: application/json");
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        
        LOGD("nativePostRequest: curl options set, performing POST request...");

        CURLcode res = curl_easy_perform(curl);
        
        curl_slist_free_all(headers);
        curl_easy_cleanup(curl);

        if (res != CURLE_OK) {
            LOGE("nativePostRequest: curl_easy_perform failed, code=%d, msg=%s", 
                 res, curl_easy_strerror(res));
            std::string error = "{\"status\": \"error\", \"message\": \"";
            error += curl_easy_strerror(res);
            error += "\"}";
            env->ReleaseStringUTFChars(url, url_str);
            env->ReleaseStringUTFChars(body, body_str);
            return env->NewStringUTF(error.c_str());
        }
        
        LOGD("nativePostRequest: POST request completed successfully");
    } else {
        LOGE("nativePostRequest: Failed to initialize curl");
        response = "{\"status\": \"error\", \"message\": \"Failed to initialize curl\"}";
    }

    env->ReleaseStringUTFChars(url, url_str);
    env->ReleaseStringUTFChars(body, body_str);
    
    if (response.empty()) {
        LOGE("nativePostRequest: Empty response received");
        return env->NewStringUTF("{\"status\": \"error\", \"message\": \"Empty response\"}");
    }
    
    LOGD("nativePostRequest: response length = %zu", response.length());
    return env->NewStringUTF(response.c_str());
}