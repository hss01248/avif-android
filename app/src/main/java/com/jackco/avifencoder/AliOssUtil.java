package com.jackco.avifencoder;

import android.app.Application;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;

public class AliOssUtil {

    static  OSS oss;
    public static void init(Application application){
        try {
            OSSLog.enableLog();  //调用此方法即可开启日志

            String endpoint = "http://testcdndomo.oss-cn-beijing.aliyuncs.com";

            String stsServer = "http://10.0.192.20:7080/";
// 推荐使用OSSAuthCredentialsProvider。token过期可以及时更新。
            OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider(stsServer);

// 配置类如果不设置，会有默认配置。
            ClientConfiguration conf = new ClientConfiguration();
            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒。
            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒。
            conf.setMaxConcurrentRequest(5); // 最大并发请求数，默认5个。
            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次。

            oss = new OSSClient(application, endpoint, credentialProvider);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }


    }

    public static void upload(String filePath){
        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest("testcdndomo", new File(filePath).getName(), filePath);

// 文件元信息的设置是可选的。
// ObjectMetadata metadata = new ObjectMetadata();
// metadata.setContentType("application/octet-stream"); // 设置content-type。
// metadata.setContentMD5(BinaryUtil.calculateBase64Md5(uploadFilePath)); // 校验MD5。
// put.setMetadata(metadata);

        try {
            PutObjectResult putResult = oss.putObject(put);

            Log.d("PutObject", "UploadSuccess");
            Log.d("ETag", putResult.getETag());
            Log.d("RequestId", putResult.getRequestId());
        } catch (ClientException e) {
            // 本地异常，如网络异常等。
            e.printStackTrace();
        } catch (ServiceException e) {
            // 服务异常。
            Log.e("RequestId", e.getRequestId());
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }
}
